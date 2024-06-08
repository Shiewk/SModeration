package de.shiewk.smoderation.punishments;

import de.shiewk.smoderation.event.PunishmentIssueEvent;
import de.shiewk.smoderation.storage.PunishmentContainer;
import de.shiewk.smoderation.util.ByteUtil;
import de.shiewk.smoderation.util.PlayerUtil;
import de.shiewk.smoderation.util.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Punishment {

    public static final NamedTextColor PRIMARY_COLOR = NamedTextColor.RED;
    public static final NamedTextColor SECONDARY_COLOR = NamedTextColor.GOLD;
    public static final String DEFAULT_REASON = "No reason provided.";
    public final PunishmentType type;
    public final long time;
    public final long until;
    public final UUID by;
    public final UUID to;
    public final String reason;

    protected Punishment(PunishmentType type, long time, long until, UUID by, UUID to, String reason) {
        this.type = type;
        this.time = time;
        this.until = until;
        this.by = by;
        this.to = to;
        this.reason = reason;
    }

    public static Punishment mute(long time, long until, UUID by, UUID to, String reason){
        return new Punishment(PunishmentType.MUTE, time, until, by, to, reason);
    }

    public static Punishment ban(long time, long until, UUID by, UUID to, String reason){
        return new Punishment(PunishmentType.BAN, time, until, by, to, reason);
    }

    public static Punishment kick(long time, UUID by, UUID to, String reason){
        return new Punishment(PunishmentType.KICK, time, time, by, to, reason);
    }

    private static final int BUFFER_LENGTH = 56;

    public byte[] toBytes(){
        final byte[] reasonBytes = reason.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_LENGTH + reasonBytes.length);
        buffer.putInt(0, type.ordinal());
        buffer.putLong(4, time);
        buffer.putLong(12, until);
        buffer.put(20, ByteUtil.uuidToBytes(by));
        buffer.put(36, ByteUtil.uuidToBytes(to));
        buffer.putInt(40, reason.length());
        buffer.put(44, reasonBytes);
        return buffer.array();
    }

    /**
     * @deprecated behaves weirdly, does not support punishment reasons
     */
    @Deprecated(forRemoval = true)
    public static @NotNull Punishment fromBytes(byte[] bytes){
        if (bytes.length != BUFFER_LENGTH){
            throw new IllegalArgumentException("the array has to be %s in length".formatted(BUFFER_LENGTH));
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        final int ptypeIndex = buffer.getInt(0);
        PunishmentType ptype;
        try {
            ptype = PunishmentType.values()[ptypeIndex];
        } catch (IndexOutOfBoundsException e){
            throw new IllegalArgumentException("The punishment type %s does not exist.".formatted(ptypeIndex));
        }
        final long time = buffer.getLong(4);
        final long until = buffer.getLong(12);
        final byte[] byBytes = new byte[16];
        System.arraycopy(bytes, 20, byBytes, 0, 16);
        final byte[] toBytes = new byte[16];
        System.arraycopy(bytes, 36, toBytes, 0, 16);
        final UUID by = ByteUtil.bytesToUuid(byBytes);
        final UUID to = ByteUtil.bytesToUuid(toBytes);
        return new Punishment(ptype, time, until, by, to, "");
    }

    @Override
    public String toString() {
        return "Punishment{" +
                "type=" + type +
                ", time=" + time +
                ", until=" + until +
                ", by=" + by +
                ", to=" + to +
                ", reason=" + reason +
                '}';
    }

    public static void issue(Punishment punishment, PunishmentContainer container){
        final PunishmentIssueEvent event = new PunishmentIssueEvent(punishment, container);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()){
            container.add(punishment);
            punishment.firstIssue(container);
        }
    }

    private Component broadcastMessage(){
        final String toName = PlayerUtil.offlinePlayerName(to);
        switch (type) {
            case MUTE -> {
                return Component.text(toName).color(SECONDARY_COLOR).append(
                        Component.text(" has been muted by ").color(PRIMARY_COLOR)
                        .append(Component.text(PlayerUtil.offlinePlayerName(this.by)).color(SECONDARY_COLOR))
                        .append(Component.text(" for "))
                        .append(Component.text(TimeUtil.formatTimeLong(this.until - this.time)).color(SECONDARY_COLOR))
                        .append(Component.text(".\nReason: "))
                        .append(Component.text(reason).color(SECONDARY_COLOR)));
            }
            case KICK -> {
                return Component.text(toName).color(SECONDARY_COLOR).append(
                        Component.text(" has been kicked by ").color(PRIMARY_COLOR)
                        .append(Component.text(PlayerUtil.offlinePlayerName(this.by)).color(SECONDARY_COLOR))
                        .append(Component.text(".\nReason: "))
                        .append(Component.text(reason).color(SECONDARY_COLOR))
                        .color(PRIMARY_COLOR));
            }
            case BAN -> {
                return Component.text(toName).color(SECONDARY_COLOR).append(
                        Component.text(" has been banned by ").color(PRIMARY_COLOR)
                        .append(Component.text(PlayerUtil.offlinePlayerName(this.by)).color(SECONDARY_COLOR))
                        .append(Component.text(" for "))
                        .append(Component.text(TimeUtil.formatTimeLong(this.until - this.time)).color(SECONDARY_COLOR))
                        .append(Component.text(".\nReason: "))
                        .append(Component.text(reason).color(SECONDARY_COLOR))
                        .color(PRIMARY_COLOR));
            }
            default -> throw new IllegalStateException("Unknown punishment type " + type);
        }
    }

    private void broadcastIssue(PunishmentContainer container){
        for (CommandSender sender : container.collectBroadcastTargets()) {
            sender.sendMessage(broadcastMessage());
        }
    }

    private void firstIssue(PunishmentContainer container){
        switch (type) {
            case MUTE, BAN -> {
                final CommandSender sender = PlayerUtil.senderByUUID(to);
                if (sender != null) {
                    sender.sendMessage(playerMessage());
                }
            }
        }
        broadcastIssue(container);
    }

    public Component playerMessage(){
        switch (type) {
            case MUTE -> {
                return Component.text("You have been muted by ")
                    .append(Component.text(PlayerUtil.offlinePlayerName(this.by)).color(SECONDARY_COLOR))
                    .append(Component.text(".\nReason: "))
                    .append(Component.text(reason).color(SECONDARY_COLOR))
                    .append(Component.text("\nYour mute expires in "))
                    .append(Component.text(TimeUtil.formatTimeLong(this.until - System.currentTimeMillis())).color(SECONDARY_COLOR))
                    .append(Component.text("."))
                    .color(PRIMARY_COLOR);
            }
            case KICK -> {
                return Component.text("You have been kicked by ")
                    .append(Component.text(PlayerUtil.offlinePlayerName(this.by)).color(SECONDARY_COLOR))
                    .append(Component.text(".\nReason: "))
                    .append(Component.text(reason).color(SECONDARY_COLOR))
                    .color(PRIMARY_COLOR);
            }
            case BAN -> {
                return Component.text("You have been banned from this server by ")
                        .append(Component.text(PlayerUtil.offlinePlayerName(this.by)).color(SECONDARY_COLOR))
                        .append(Component.text(".\nReason: "))
                        .append(Component.text(reason).color(SECONDARY_COLOR))
                        .append(Component.text("\nYour ban expires in "))
                        .append(Component.text(TimeUtil.formatTimeLong(this.until - System.currentTimeMillis())).color(SECONDARY_COLOR))
                        .append(Component.text("."))
                        .color(PRIMARY_COLOR);
            }
            default -> throw new IllegalStateException("Unknown punishment type " + type);
        }
    }
}

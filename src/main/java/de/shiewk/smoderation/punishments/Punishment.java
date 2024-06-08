package de.shiewk.smoderation.punishments;

import de.shiewk.smoderation.event.PunishmentIssueEvent;
import de.shiewk.smoderation.storage.PunishmentContainer;
import de.shiewk.smoderation.util.ByteUtil;
import de.shiewk.smoderation.util.PlayerUtil;
import de.shiewk.smoderation.util.TimeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.nio.ByteBuffer;
import java.util.UUID;

import static de.shiewk.smoderation.SModeration.*;

public class Punishment {
    public static final String DEFAULT_REASON = "No reason provided.";
    public final PunishmentType type;
    public final long time;
    public final long until;
    public final UUID by;
    public final UUID to;
    public final String reason;
    private UUID undoneBy;

    public Punishment(PunishmentType type, long time, long until, UUID by, UUID to, String reason) {
        this.type = type;
        this.time = time;
        this.until = until;
        this.by = by;
        this.to = to;
        this.reason = reason;
    }

    public boolean wasUndone(){
        return undoneBy != null;
    }

    public UUID undoneBy() {
        return undoneBy;
    }

    public void undo(UUID undoneBy){
        if (this.undoneBy != null){
            throw new IllegalArgumentException("This punishment was already undone.");
        }
        this.undoneBy = undoneBy;
    }

    public boolean isActive(){
        return until > System.currentTimeMillis() && !wasUndone();
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
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_LENGTH + reasonBytes.length + (undoneBy != null ? 17 : 1));
        buffer.putInt(0, type.ordinal());
        buffer.putLong(4, time);
        buffer.putLong(12, until);
        buffer.put(20, ByteUtil.uuidToBytes(by));
        buffer.put(36, ByteUtil.uuidToBytes(to));
        buffer.putInt(40, reason.length());
        buffer.put(44, reasonBytes);
        buffer.put(44+reasonBytes.length, undoneBy != null ? (byte) 1 : (byte) 0);
        if (undoneBy != null){
            buffer.put(44+reasonBytes.length+1, ByteUtil.uuidToBytes(undoneBy));
        }
        return buffer.array();
    }

    private Component undoMessage(){
        String msg = "";
        switch (type){
            case MUTE -> msg = "unmuted";
            case KICK -> msg = "unkicked??";
            case BAN -> msg = "unbanned";
        }
        return Component.text(PlayerUtil.offlinePlayerName(to)).color(SECONDARY_COLOR)
                .append(Component.text(" was ").color(PRIMARY_COLOR))
                .append(Component.text(msg))
                .append(Component.text(" by ").color(PRIMARY_COLOR))
                .append(Component.text(PlayerUtil.offlinePlayerName(undoneBy)))
                .append(Component.text(".").color(PRIMARY_COLOR));
    }

    public void broadcastUndo(PunishmentContainer container){
        for (CommandSender sender : container.collectBroadcastTargets()) {
            sender.sendMessage(CHAT_PREFIX.append(undoMessage()));
        }
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
            sender.sendMessage(CHAT_PREFIX.append(broadcastMessage()));
        }
    }

    private void firstIssue(PunishmentContainer container){
        switch (type) {
            case MUTE, BAN -> {
                final CommandSender sender = PlayerUtil.senderByUUID(to);
                if (sender != null) {
                    sender.sendMessage(CHAT_PREFIX.append(playerMessage()));
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

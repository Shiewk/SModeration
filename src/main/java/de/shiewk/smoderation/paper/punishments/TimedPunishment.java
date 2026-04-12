package de.shiewk.smoderation.paper.punishments;

import de.shiewk.smoderation.paper.command.argument.DurationArgument;
import de.shiewk.smoderation.paper.util.PlayerUtil;
import de.shiewk.smoderation.paper.util.SerializationHelper;
import de.shiewk.smoderation.paper.util.TimeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public abstract class TimedPunishment extends Punishment {

    protected final long duration;
    protected UUID cancelledBy;

    protected TimedPunishment(UUID id, String type, long timestamp, UUID issuer, UUID target, String reason, long duration, UUID cancelledBy) {
        super(id, type, timestamp, issuer, target, reason);
        this.duration = duration;
        this.cancelledBy = cancelledBy;
    }

    public long getDuration() {
        return duration;
    }

    public UUID getCancelledBy() {
        return cancelledBy;
    }

    public boolean wasCancelled(){
        return cancelledBy != null;
    }

    public boolean isActive(){
        return !wasCancelled() && (isPermanent() || System.currentTimeMillis() < getExpiry());
    }

    @Override
    public void addSerializableProperties(SerializationHelper helper) {
        super.addSerializableProperties(helper);
        helper.putLong("duration", duration);
        helper.putUUID("cancelledBy", cancelledBy);
    }

    @Override
    public boolean matchesSearchQuery(String query) {
        if (super.matchesSearchQuery(query)) return true;
        query = query.toLowerCase();
        return cancelledBy.toString().equalsIgnoreCase(query)
                || PlayerUtil.offlinePlayerName(cancelledBy).toLowerCase().contains(query);
    }

    @Override
    public Component infoMessage(){
        if (isPermanent()){
            return translatable(
                    "smod.punishment.playerMessage." + type + ".permanent",
                    text(PlayerUtil.offlinePlayerName(this.issuer)),
                    text(reason)
            );
        } else {
            return translatable(
                    "smod.punishment.playerMessage." + type,
                    text(PlayerUtil.offlinePlayerName(this.issuer)),
                    text(reason),
                    TimeUtil.formatTimeLong(this.timestamp + this.duration - System.currentTimeMillis())
            );
        }
    }

    @Override
    public Component adminMessage(){
        if (isPermanent()){
            return translatable(
                    "smod.punishment.broadcast." + type + ".permanent",
                    text(PlayerUtil.offlinePlayerName(target)),
                    text(PlayerUtil.offlinePlayerName(issuer)),
                    text(reason)
            );
        } else {
            return translatable(
                    "smod.punishment.broadcast." + type,
                    text(PlayerUtil.offlinePlayerName(target)),
                    text(PlayerUtil.offlinePlayerName(issuer)),
                    text(reason),
                    TimeUtil.formatTimeLong(this.duration)
            );
        }
    }

    public Component cancelMessage(){
        return translatable(
                "smod.punishment.cancel." + type,
                text(PlayerUtil.offlinePlayerName(target)),
                text(PlayerUtil.offlinePlayerName(cancelledBy))
        );
    }

    public long getExpiry() {
        return isPermanent() ? Long.MAX_VALUE : getTimestamp() + getDuration();
    }

    public boolean isPermanent() {
        return getDuration() == DurationArgument.INFINITE_DURATION;
    }

    protected void cancel(UUID canceller) {
        if (this.cancelledBy != null){
            throw new IllegalArgumentException("This punishment was already cancelled.");
        }
        this.cancelledBy = canceller;
        for (CommandSender sender : getBroadcastTargets()) {
            sender.sendMessage(cancelMessage());
        }
    }
}

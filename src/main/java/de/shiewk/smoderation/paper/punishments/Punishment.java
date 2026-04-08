package de.shiewk.smoderation.paper.punishments;

import de.shiewk.smoderation.paper.util.PlayerUtil;
import de.shiewk.smoderation.paper.util.SerializationHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public abstract class Punishment {

    public static final String DEFAULT_REASON = "No reason provided.";

    protected final UUID id;
    protected final String type;
    protected final long timestamp;
    protected final UUID issuer;
    protected final UUID target;
    protected final String reason;

    protected Punishment(UUID id, String type, long timestamp, UUID issuer, UUID target, String reason) {
        this.id = id;
        this.type = type;
        this.timestamp = timestamp;
        this.issuer = issuer;
        this.target = target;
        this.reason = reason;
    }

    public UUID getID() {
        return id;
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public UUID getIssuerID() {
        return issuer;
    }

    public UUID getTargetID() {
        return target;
    }

    public String getReason() {
        return reason;
    }

    public void addSerializableProperties(SerializationHelper helper){
        helper.putUUID("id", id);
        helper.putString("type", type);
        helper.putLong("timestamp", timestamp);
        helper.putUUID("issuer", issuer);
        helper.putUUID("target", target);
        helper.putString("reason", reason);
    }

    public boolean matchesSearchQuery(String query){
        if (query == null) return true;
        query = query.toLowerCase();
        return reason.toLowerCase().contains(query)
                || issuer.toString().equalsIgnoreCase(query)
                || target.toString().equalsIgnoreCase(query)
                || PlayerUtil.offlinePlayerName(issuer).toLowerCase().contains(query)
                || PlayerUtil.offlinePlayerName(target).toLowerCase().contains(query);
    }

    public Component infoMessage(){
        return translatable(
                "smod.punishment.playerMessage." + type,
                text(PlayerUtil.offlinePlayerName(this.issuer)),
                text(reason)
        );
    }

    public Component adminMessage(){
        return translatable(
                "smod.punishment.broadcast." + type,
                text(PlayerUtil.offlinePlayerName(target)),
                text(PlayerUtil.offlinePlayerName(issuer)),
                text(reason)
        );
    }

    public void processIssue() {
        CommandSender sender = PlayerUtil.senderByUUID(target);
        if (sender != null) {
            sender.sendMessage(infoMessage());
        }
        for (CommandSender target : getBroadcastTargets()) {
            target.sendMessage(adminMessage());
        }
    }

    public static List<CommandSender> getBroadcastTargets() {
        ObjectArrayList<CommandSender> senders = new ObjectArrayList<>();
        senders.add(Bukkit.getConsoleSender());
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("smod.notifications")){
                senders.add(onlinePlayer);
            }
        }
        return List.copyOf(senders);
    }

    public static UUID generateUUID() {
        Random random = new Random();
        return new UUID(System.currentTimeMillis(), random.nextLong());
    }
}

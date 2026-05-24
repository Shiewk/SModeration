package de.shiewk.smoderation.paper.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class PlayerUtil {
    private PlayerUtil(){}

    public static final UUID UUID_CONSOLE = new UUID(0, 0);

    public static @NotNull String offlinePlayerName(UUID uuid){
        if (uuid.equals(UUID_CONSOLE)){
            return "Console";
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return player.getName() == null ? uuid.toString() : player.getName();
    }

    public static @Nullable CommandSender senderByUUID(@NotNull UUID uid){
        if (uid.equals(UUID_CONSOLE)){
            return Bukkit.getConsoleSender();
        } else {
            return Bukkit.getPlayer(uid);
        }
    }

    public static UUID uuidFromString(String string) {
        if (string.length() == 36) {
            return UUID.fromString(string);
        } else {
            return UUID.fromString(string.replaceFirst("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5"));
        }
    }
}

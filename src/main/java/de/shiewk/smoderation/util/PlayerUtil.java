package de.shiewk.smoderation.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class PlayerUtil {
    private PlayerUtil(){}

    public static final UUID UUID_CONSOLE = new UUID(0, 0);

    public static @NotNull String offlinePlayerName(UUID uuid){
        if (uuid.equals(UUID_CONSOLE)){
            return "CONSOLE";
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return player.getName() == null ? uuid.toString() : player.getName();
    }

    public static @Nullable UUID offlinePlayerUUIDByName(String name){
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(name); // getOfflinePlayerIfCached(String) is safer (I have experience with getOfflinePlayer(String) returning wrong UUIDs)
        if (offlinePlayer != null) {
            return offlinePlayer.getUniqueId();
        } else {
            return null;
        }
    }

    public static @Nullable CommandSender senderByUUID(@NotNull UUID uid){
        if (uid.equals(UUID_CONSOLE)){
            return Bukkit.getConsoleSender();
        } else {
            return Bukkit.getPlayer(uid);
        }
    }

}

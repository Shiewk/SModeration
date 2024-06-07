package de.shiewk.smoderation.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public abstract class PlayerUtil {
    private PlayerUtil(){}

    public static String offlinePlayerName(UUID uuid){
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return player.getName() == null ? uuid.toString() : player.getName();
    }

}

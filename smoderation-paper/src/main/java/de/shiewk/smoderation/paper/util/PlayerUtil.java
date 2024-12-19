package de.shiewk.smoderation.paper.util;

import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.punishments.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class PlayerUtil {
    private PlayerUtil(){}

    public static final UUID UUID_CONSOLE = new UUID(0, 0);

    public static @NotNull String offlinePlayerName(UUID uuid){
        if (uuid.equals(UUID_CONSOLE)){
            return "CONSOLE";
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return player.getName() == null ? "Unknown Player " + uuid : player.getName();
    }

    public static @Nullable UUID offlinePlayerUUIDByName(String name){
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(name); // getOfflinePlayerIfCached(String) is safer (I have experience with getOfflinePlayer(String) returning wrong UUIDs)
        if (offlinePlayer != null) {
            return offlinePlayer.getUniqueId();
        } else {
            // try to find uuid by searching through punishments
            final Punishment punishment = SModerationPaper.container.find(p -> offlinePlayerName(p.to).equalsIgnoreCase(name));
            if (punishment != null) {
                return punishment.to;
            }
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

    public static @Nullable Player findOnlinePlayer(String name){
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equalsIgnoreCase(name)){
                return onlinePlayer;
            }
        }
        return null;
    }

    public static List<String> listPlayerNames(){
        return listPlayerNames(pl -> true);
    }

    public static List<String> listPlayerNames(final Predicate<Player> predicate) {
        final ArrayList<String> names = new ArrayList<>();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (predicate.test(onlinePlayer)){
                names.add(onlinePlayer.getName());
            }
        }
        return List.copyOf(names);
    }

    public static List<String> listPlayerNames(String search) {
        return StringUtil.copyPartialMatches(search, listPlayerNames(), new ArrayList<>());
    }
}

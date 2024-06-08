package de.shiewk.smoderation.command;

import de.shiewk.smoderation.SModeration;
import de.shiewk.smoderation.punishments.Punishment;
import de.shiewk.smoderation.util.PlayerUtil;
import de.shiewk.smoderation.util.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MuteCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2){
            return false;
        } else {
            UUID senderUUID;
            if (sender instanceof ConsoleCommandSender){
                senderUUID = PlayerUtil.UUID_CONSOLE;
            } else if (sender instanceof Player pl){
                senderUUID = pl.getUniqueId();
            } else if (sender instanceof BlockCommandSender){
                sender.sendMessage(Component.text("Blocks can't execute this command.").color(NamedTextColor.RED));
                return true;
            } else {
                sender.sendMessage(Component.text("Your command sender type is unknown (%s).".formatted(sender.getClass().getName())).color(NamedTextColor.RED));
                return true;
            }
            String playerName = args[0];
            UUID uuid = PlayerUtil.offlinePlayerUUIDByName(playerName);
            if (senderUUID.equals(uuid)) {
                sender.sendMessage(Component.text("You can't mute yourself.").color(NamedTextColor.RED));
                return true;
            }
            if (uuid == null) {
                sender.sendMessage(Component.text("This player is either offline or was never on this server.").color(NamedTextColor.RED));
                return true;
            }
            long duration = 0;
            int p = 1;
            for (int i = 1 /* start with index 1 to avoid player name */; i < args.length; i++) {
                String arg = args[i];
                long parsedDuration = TimeUtil.parseDurationMillisSafely(arg);
                if (parsedDuration == -1){
                    p = i;
                    break;
                } else {
                    duration += parsedDuration;
                }
            }
            if (duration == 0){
                sender.sendMessage(Component.text("Please provide a valid duration.").color(NamedTextColor.RED));
                return false;
            }
            StringBuilder reason = new StringBuilder();
            for (int i = p; i < args.length; i++) {
                if (!reason.isEmpty()){
                    reason.append(" ");
                }
                reason.append(args[i]);
            }
            final Punishment punishment = Punishment.mute(System.currentTimeMillis(), System.currentTimeMillis() + duration, senderUUID, uuid, reason.isEmpty() ? Punishment.DEFAULT_REASON : reason.toString());
            Punishment.issue(punishment, SModeration.container);
            return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2){
            String toComplete = args.length > 0 ? args[0] : "";
            ArrayList<String> names = new ArrayList<>();
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                names.add(onlinePlayer.getName());
            }
            ArrayList<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(toComplete, names, completions);
            return completions;
        } else {
            for (int i = 1; i < args.length; i++) {
                if (TimeUtil.parseDurationMillisSafely(args[i]) == -1){
                    try {
                        Long.parseLong(args[i]);
                    } catch (NumberFormatException ignored){
                        if (i != 1){
                            return List.of();
                        }
                    }
                }
            }
            return List.of(
                    "100ms",
                    "15s", // some sample completions for duration
                    "30min", // you can input your own ones as well
                    "6h",
                    "1d",
                    "2w",
                    "3mo",
                    "1y"
            );
        }
    }
}

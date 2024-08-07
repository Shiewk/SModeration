package de.shiewk.smoderation.command;

import de.shiewk.smoderation.SModeration;
import de.shiewk.smoderation.punishments.Punishment;
import de.shiewk.smoderation.util.PlayerUtil;
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

import static net.kyori.adventure.text.Component.text;

public class KickCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1){
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
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                sender.sendMessage(Component.text("This player is not online.").color(NamedTextColor.RED));
                return true;
            }
            UUID uuid = player.getUniqueId();
            if (senderUUID.equals(uuid)) {
                sender.sendMessage(Component.text("You can't kick yourself.").color(NamedTextColor.RED));
                return true;
            }
            if (player.hasPermission("smod.preventkick")){
                sender.sendMessage(text().content("This player can't be kicked.").color(NamedTextColor.RED));
                return true;
            }
            StringBuilder reason = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (!reason.isEmpty()){
                    reason.append(" ");
                }
                reason.append(args[i]);
            }
            final Punishment punishment = Punishment.kick(System.currentTimeMillis(), senderUUID, uuid, reason.isEmpty() ? Punishment.DEFAULT_REASON : reason.toString());
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
        }
        return List.of();
    }
}

package de.shiewk.smoderation.command;

import de.shiewk.smoderation.SModeration;
import de.shiewk.smoderation.util.PlayerUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static de.shiewk.smoderation.SModeration.*;
import static net.kyori.adventure.text.Component.text;

public class VanishCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = null;
        if (args.length > 0){
            player = PlayerUtil.findOnlinePlayer(args[0]);
        } else if (sender instanceof Player){
            player = (Player) sender;
        }
        if (player != null){
            toggleVanish(player);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2){
            return PlayerUtil.listPlayerNames();
        }
        return List.of();
    }

    private static final ObjectArrayList<Player> vanishedPlayers = new ObjectArrayList<>();

    public static void toggleVanish(Player player){
        final boolean newStatus = !isVanished(player);
        if (newStatus){
            vanishedPlayers.add(player);
            for (CommandSender sender : SModeration.container.collectBroadcastTargets()) {
                sender.sendMessage(CHAT_PREFIX.append(
                        player.displayName()
                                .colorIfAbsent(SECONDARY_COLOR)
                ).append(
                        text()
                                .content(" vanished.")
                                .color(PRIMARY_COLOR)
                ));
            }
            player.sendMessage(CHAT_PREFIX.append(text("You are now vanished.").color(PRIMARY_COLOR)));
            player.setVisibleByDefault(false);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("smod.vanish.see")){
                    onlinePlayer.showEntity(PLUGIN, player);
                }
            }
        } else {
            vanishedPlayers.remove(player);
            for (CommandSender sender : container.collectBroadcastTargets()) {
                sender.sendMessage(CHAT_PREFIX.append(
                        player.displayName()
                                .colorIfAbsent(SECONDARY_COLOR)
                ).append(
                        text()
                                .content(" re-appeared.")
                                .color(PRIMARY_COLOR)
                ));
            }
            player.sendMessage(CHAT_PREFIX.append(text("You are no longer vanished.").color(PRIMARY_COLOR)));
            player.setVisibleByDefault(true);
        }
    }

    public static boolean isVanished(Player player){
        return vanishedPlayers.contains(player);
    }

    public static ObjectArrayList<Player> getVanishedPlayers() {
        return vanishedPlayers.clone();
    }
}

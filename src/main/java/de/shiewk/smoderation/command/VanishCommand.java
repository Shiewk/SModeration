package de.shiewk.smoderation.command;

import de.shiewk.smoderation.SModeration;
import de.shiewk.smoderation.util.PlayerUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
        if (args.length == 0 || args[0].equalsIgnoreCase("toggle")){
            Player player = null;
            if (args.length > 1){
                player = PlayerUtil.findOnlinePlayer(args[1]);
            } else if (sender instanceof Player){
                player = (Player) sender;
            }
            if (player != null){
                toggleVanish(player);
                return true;
            } else {
                return false;
            }
        } else if (args[0].equalsIgnoreCase("list")) {
            if (sender.hasPermission("smod.vanish.see")){
                listVanishedPlayersTo(sender);
            } else {
                sender.sendMessage(text().color(NamedTextColor.RED).content("You do not have permission to list all vanished players."));
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2){
            return List.of("list", "toggle");
        }
        if (args.length < 3 && args[0].equalsIgnoreCase("toggle")){
            return PlayerUtil.listPlayerNames(args[1]);
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

    public static void listVanishedPlayersTo(CommandSender receiver){
        if (vanishedPlayers.isEmpty()){
            receiver.sendMessage(CHAT_PREFIX.append(
                    text().content("No players are currently vanished.").color(PRIMARY_COLOR)
            ));
        } else {
            Component vanishList = CHAT_PREFIX.append(
                    text().content("The following players are currently vanished: ").color(PRIMARY_COLOR)
            );
            for (ObjectListIterator<Player> iterator = vanishedPlayers.iterator(); iterator.hasNext(); ) {
                Player vanishedPlayer = iterator.next();
                vanishList = vanishList.append(
                        vanishedPlayer.displayName().colorIfAbsent(SECONDARY_COLOR)
                );
                if (iterator.hasNext()){
                    vanishList = vanishList.append(
                            text().content(", ").color(PRIMARY_COLOR)
                    );
                }
            }
            receiver.sendMessage(vanishList);
        }
    }
}

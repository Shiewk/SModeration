package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.event.VanishToggleEvent;
import de.shiewk.smoderation.paper.util.CommandUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

import static de.shiewk.smoderation.paper.SModerationPaper.*;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static net.kyori.adventure.text.Component.text;

public final class VanishCommand implements CommandProvider {

    public static final NamespacedKey KEY_VANISHED = new NamespacedKey("smoderation", "vanished");

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return literal("vanish")
                .requires(CommandUtil.requirePermission("smod.vanish"))
                .executes(this::toggleVanishSelf)
                .then(literal("toggle")
                        .executes(this::toggleVanishSelf)
                        .then(argument("targets", ArgumentTypes.players())
                                .executes(this::toggleVanishForTargets)
                        )
                )
                .then(literal("list")
                        .requires(CommandUtil.requirePermission("smod.vanish.see"))
                        .executes(this::listVanishedPlayers)
                )
                .build();
    }

    private int toggleVanishForTargets(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<Player> targets = context.getArgument("targets", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
        if (targets.isEmpty()){
            CommandUtil.error("No player was found.");
        } else {
            for (Player target : targets) {
                toggleVanish(target);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private int toggleVanishSelf(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        toggleVanish(CommandUtil.getExecutingPlayer(context.getSource()));
        return Command.SINGLE_SUCCESS;
    }

    private int listVanishedPlayers(CommandContext<CommandSourceStack> context) {
        listVanishedPlayersTo(context.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public String getCommandDescription() {
        return "Toggles vanish mode which prevents other players from seeing you're online";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("smvanish", "smodvanish", "v", "smv");
    }

    private static final ObjectArrayList<Player> vanishedPlayers = new ObjectArrayList<>(1);

    public static void toggleVanish(Player player){
        final boolean newStatus = !isVanished(player);
        VanishToggleEvent event = new VanishToggleEvent(player, newStatus);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()){
            return;
        }
        if (newStatus){
            vanishedPlayers.add(player);
            for (CommandSender sender : SModerationPaper.container.collectBroadcastTargets()) {
                sender.sendMessage(CHAT_PREFIX.append(
                        player.displayName().colorIfAbsent(SECONDARY_COLOR)
                ).append(text()
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
                        player.displayName().colorIfAbsent(SECONDARY_COLOR)
                ).append(text()
                        .content(" re-appeared.")
                        .color(PRIMARY_COLOR)
                ));
            }
            player.sendMessage(CHAT_PREFIX.append(text("You are no longer vanished.").color(PRIMARY_COLOR)));
            player.setVisibleByDefault(true);
        }
    }

    public static boolean toggleVanishSilent(Player player, boolean force){
        final boolean newStatus = !isVanished(player);
        VanishToggleEvent event = new VanishToggleEvent(player, newStatus);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() && !force){
            return false;
        }
        if (newStatus){
            vanishedPlayers.add(player);
            player.setVisibleByDefault(false);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("smod.vanish.see")){
                    onlinePlayer.showEntity(PLUGIN, player);
                }
            }
        } else {
            vanishedPlayers.remove(player);
            player.setVisibleByDefault(true);
        }
        return true;
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

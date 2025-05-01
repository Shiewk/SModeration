package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.shiewk.smoderation.paper.util.CommandUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

import static de.shiewk.smoderation.paper.SModerationPaper.*;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class EnderchestSeeCommand implements CommandProvider {

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return literal("enderchestsee")
                .requires(CommandUtil.requirePermission("smod.enderchestsee"))
                .then(argument("player", ArgumentTypes.player())
                        .executes(this::openEnderChest)
                )
                .build();
    }

    private int openEnderChest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player sender = CommandUtil.getExecutingPlayer(context.getSource());
        Player target = CommandUtil.getPlayerSingle(context, "player");
        sender.sendMessage(CHAT_PREFIX.append(
                Component.text("Opening ender chest of ").color(PRIMARY_COLOR)
                        .append(target.teamDisplayName().colorIfAbsent(SECONDARY_COLOR))
                        .append(Component.text("."))
        ));
        sender.openInventory(target.getEnderChest());
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public String getCommandDescription() {
        return "Views the ender chest of a player.";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("ecsee");
    }
}

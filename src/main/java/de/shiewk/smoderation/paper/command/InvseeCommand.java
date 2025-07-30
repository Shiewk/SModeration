package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.shiewk.smoderation.paper.inventory.InvSeeEquipmentInventory;
import de.shiewk.smoderation.paper.inventory.InvSeeInventory;
import de.shiewk.smoderation.paper.util.CommandUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static net.kyori.adventure.text.Component.translatable;

public final class InvseeCommand implements CommandProvider {

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return literal("invsee")
                .requires(CommandUtil.requirePermission("smod.invsee"))
                .then(argument("player", ArgumentTypes.player())
                        .executes(this::invseeInventory)
                        .then(literal("inventory")
                                .executes(this::invseeInventory)
                        )
                        .then(literal("armor")
                                .executes(this::invseeEquipment)
                        )
                        .then(literal("equipment")
                                .executes(this::invseeEquipment)
                        )
                )
                .build();
    }

    private int invseeInventory(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player sender = CommandUtil.getExecutingPlayer(context.getSource());
        Player target = CommandUtil.getPlayerSingle(context, "player");
        if (sender.equals(target)){
            CommandUtil.errorTranslatable("smod.command.invsee.fail.self");
        }
        sender.sendMessage(translatable("smod.command.invsee.opening", target.teamDisplayName()));
        new InvSeeInventory(sender, target).open();
        return Command.SINGLE_SUCCESS;
    }

    private int invseeEquipment(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player sender = CommandUtil.getExecutingPlayer(context.getSource());
        Player target = CommandUtil.getPlayerSingle(context, "player");
        if (sender.equals(target)){
            CommandUtil.errorTranslatable("smod.command.invsee.fail.self");
        }
        sender.sendMessage(translatable("smod.command.invsee.opening", target.teamDisplayName()));
        new InvSeeEquipmentInventory(sender, target).open();
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public String getCommandDescription() {
        return "Views the inventory of another player.";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("sinvsee", "smodinvsee", "invs");
    }
}

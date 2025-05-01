package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.command.argument.PlayerUUIDArgument;
import de.shiewk.smoderation.paper.punishments.Punishment;
import de.shiewk.smoderation.paper.punishments.PunishmentType;
import de.shiewk.smoderation.paper.util.CommandUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class UnbanCommand implements CommandProvider {

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return literal("unban")
                .requires(CommandUtil.requirePermission("smod.unban"))
                .then(argument("player", new PlayerUUIDArgument())
                        .executes(this::unbanPlayer)
                )
                .build();
    }

    private int unbanPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        UUID senderUUID = CommandUtil.getSenderUUID(context.getSource());
        UUID target = context.getArgument("player", UUID.class);
        final Punishment punishment = SModerationPaper.container.find(
                p -> p.to.equals(target) && p.isActive() && p.type == PunishmentType.BAN
        );
        if (punishment != null) {
            punishment.undo(senderUUID);
            punishment.broadcastUndo(SModerationPaper.container);
        } else {
            CommandUtil.error("This player is not banned.");
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    @Override
    public String getCommandDescription() {
        return "Unbans a banned player.";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("smodunban");
    }
}

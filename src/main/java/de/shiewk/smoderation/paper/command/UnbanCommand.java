package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.command.argument.PlayerUUIDArgument;
import de.shiewk.smoderation.paper.punishments.Ban;
import de.shiewk.smoderation.paper.punishments.Punishment;
import de.shiewk.smoderation.paper.punishments.PunishmentManager;
import de.shiewk.smoderation.paper.punishments.TimedPunishment;
import de.shiewk.smoderation.paper.util.CommandUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static net.kyori.adventure.text.Component.text;

@SuppressWarnings("UnstableApiUsage") // Paper Brigadier API
public final class UnbanCommand implements CommandProvider {

    private final PunishmentManager punishmentManager;

    public UnbanCommand(PunishmentManager punishmentManager) {
        this.punishmentManager = punishmentManager;
    }

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
        final List<Punishment> punishments = punishmentManager.byTargetUUID(
                target,
                p -> p instanceof Ban ban && ban.isActive()
        );
        for (Punishment punishment : punishments) {
            TimedPunishment timed = (TimedPunishment) punishment;
            timed.cancel(senderUUID);
            try {
                timed.updateSaveData();
            } catch (IOException e) {
                SModerationPaper.LOGGER.error("Failed to save punishment update", e);
                CommandUtil.error(text("Failed to save updates, please see server console"));
            }
        }
        if (punishments.isEmpty()) {
            CommandUtil.errorTranslatable("smod.command.unban.fail.notBanned");
        }
        return Command.SINGLE_SUCCESS;
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

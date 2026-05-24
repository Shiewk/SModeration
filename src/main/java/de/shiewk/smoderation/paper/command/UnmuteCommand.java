package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.command.argument.PlayerUUIDArgument;
import de.shiewk.smoderation.paper.punishments.Mute;
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
public final class UnmuteCommand implements CommandProvider {

    private final PunishmentManager punishmentManager;

    public UnmuteCommand(PunishmentManager punishmentManager) {
        this.punishmentManager = punishmentManager;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return literal("unmute")
                .requires(CommandUtil.requirePermission("smod.unmute"))
                .then(argument("player", new PlayerUUIDArgument())
                        .executes(this::unmutePlayer)
                )
                .build();
    }

    private int unmutePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        UUID senderUUID = CommandUtil.getSenderUUID(context.getSource());
        UUID target = context.getArgument("player", UUID.class);
        final List<Punishment> punishments = punishmentManager.byTargetUUID(
                target,
                p -> p instanceof Mute mute && mute.isActive()
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
            CommandUtil.errorTranslatable("smod.command.unmute.fail.notBanned");
        }
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public String getCommandDescription() {
        return "Unmutes a muted player.";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("smodunmute");
    }
}

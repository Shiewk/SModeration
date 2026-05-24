package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.command.argument.PlayerUUIDArgument;
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
public class CancelPunishmentCommand implements CommandProvider {

    private final PunishmentManager punishmentManager;
    private final String[] commandNames;
    private final String typeId;
    private final String permission;
    private final String description;

    public CancelPunishmentCommand(PunishmentManager punishmentManager, String[] commandNames, String typeId, String permission, String description) {
        this.punishmentManager = punishmentManager;
        this.commandNames = commandNames;
        this.typeId = typeId;
        this.permission = permission;
        this.description = description;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return literal(commandNames[0])
                .requires(CommandUtil.requirePermission(this.permission))
                .then(argument("player", new PlayerUUIDArgument())
                        .executes(this::cancelPunishment)
                )
                .build();
    }

    private int cancelPunishment(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        UUID senderUUID = CommandUtil.getSenderUUID(context.getSource());
        UUID target = context.getArgument("player", UUID.class);
        List<Punishment> punishments = punishmentManager.byTargetUUID(
                target,
                p -> p instanceof TimedPunishment t && t.getTypeId().equals(this.typeId) && t.isActive()
        );
        for (Punishment punishment : punishments) {
            TimedPunishment timed = (TimedPunishment) punishment;
            timed.cancel(senderUUID);
            try {
                timed.updateSaveData();
            } catch (IOException e){
                SModerationPaper.LOGGER.error("Failed to save punishment update", e);
                CommandUtil.error(text("Failed to save update, please see server console"));
            }
        }
        if (punishments.isEmpty()) {
            CommandUtil.errorTranslatable("smod.command.cancel.fail.notPunished");
        }
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public String getCommandDescription() {
        return this.description;
    }

    @Override
    public Collection<String> getAliases() {
        return List.of(this.commandNames);
    }
}

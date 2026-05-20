package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.shiewk.smoderation.paper.command.argument.PlayerUUIDArgument;
import de.shiewk.smoderation.paper.punishments.Punishment;
import de.shiewk.smoderation.paper.punishments.PunishmentManager;
import de.shiewk.smoderation.paper.punishments.TimedPunishment;
import de.shiewk.smoderation.paper.util.CommandUtil;
import de.shiewk.smoderation.paper.util.PlayerUtil;
import de.shiewk.smoderation.paper.util.TimeUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static net.kyori.adventure.text.Component.*;

@SuppressWarnings("UnstableApiUsage") // Paper Brigadier API
public final class ModLogsCommand implements CommandProvider {

    private final PunishmentManager punishmentManager;

    public ModLogsCommand(PunishmentManager punishmentManager) {
        this.punishmentManager = punishmentManager;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return literal("modlogs")
                .requires(CommandUtil.requirePermission("smod.logs"))
                .then(argument("player", new PlayerUUIDArgument())
                        .executes(this::showModLogs)
                        .then(literal("current")
                                .executes(this::showModLogs)
                        )
                        .then(literal("all")
                                .executes(this::showHistory)
                        )
                        .then(literal("history")
                                .executes(this::showHistory)
                        )
                )
                .build();
    }

    private int showHistory(CommandContext<CommandSourceStack> context){
        CommandSender sender = context.getSource().getSender();
        UUID uuid = context.getArgument("player", UUID.class);
        String name = PlayerUtil.offlinePlayerName(uuid);
        sender.sendMessage(translatable("smod.command.modlogs.history.heading", text(name), text(uuid.toString())));
        List<Punishment> punishments = new ObjectArrayList<>(punishmentManager.byTargetUUID(uuid));
        // Sort the punishments by issue time ascending
        punishments.sort(Comparator.comparingLong(Punishment::getTimestamp));
        if (punishments.isEmpty()){
            sender.sendMessage(translatable("smod.command.modlogs.history.empty"));
        }
        for (Punishment punishment : punishments) {
            sender.sendMessage(empty());
            // Type
            sender.sendMessage(translatable("smod.command.modlogs.history.type", translatable("smod.punishment.name." + punishment.getType())));
            // Timestamp
            sender.sendMessage(translatable("smod.command.modlogs.history.timestamp", TimeUtil.calendarTimestamp(punishment.getTimestamp())));
            // Issuer
            sender.sendMessage(translatable("smod.command.modlogs.history.issuer", text(PlayerUtil.offlinePlayerName(punishment.getIssuerID()))));
            if (punishment instanceof TimedPunishment timed) {
                // Duration
                sender.sendMessage(translatable("smod.command.modlogs.history.duration", TimeUtil.formatTimeLong(timed.getDuration())));
                // Also, expiry time
                sender.sendMessage(translatable("smod.command.modlogs.history.expiry", TimeUtil.calendarTimestamp(timed.getExpiry())));
                // Who cancelled it
                if (timed.wasCancelled()) {
                    sender.sendMessage(translatable("smod.command.modlogs.history.cancelled", text(PlayerUtil.offlinePlayerName(timed.getCancelledBy()))));
                }
            }
            // Reason
            sender.sendMessage(translatable("smod.command.modlogs.history.reason", text(punishment.getReason())));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int showModLogs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        UUID uuid = context.getArgument("player", UUID.class);
        String name = PlayerUtil.offlinePlayerName(uuid);
        sender.sendMessage(translatable("smod.command.modlogs.heading", text(name), text(uuid.toString())));
        List<Punishment> punishments = punishmentManager.byTargetUUID(uuid);
        for (Punishment punishment : punishments) {
            if (punishment instanceof TimedPunishment timed && timed.isActive()){
                if (timed.isPermanent()){
                    sender.sendMessage(translatable("smod.command.modlogs." + punishment.getType() + ".permanent", text(punishment.getReason())));
                } else {
                    sender.sendMessage(translatable("smod.command.modlogs." + punishment.getType(),
                            TimeUtil.calendarTimestamp(timed.getExpiry()),
                            TimeUtil.formatTimeLong(timed.getExpiry() - System.currentTimeMillis()),
                            text(punishment.getReason())
                    ));
                }
            }
        }
        if (punishments.isEmpty()){
            sender.sendMessage(translatable("smod.command.modlogs.none"));
        }
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public String getCommandDescription() {
        return "Views all current punishments of a player.";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("logs", "seen", "smodlogs");
    }
}

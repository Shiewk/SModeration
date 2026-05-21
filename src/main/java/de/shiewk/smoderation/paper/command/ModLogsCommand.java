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
                        .executes(this::showCurrent)
                        .then(literal("current")
                                .executes(this::showCurrent)
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

    private void printInformation(CommandSender target, List<Punishment> punishments) {
        for (Punishment punishment : punishments) {
            target.sendMessage(empty());
            // Type
            target.sendMessage(translatable("smod.command.modlogs.type", translatable("smod.punishment.name." + punishment.getType())));
            // Timestamp
            target.sendMessage(translatable("smod.command.modlogs.timestamp", TimeUtil.calendarTimestamp(punishment.getTimestamp())));
            // Issuer
            target.sendMessage(translatable("smod.command.modlogs.issuer", text(PlayerUtil.offlinePlayerName(punishment.getIssuerID()))));
            if (punishment instanceof TimedPunishment timed) {
                // Duration
                target.sendMessage(translatable("smod.command.modlogs.duration",
                        timed.isPermanent() ?
                                translatable("smod.time.permanent") :
                                TimeUtil.formatTimeLong(timed.getDuration())
                ));
                // Also, expiry time
                target.sendMessage(translatable("smod.command.modlogs.expiry",
                        timed.isPermanent() ?
                                translatable("smod.time.never") :
                                TimeUtil.calendarTimestamp(timed.getExpiry())
                ));
                // Who cancelled it
                if (timed.wasCancelled()) {
                    target.sendMessage(translatable("smod.command.modlogs.cancelled", text(PlayerUtil.offlinePlayerName(timed.getCancelledBy()))));
                }
            }
            // Reason
            target.sendMessage(translatable("smod.command.modlogs.reason", text(punishment.getReason())));
        }
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
        printInformation(sender, punishments);
        return Command.SINGLE_SUCCESS;
    }

    private int showCurrent(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        UUID uuid = context.getArgument("player", UUID.class);
        String name = PlayerUtil.offlinePlayerName(uuid);
        sender.sendMessage(translatable("smod.command.modlogs.heading", text(name), text(uuid.toString())));
        List<Punishment> punishments = punishmentManager.byTargetUUID(uuid)
                .stream()
                .filter(p -> p instanceof TimedPunishment timed && timed.isActive())
                .toList();
        printInformation(sender, punishments);
        if (punishments.isEmpty()) {
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

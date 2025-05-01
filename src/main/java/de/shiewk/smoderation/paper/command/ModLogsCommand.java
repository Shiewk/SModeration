package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.command.argument.PlayerUUIDArgument;
import de.shiewk.smoderation.paper.punishments.Punishment;
import de.shiewk.smoderation.paper.punishments.PunishmentType;
import de.shiewk.smoderation.paper.util.CommandUtil;
import de.shiewk.smoderation.paper.util.PlayerUtil;
import de.shiewk.smoderation.paper.util.TimeUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static de.shiewk.smoderation.paper.SModerationPaper.*;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class ModLogsCommand implements CommandProvider {

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return literal("modlogs")
                .requires(CommandUtil.requirePermission("smod.logs"))
                .then(argument("player", new PlayerUUIDArgument())
                        .executes(this::showModLogs)
                )
                .build();
    }

    private int showModLogs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        UUID uuid = context.getArgument("player", UUID.class);
        String name = PlayerUtil.offlinePlayerName(uuid);
        sender.sendMessage(CHAT_PREFIX.append(Component.text("Player ").color(PRIMARY_COLOR)
                .append(Component.text(name).color(SECONDARY_COLOR))
                .append(Component.text(" (%s)".formatted(uuid)).color(INACTIVE_COLOR))));
        final List<Punishment> punishments = SModerationPaper.container.findAll(p -> p.to.equals(uuid) && p.isActive());
        for (Punishment punishment : punishments) {
            sender.sendMessage(Component.text("- is currently ").color(PRIMARY_COLOR)
                    .append(Component.text(punishment.type == PunishmentType.BAN ? "banned" : "muted").color(SECONDARY_COLOR))
                    .append(Component.text(" until ").color(PRIMARY_COLOR))
                    .append(Component.text(TimeUtil.calendarTimestamp(punishment.until)).color(SECONDARY_COLOR))
                    .append(Component.text(" (in %s)".formatted(TimeUtil.formatTimeLong(punishment.until - System.currentTimeMillis()))).color(INACTIVE_COLOR))
                    .append(Component.text(". Reason: ").color(PRIMARY_COLOR))
                    .append(Component.text(punishment.reason).color(SECONDARY_COLOR)));
        }
        if (punishments.isEmpty()){
            sender.sendMessage(Component.text("- has no punishments.").color(PRIMARY_COLOR));
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

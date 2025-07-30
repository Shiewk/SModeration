package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.shiewk.smoderation.paper.command.argument.PlayerUUIDArgument;
import de.shiewk.smoderation.paper.punishments.Punishment;
import de.shiewk.smoderation.paper.util.CommandUtil;
import de.shiewk.smoderation.paper.util.PlayerUtil;
import de.shiewk.smoderation.paper.util.TimeUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static de.shiewk.smoderation.paper.SModerationPaper.container;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

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
        sender.sendMessage(translatable("smod.command.modlogs.heading", text(name), text(uuid.toString())));
        final List<Punishment> punishments = container.findAll(p -> p.to.equals(uuid) && p.isActive());
        for (Punishment punishment : punishments) {
            sender.sendMessage(translatable("smod.command.modlogs." + punishment.type.name().toLowerCase(),
                    TimeUtil.calendarTimestamp(punishment.until),
                    TimeUtil.formatTimeLong(punishment.until - System.currentTimeMillis()),
                    text(punishment.reason)
            ));
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

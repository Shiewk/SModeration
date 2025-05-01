package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.command.argument.DurationArgument;
import de.shiewk.smoderation.paper.command.argument.PlayerUUIDArgument;
import de.shiewk.smoderation.paper.punishments.Punishment;
import de.shiewk.smoderation.paper.util.CommandUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class MuteCommand implements CommandProvider {

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return literal("mute")
                .requires(CommandUtil.requirePermission("smod.mute"))
                .then(argument("player", new PlayerUUIDArgument())
                        .then(argument("duration", new DurationArgument())
                                .executes(this::muteWithoutReason)
                                .then(argument("reason", StringArgumentType.greedyString())
                                        .executes(this::muteWithReason)
                                )
                        )
                )
                .build();
    }

    private int muteWithoutReason(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        UUID sender = CommandUtil.getSenderUUID(context.getSource());
        UUID target = context.getArgument("player", UUID.class);
        long duration = context.getArgument("duration", Long.class);
        executeMute(sender, target, duration, Punishment.DEFAULT_REASON);
        return Command.SINGLE_SUCCESS;
    }

    private int muteWithReason(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        UUID sender = CommandUtil.getSenderUUID(context.getSource());
        UUID target = context.getArgument("player", UUID.class);
        long duration = context.getArgument("duration", Long.class);
        String reason = StringArgumentType.getString(context, "reason");
        executeMute(sender, target, duration, reason);
        return Command.SINGLE_SUCCESS;
    }

    public static void executeMute(UUID sender, UUID target, long duration, String reason) throws CommandSyntaxException {
        if (sender.equals(target)) {
            CommandUtil.error("You can't mute yourself.");
        } else {
            Player targetPlayer = Bukkit.getPlayer(target);
            if (targetPlayer != null && targetPlayer.hasPermission("smod.preventmute")){
                CommandUtil.error("This player can't be muted.");
            } else {
                final Punishment punishment = Punishment.mute(
                        System.currentTimeMillis(),
                        System.currentTimeMillis() + duration,
                        sender,
                        target,
                        reason
                );
                Punishment.issue(punishment, SModerationPaper.container);
            }
        }
    }

    @Override
    public String getCommandDescription() {
        return "Mutes a player for a customizable duration.";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("smodmute");
    }
}

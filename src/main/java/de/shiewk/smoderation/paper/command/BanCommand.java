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

public final class BanCommand implements CommandProvider {

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return literal("ban")
                .requires(CommandUtil.requirePermission("smod.ban"))
                .then(argument("player", new PlayerUUIDArgument())
                        .then(argument("duration", new DurationArgument())
                                .executes(this::banWithoutReason)
                                .then(argument("reason", StringArgumentType.greedyString())
                                        .executes(this::banWithReason)
                                )
                        )
                )
                .build();
    }

    private int banWithoutReason(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (SModerationPaper.CONFIG.shouldForceReason()){
            CommandUtil.errorTranslatable("smod.command.ban.fail.forceReason");
        }
        UUID sender = CommandUtil.getSenderUUID(context.getSource());
        UUID target = context.getArgument("player", UUID.class);
        long duration = context.getArgument("duration", Long.class);
        executeBan(sender, target, duration, Punishment.DEFAULT_REASON);
        return Command.SINGLE_SUCCESS;
    }

    private int banWithReason(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        UUID sender = CommandUtil.getSenderUUID(context.getSource());
        UUID target = context.getArgument("player", UUID.class);
        long duration = context.getArgument("duration", Long.class);
        String reason = StringArgumentType.getString(context, "reason");
        executeBan(sender, target, duration, reason);
        return Command.SINGLE_SUCCESS;
    }

    public static void executeBan(UUID sender, UUID target, long duration, String reason) throws CommandSyntaxException {
        Player targetPlayer = Bukkit.getPlayer(target);
        if (duration == 0){
            if (targetPlayer == null){
                CommandUtil.errorTranslatable("smod.command.ban.fail.tooShort");
            } else {
                KickCommand.executeKick(sender, targetPlayer, reason);
            }
            return;
        }
        if (sender.equals(target)) {
            CommandUtil.errorTranslatable("smod.command.ban.fail.self");
        } else {
            if (targetPlayer != null && targetPlayer.hasPermission("smod.preventban")){
                CommandUtil.errorTranslatable("smod.command.ban.fail.protect");
            } else {
                final Punishment punishment = Punishment.ban(
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
        return "Bans a player for a customizable duration.";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("smodban");
    }
}

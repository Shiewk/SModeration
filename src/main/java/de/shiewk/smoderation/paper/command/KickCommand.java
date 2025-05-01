package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.punishments.Punishment;
import de.shiewk.smoderation.paper.util.CommandUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public final class KickCommand implements CommandProvider {

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return literal("kick")
                .requires(CommandUtil.requirePermission("smod.kick"))
                .then(argument("player", ArgumentTypes.player())
                        .executes(this::kickWithoutReason)
                        .then(argument("reason", StringArgumentType.greedyString())
                                .executes(this::kickWithReason)
                        )
                )
                .build();
    }

    private int kickWithReason(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        UUID sender = CommandUtil.getSenderUUID(context.getSource());
        Player target = CommandUtil.getPlayerSingle(context, "player");
        String reason = StringArgumentType.getString(context, "reason");
        executeKick(sender, target, reason);
        return Command.SINGLE_SUCCESS;
    }

    private int kickWithoutReason(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        UUID sender = CommandUtil.getSenderUUID(context.getSource());
        Player target = CommandUtil.getPlayerSingle(context, "player");
        executeKick(sender, target, Punishment.DEFAULT_REASON);
        return Command.SINGLE_SUCCESS;
    }

    public static void executeKick(UUID sender, Player target, String reason) throws CommandSyntaxException {
        UUID targetId = target.getUniqueId();
        if (sender.equals(targetId)) {
            CommandUtil.error("You can't kick yourself.");
        } else if (target.hasPermission("smod.preventkick")){
            CommandUtil.error("This player can't be kicked.");
        }
        final Punishment punishment = Punishment.kick(
                System.currentTimeMillis(),
                sender,
                targetId,
                reason
        );
        Punishment.issue(punishment, SModerationPaper.container);
    }

    @Override
    public String getCommandDescription() {
        return "Kicks a player";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("smodkick");
    }
}

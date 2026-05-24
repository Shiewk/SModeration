package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.command.argument.PlayerUUIDArgument;
import de.shiewk.smoderation.paper.punishments.Punishment;
import de.shiewk.smoderation.paper.punishments.PunishmentManager;
import de.shiewk.smoderation.paper.util.CommandUtil;
import de.shiewk.smoderation.paper.util.PlayerUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

@SuppressWarnings("UnstableApiUsage") // Paper Brigadier API
public class UntimedPunishmentCommand implements CommandProvider {

    public interface PunishmentFactory {

        Punishment create(PunishmentManager manager, UUID issuer, UUID target, String reason);

    }

    private final PunishmentManager manager;
    private final String[] names;
    private final String permission;
    private final String protectionPermission;
    private final String description;
    private final boolean requireOnline;
    private final PunishmentFactory factory;

    public UntimedPunishmentCommand(PunishmentManager manager, String[] names, String permission, String protectionPermission, String description, boolean requireOnline, PunishmentFactory factory) {
        this.manager = manager;
        this.names = names;
        this.permission = permission;
        this.protectionPermission = protectionPermission;
        this.description = description;
        this.requireOnline = requireOnline;
        this.factory = factory;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return literal(this.names[0])
                .requires(CommandUtil.requirePermission(this.permission))
                .then(argument("player", new PlayerUUIDArgument())
                        .executes(this::punishWithoutReason)
                        .then(argument("reason", StringArgumentType.greedyString())
                                .executes(this::punishWithReason)
                        )
                )
                .build();
    }

    @Override
    public String getCommandDescription() {
        return this.description;
    }

    @Override
    public Collection<String> getAliases() {
        return List.of(this.names);
    }

    private int punishWithoutReason(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (SModerationPaper.config().getBoolean("force-reason", false)){
            CommandUtil.errorTranslatable("smod.command.punish.fail.forceReason");
        }
        UUID sender = CommandUtil.getSenderUUID(context.getSource());
        UUID target = context.getArgument("player", UUID.class);
        punish(sender, target, SModerationPaper.config().getString("default-reason", "No reason provided."));
        return Command.SINGLE_SUCCESS;
    }

    private int punishWithReason(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        UUID sender = CommandUtil.getSenderUUID(context.getSource());
        UUID target = context.getArgument("player", UUID.class);
        String reason = StringArgumentType.getString(context, "reason");
        punish(sender, target, reason);
        return Command.SINGLE_SUCCESS;
    }

    private void punish(UUID sender, UUID target, String reason) throws CommandSyntaxException {
        if (sender.equals(target)) {
            CommandUtil.errorTranslatable("smod.command.generic.fail.self");
        }
        CommandSender targetSender = PlayerUtil.senderByUUID(target);
        if (targetSender != null) {
            if (targetSender.hasPermission(this.protectionPermission)) {
                CommandUtil.errorTranslatable("smod.command.generic.fail.protect");
            }
        }
        if (requireOnline && Bukkit.getPlayer(target) == null) {
            CommandUtil.errorTranslatable("smod.command.generic.fail.notOnline");
        }
        Punishment punishment = factory.create(manager, sender, target, reason);
        manager.tryIssue(punishment);
    }

}

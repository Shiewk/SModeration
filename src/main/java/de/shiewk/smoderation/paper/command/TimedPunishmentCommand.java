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
import de.shiewk.smoderation.paper.punishments.PunishmentManager;
import de.shiewk.smoderation.paper.punishments.PunishmentType;
import de.shiewk.smoderation.paper.punishments.TimedPunishment;
import de.shiewk.smoderation.paper.util.CommandUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

@SuppressWarnings("UnstableApiUsage") // Paper Brigadier API
public class TimedPunishmentCommand implements CommandProvider {

    public interface PunishmentFactory {

        Punishment create(PunishmentManager manager, UUID issuer, UUID target, long duration, String reason);

    }

    private final PunishmentManager manager;
    private final String[] names;
    private final PunishmentType<?> type;
    private final String description;
    private final boolean requireOnline;
    private final PunishmentFactory factory;

    public TimedPunishmentCommand(PunishmentManager manager, String[] names, PunishmentType<?> type, String description, boolean requireOnline, PunishmentFactory factory) {
        this.manager = manager;
        this.names = names;
        this.type = type;
        this.description = description;
        this.requireOnline = requireOnline;
        this.factory = factory;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return literal(this.names[0])
                .requires(CommandUtil.requirePermission(this.type.getPermission()))
                .then(argument("player", new PlayerUUIDArgument())
                        .then(argument("duration", new DurationArgument())
                                .executes(this::punishWithoutReason)
                                .then(argument("reason", StringArgumentType.greedyString())
                                        .executes(this::punishWithReason)
                                )
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
            CommandUtil.errorTranslatable("smod.command.generic.fail.forceReason");
        }
        UUID sender = CommandUtil.getSenderUUID(context.getSource());
        UUID target = context.getArgument("player", UUID.class);
        long duration = context.getArgument("duration", Long.class);
        punish(sender, target, duration, SModerationPaper.config().getString("default-reason", "No reason provided."));
        return Command.SINGLE_SUCCESS;
    }

    private int punishWithReason(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        UUID sender = CommandUtil.getSenderUUID(context.getSource());
        UUID target = context.getArgument("player", UUID.class);
        long duration = context.getArgument("duration", Long.class);
        String reason = StringArgumentType.getString(context, "reason");
        punish(sender, target, duration, reason);
        return Command.SINGLE_SUCCESS;
    }

    public void punish(UUID sender, UUID target, long duration, String reason) throws CommandSyntaxException {
        UntimedPunishmentCommand.checkCommonConditions(sender, target, this.type, requireOnline);
        if (duration == 0){
            CommandUtil.errorTranslatable("smod.command.generic.fail.tooShort");
        }
        if (!manager.byTargetUUID(target, p -> p instanceof TimedPunishment t && p.getTypeId().equals(type.getTypeId()) && t.isActive()).isEmpty()) {
            CommandUtil.errorTranslatable("smod.command.generic.fail.alreadyActive");
        }
        Punishment punishment = factory.create(manager, sender, target, duration, reason);
        manager.tryIssue(punishment);
    }

}

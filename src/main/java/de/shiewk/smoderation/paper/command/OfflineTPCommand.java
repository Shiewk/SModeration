package de.shiewk.smoderation.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.shiewk.smoderation.paper.command.argument.OfflinePlayerArgument;
import de.shiewk.smoderation.paper.util.CommandUtil;
import de.shiewk.smoderation.paper.util.PlayerUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Collection;
import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public final class OfflineTPCommand implements CommandProvider {

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return literal("offlinetp")
            .requires(CommandUtil.requirePermission("smod.offlinetp"))
            .then(argument("player", new OfflinePlayerArgument())
                    .executes(this::offlineTeleport)
            )
            .build();
    }

    private int offlineTeleport(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player sender = CommandUtil.getExecutingPlayer(context.getSource());
        OfflinePlayer player = context.getArgument("player", OfflinePlayer.class);

        Location location = player.getLocation();

        if (location == null) {
            CommandUtil.errorTranslatable("smod.command.offlinetp.fail.unknown");
        }

        sender.teleportAsync(location, PlayerTeleportEvent.TeleportCause.COMMAND);
        sender.sendMessage(translatable("smod.command.offlinetp.teleporting", text(PlayerUtil.offlinePlayerName(player.getUniqueId()))));

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public String getCommandDescription() {
        return "Teleports you to an offline player.";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("smodofflinetp");
    }
}

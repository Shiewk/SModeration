package de.shiewk.smoderation.paper.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static de.shiewk.smoderation.paper.util.PlayerUtil.UUID_CONSOLE;
import static net.kyori.adventure.text.Component.translatable;

public final class CommandUtil {
    private CommandUtil(){}

    public static Predicate<CommandSourceStack> requirePermission(String permission) {
        return stack -> stack.getSender().hasPermission(permission);
    }

    public static Player getExecutingPlayer(CommandSourceStack stack) throws CommandSyntaxException {
        CommandSender sender = stack.getSender();
        if (sender instanceof Player player) {
            return player;
        } else {
            errorTranslatable("smod.command.fail.players");
            throw new UnknownError(); // can't happen
        }
    }

    public static UUID getSenderUUID(CommandSourceStack stack) throws CommandSyntaxException {
        CommandSender sender = stack.getSender();
        if (sender instanceof Player player) {
            return player.getUniqueId();
        } else if (sender instanceof ConsoleCommandSender){
            return UUID_CONSOLE;
        } else {
            errorTranslatable("smod.command.fail.playersConsole");
            throw new UnknownError(); // can't happen
        }
    }

    public static Player getPlayerSingle(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        @NotNull List<Player> players = context.getArgument(name, PlayerSelectorArgumentResolver.class).resolve(context.getSource());
        if (players.isEmpty()){
            errorTranslatable("smod.command.fail.invalidPlayer");
        }
        return players.getFirst();
    }

    public static void error(Component message) throws CommandSyntaxException {
        throw new CommandSyntaxException(
                new SimpleCommandExceptionType(null),
                MessageComponentSerializer.message().serialize(message)
        );
    }

    public static void errorTranslatable(String key) throws CommandSyntaxException {
        error(translatable(key));
    }

    public static void errorTranslatable(String key, Component...args) throws CommandSyntaxException {
        error(translatable(key, args));
    }

    public static void errorTranslatable(String key, String...args) throws CommandSyntaxException {
        errorTranslatable(key, Arrays.stream(args).map(Component::text).toArray(Component[]::new));
    }
}

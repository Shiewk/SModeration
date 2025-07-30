package de.shiewk.smoderation.paper.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.shiewk.smoderation.paper.util.CommandUtil;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PlayerUUIDArgument implements CustomArgumentType.Converted<UUID, String> {

    @Override
    public @NotNull UUID convert(@NotNull String nativeType) throws CommandSyntaxException {
        try {
            return UUID.fromString(nativeType);
        } catch (IllegalArgumentException e) {
            OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(nativeType);
            if (player != null){
                return player.getUniqueId();
            } else {
                CommandUtil.errorTranslatable("smod.argument.uuid.fail.notCached");
                throw new UnknownError(); // can't happen
            }
        }
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public @NotNull <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        Bukkit.getOnlinePlayers()
                .stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

}

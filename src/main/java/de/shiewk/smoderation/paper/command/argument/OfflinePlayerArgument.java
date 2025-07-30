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
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class OfflinePlayerArgument implements CustomArgumentType.Converted<OfflinePlayer, String> {

    @Override
    public OfflinePlayer convert(@NotNull String nativeType) throws CommandSyntaxException {
        OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(nativeType);
        if (player != null){
            return player;
        } else {
            CommandUtil.errorTranslatable("smod.argument.offlinePlayer.fail.notCached");
            throw new AssertionError(); // can't happen
        }
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public @NotNull <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        return builder.buildFuture();
    }

}

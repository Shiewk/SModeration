package de.shiewk.smoderation.paper.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.shiewk.smoderation.paper.util.CommandUtil;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public final class DurationArgument implements CustomArgumentType.Converted<Long, String> {

    public static final Pattern DURATION_PATTERN = Pattern.compile("([0-9]{1,9})(ms|s|min|h|d|w|mo|y)");
    public static final Pattern VALIDATION_PATTERN = Pattern.compile("(([0-9]{1,9})(ms|s|min|h|d|w|mo|y))+");

    @Override
    public @NotNull Long convert(@NotNull String nativeType) throws CommandSyntaxException {
        if (!VALIDATION_PATTERN.matcher(nativeType).matches()){
            CommandUtil.errorTranslatable("smod.argument.duration.fail.pattern");
        }
        AtomicLong totalDuration = new AtomicLong();
        for (MatchResult result : DURATION_PATTERN.matcher(nativeType).results().toList()) {
            long amount = Long.parseLong(result.group(1));
            long timeSpan = switch (result.group(2)) {
                case "ms" -> 1;
                case "s" -> 1000;
                case "min" -> 60_000;
                case "h" -> 3600_000;
                case "d" -> 86400_000;
                case "w" -> 604800_000;
                case "mo" -> 2_592_000_000L;
                case "y" -> 31_536_000_000L;
                default -> {
                    CommandUtil.errorTranslatable("smod.argument.duration.fail.invalid", result.group(2));
                    throw new UnknownError(); // can't happen
                }
            };
            totalDuration.addAndGet(amount*timeSpan);
        }
        return totalDuration.get();
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public @NotNull <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        if (builder.getRemaining().isBlank()){
            List.of(
                    "100ms",
                    "15s",
                    "2min",
                    "3h",
                    "7d",
                    "1w",
                    "3mo",
                    "1y",
                    "1mo15d",
                    "2h30min"
            ).forEach(builder::suggest);
        }
        return builder.buildFuture();
    }
}

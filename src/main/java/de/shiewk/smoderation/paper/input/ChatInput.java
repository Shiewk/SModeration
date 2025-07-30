package de.shiewk.smoderation.paper.input;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static de.shiewk.smoderation.paper.SModerationPaper.CHAT_PREFIX;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public class ChatInput {
    private final Player player;
    private final Component prompt;
    private final Consumer<Component> action;
    private int remainingTicks;

    private ChatInput(@NotNull Player player, @NotNull Component prompt, @NotNull Consumer<Component> action, int remainingSeconds){
        Objects.requireNonNull(action);
        Objects.requireNonNull(prompt);
        Objects.requireNonNull(player);
        this.player = player;
        this.prompt = prompt;
        this.action = action;
        this.remainingTicks = remainingSeconds * 20 + 1;
    }

    public static void tickAll() {
        runningInputs.values().forEach(ChatInput::tick);
    }

    void tick(){
        remainingTicks--;
        if (remainingTicks <= 0){
            runningInputs.remove(player);
            return;
        }
        if (remainingTicks % 20 == 0){
            player.showTitle(Title.title(
                    translatable("smod.chatInput.remainingTime", text(getRemainingTicks() / 20)),
                    getPrompt(),
                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ZERO)
            ));
        }
    }

    final static ConcurrentHashMap<Player, ChatInput> runningInputs = new ConcurrentHashMap<>();

    public static void prompt(Player player, Consumer<Component> consumer, Component prompt, int timeSeconds){
        runningInputs.put(player, new ChatInput(player, prompt, consumer, timeSeconds));
        player.sendMessage(CHAT_PREFIX.append(prompt));
    }

    public Component getPrompt() {
        return prompt;
    }

    public Consumer<Component> getAction() {
        return action;
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }
}

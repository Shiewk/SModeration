package de.shiewk.smoderation.command;

import de.shiewk.smoderation.listener.SocialSpyListener;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static de.shiewk.smoderation.SModeration.CHAT_PREFIX;
import static net.kyori.adventure.text.Component.text;

public class SocialSpyCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final boolean enabled = SocialSpyListener.toggle(sender);
        sender.sendMessage(CHAT_PREFIX.append(text("SocialSpy ").append(
                        enabled ?
                                text("enabled").color(NamedTextColor.GREEN) :
                                text("disabled").color(NamedTextColor.RED)
        ).append(text("."))));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

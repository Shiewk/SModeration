package de.shiewk.smoderation.command;

import de.shiewk.smoderation.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static de.shiewk.smoderation.SModeration.*;

public class EnderchestSeeCommand implements TabExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            return false;
        }
        if (sender instanceof HumanEntity human){
            final Player player = PlayerUtil.findOnlinePlayer(args[0]);
            if (player != null) {
                if (human.getUniqueId().equals(player.getUniqueId())){
                    human.sendMessage(Component.text("You can't open your own ender chest.").color(FAIL_COLOR));
                } else {
                    human.sendMessage(CHAT_PREFIX.append(
                            Component.text("Opening ender chest of ").color(PRIMARY_COLOR)
                                    .append(Component.text(player.getName()).color(SECONDARY_COLOR))
                                    .append(Component.text("."))
                    ));
                    human.openInventory(player.getEnderChest());
                }
            } else {
                human.sendMessage(Component.text("This player is not online.").color(FAIL_COLOR));
            }
        } else {
            sender.sendMessage(Component.text("Only an entity that can open inventories can execute this command!").color(FAIL_COLOR));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1){
            return List.of();
        }
        List<String> available = new ArrayList<>();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            available.add(onlinePlayer.getName());
        }
        List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(args.length > 0 ? args[0] : "", available, completions);
        return completions;
    }
}

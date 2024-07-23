package de.shiewk.smoderation.command;

import de.shiewk.smoderation.SModeration;
import de.shiewk.smoderation.util.PlayerUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VanishCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = null;
        if (args.length > 0){
            player = PlayerUtil.findOnlinePlayer(args[0]);
        } else if (sender instanceof Player){
            player = (Player) sender;
        }
        if (player != null){
            SModeration.toggleVanish(player);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2){
            return PlayerUtil.listPlayerNames();
        }
        return List.of();
    }
}

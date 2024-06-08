package de.shiewk.smoderation.command;

import de.shiewk.smoderation.SModeration;
import de.shiewk.smoderation.punishments.Punishment;
import de.shiewk.smoderation.punishments.PunishmentType;
import de.shiewk.smoderation.util.PlayerUtil;
import de.shiewk.smoderation.util.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static de.shiewk.smoderation.SModeration.*;

public class ModLogsCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1){
            return false;
        } else {
            String playername = args[0];
            String name;
            UUID uuid;
            try {
                uuid = UUID.fromString(playername);
            } catch (IllegalArgumentException ignored){
                uuid = PlayerUtil.offlinePlayerUUIDByName(playername);
            }
            if (uuid == null){
                sender.sendMessage(Component.text("This player was not found. Try running /%s with an UUID instead.".formatted(label)).color(NamedTextColor.RED));
                return true;
            }
            name = PlayerUtil.offlinePlayerName(uuid);
            sender.sendMessage(CHAT_PREFIX.append(Component.text("Player ").color(PRIMARY_COLOR)
                    .append(Component.text(name).color(SECONDARY_COLOR))
                    .append(Component.text(" (%s)".formatted(uuid)).color(INACTIVE_COLOR))));
            UUID finalUuid = uuid;
            final List<Punishment> punishments = SModeration.container.findAll(p -> p.to.equals(finalUuid) && p.isActive());
            for (Punishment punishment : punishments) {
                sender.sendMessage(Component.text("- is currently ").color(PRIMARY_COLOR)
                        .append(Component.text(punishment.type == PunishmentType.BAN ? "banned" : "muted").color(SECONDARY_COLOR))
                        .append(Component.text(" until ").color(PRIMARY_COLOR))
                        .append(Component.text(TimeUtil.calendarTimestamp(punishment.until)).color(SECONDARY_COLOR))
                        .append(Component.text(" (in %s)".formatted(TimeUtil.formatTimeLong(punishment.until - System.currentTimeMillis()))).color(INACTIVE_COLOR))
                        .append(Component.text(". Reason: ").color(PRIMARY_COLOR))
                        .append(Component.text(punishment.reason).color(SECONDARY_COLOR)));
            }
            if (punishments.isEmpty()){
                sender.sendMessage(Component.text("- has no punishments.").color(PRIMARY_COLOR));
            }
            return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1){
            return List.of();
        }
        String search = args.length > 0 ? args[0] : "";
        List<String> playernames = new ArrayList<>();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            playernames.add(onlinePlayer.getName());
        }
        List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(search, playernames, completions);
        return completions;
    }
}

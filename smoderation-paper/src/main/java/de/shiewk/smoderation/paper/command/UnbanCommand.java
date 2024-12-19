package de.shiewk.smoderation.paper.command;

import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.punishments.Punishment;
import de.shiewk.smoderation.paper.punishments.PunishmentType;
import de.shiewk.smoderation.paper.util.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class UnbanCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1){
            return false;
        } else {

            UUID senderUUID;
            if (sender instanceof ConsoleCommandSender){
                senderUUID = PlayerUtil.UUID_CONSOLE;
            } else if (sender instanceof Player pl){
                senderUUID = pl.getUniqueId();
            } else if (sender instanceof BlockCommandSender){
                sender.sendMessage(Component.text("Blocks can't execute this command.").color(NamedTextColor.RED));
                return true;
            } else {
                sender.sendMessage(Component.text("Your command sender type is unknown (%s).".formatted(sender.getClass().getName())).color(NamedTextColor.RED));
                return true;
            }

            String nameArg = args[0];
            UUID uuid;
            try {
                uuid = UUID.fromString(nameArg);
            } catch (IllegalArgumentException ignored){
                uuid = PlayerUtil.offlinePlayerUUIDByName(nameArg);
            }
            if (uuid == null){
                sender.sendMessage(Component.text("This player was not found. Try running /%s with an UUID instead.".formatted(label)).color(NamedTextColor.RED));
                return true;
            }
            UUID finalUuid = uuid;
            final Punishment punishment = SModerationPaper.container.find(p -> p.to.equals(finalUuid) && p.isActive() && p.type == PunishmentType.BAN);
            if (punishment != null) {
                punishment.undo(senderUUID);
                punishment.broadcastUndo(SModerationPaper.container);
            } else {
                sender.sendMessage(Component.text("This player is not banned.").color(NamedTextColor.RED));
            }
            return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}

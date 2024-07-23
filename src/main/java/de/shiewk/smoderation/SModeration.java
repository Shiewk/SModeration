package de.shiewk.smoderation;

import de.shiewk.smoderation.command.*;
import de.shiewk.smoderation.event.CustomInventoryEvents;
import de.shiewk.smoderation.event.EnderchestSeeEvents;
import de.shiewk.smoderation.event.InvSeeEvents;
import de.shiewk.smoderation.listener.PunishmentListener;
import de.shiewk.smoderation.listener.VanishListener;
import de.shiewk.smoderation.storage.PunishmentContainer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import static net.kyori.adventure.text.Component.text;
import static org.bukkit.Bukkit.getPluginManager;

public final class SModeration extends JavaPlugin {

    public static final PunishmentContainer container = new PunishmentContainer();
    public static ComponentLogger LOGGER = null;
    public static SModeration PLUGIN = null;
    public static File SAVE_FILE = null;

    public static final TextColor PRIMARY_COLOR = TextColor.color(212, 0, 255);
    public static final TextColor SECONDARY_COLOR = TextColor.color(52, 143, 255);
    public static final TextColor INACTIVE_COLOR = NamedTextColor.GRAY;
    public static final TextColor FAIL_COLOR = NamedTextColor.RED;
    public static final TextComponent CHAT_PREFIX = text("SM \u00BB ").color(PRIMARY_COLOR);

    @Override
    public void onLoad() {
        LOGGER = getComponentLogger();
        PLUGIN = this;
        SAVE_FILE = new File(this.getDataFolder().getAbsolutePath() + "/container.gz");
    }

    @Override
    public void onEnable() {
        getPluginManager().registerEvents(new PunishmentListener(), this);
        getPluginManager().registerEvents(new CustomInventoryEvents(), this);
        getPluginManager().registerEvents(new InvSeeEvents(), this);
        getPluginManager().registerEvents(new EnderchestSeeEvents(), this);
        getPluginManager().registerEvents(new VanishListener(), this);

        registerCommand("mute", new MuteCommand());
        registerCommand("ban", new BanCommand());
        registerCommand("kick", new KickCommand());
        registerCommand("smod", new SModCommand());
        registerCommand("modlogs", new ModLogsCommand());
        registerCommand("unmute", new UnmuteCommand());
        registerCommand("unban", new UnbanCommand());
        registerCommand("invsee", new InvseeCommand());
        registerCommand("enderchestsee", new EnderchestSeeCommand());
        registerCommand("vanish", new VanishCommand());

        container.load(SAVE_FILE);
    }

    private void registerCommand(String label, TabExecutor executor){
        final PluginCommand command = getCommand(label);
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        } else {
            LOGGER.warn("Command %s failed to register: This command does not exist".formatted(label));
        }
    }

    @Override
    public void onDisable() {
        SModeration.container.save(SModeration.SAVE_FILE);
        for (Player player : Bukkit.getOnlinePlayers()) {
            // in case players are still vanished when the server shuts down
            if (isVanished(player)){
                toggleVanish(player);
            }
        }
    }

    private static final ObjectArrayList<Player> vanishedPlayers = new ObjectArrayList<>();

    public static void toggleVanish(Player player){
        final boolean newStatus = !isVanished(player);
        if (newStatus){
            vanishedPlayers.add(player);
            for (CommandSender sender : container.collectBroadcastTargets()) {
                sender.sendMessage(CHAT_PREFIX.append(
                        player.displayName()
                                .colorIfAbsent(SECONDARY_COLOR)
                ).append(
                        text()
                                .content(" vanished.")
                                .color(PRIMARY_COLOR)
                ));
            }
            player.sendMessage(CHAT_PREFIX.append(text("You are now vanished.").color(PRIMARY_COLOR)));
            player.setVisibleByDefault(false);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("smod.vanish.see")){
                    onlinePlayer.showEntity(PLUGIN, player);
                }
            }
        } else {
            vanishedPlayers.remove(player);
            for (CommandSender sender : container.collectBroadcastTargets()) {
                sender.sendMessage(CHAT_PREFIX.append(
                        player.displayName()
                                .colorIfAbsent(SECONDARY_COLOR)
                ).append(
                        text()
                                .content(" re-appeared.")
                                .color(PRIMARY_COLOR)
                ));
            }
            player.sendMessage(CHAT_PREFIX.append(text("You are no longer vanished.").color(PRIMARY_COLOR)));
            player.setVisibleByDefault(true);
        }
    }

    public static boolean isVanished(Player player){
        return vanishedPlayers.contains(player);
    }

    public static ObjectArrayList<Player> getVanishedPlayers() {
        return vanishedPlayers.clone();
    }

    private static final ObjectArrayList<Player> vanishedPlayers = new ObjectArrayList<>();

    public static void toggleVanish(Player player){
        final boolean newStatus = !isVanished(player);
        if (newStatus){
            vanishedPlayers.add(player);
            for (CommandSender sender : container.collectBroadcastTargets()) {
                sender.sendMessage(CHAT_PREFIX.append(
                        player.displayName()
                                .colorIfAbsent(SECONDARY_COLOR)
                ).append(
                        text()
                                .content(" vanished.")
                                .color(PRIMARY_COLOR)
                ));
            }
            player.sendMessage(CHAT_PREFIX.append(text("You are now vanished.").color(PRIMARY_COLOR)));
            player.setVisibleByDefault(false);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("smod.vanish.see")){
                    onlinePlayer.showEntity(PLUGIN, player);
                }
            }
        } else {
            vanishedPlayers.remove(player);
            for (CommandSender sender : container.collectBroadcastTargets()) {
                sender.sendMessage(CHAT_PREFIX.append(
                        player.displayName()
                                .colorIfAbsent(SECONDARY_COLOR)
                ).append(
                        text()
                                .content(" re-appeared.")
                                .color(PRIMARY_COLOR)
                ));
            }
            player.sendMessage(CHAT_PREFIX.append(text("You are no longer vanished.").color(PRIMARY_COLOR)));
            player.setVisibleByDefault(true);
        }
    }

    public static boolean isVanished(Player player){
        return vanishedPlayers.contains(player);
    }

    public static ObjectArrayList<Player> getVanishedPlayers() {
        return vanishedPlayers.clone();
    }
}

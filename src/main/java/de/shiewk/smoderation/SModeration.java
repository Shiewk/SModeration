package de.shiewk.smoderation;

import de.shiewk.smoderation.command.*;
import de.shiewk.smoderation.event.CustomInventoryEvents;
import de.shiewk.smoderation.event.EnderchestSeeEvents;
import de.shiewk.smoderation.event.InvSeeEvents;
import de.shiewk.smoderation.listener.PunishmentListener;
import de.shiewk.smoderation.storage.PunishmentContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

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
    public static final TextComponent CHAT_PREFIX = Component.text("SM \u00BB ").color(PRIMARY_COLOR);

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

        registerCommand("mute", new MuteCommand());
        registerCommand("ban", new BanCommand());
        registerCommand("kick", new KickCommand());
        registerCommand("smod", new SModCommand());
        registerCommand("modlogs", new ModLogsCommand());
        registerCommand("unmute", new UnmuteCommand());
        registerCommand("unban", new UnbanCommand());
        registerCommand("invsee", new InvseeCommand());
        registerCommand("enderchestsee", new EnderchestSeeCommand());

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
    }
}

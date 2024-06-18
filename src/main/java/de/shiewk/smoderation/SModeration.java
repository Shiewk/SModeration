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

        final PluginCommand mute = getCommand("mute");
        assert mute != null;
        mute.setExecutor(new MuteCommand());
        mute.setTabCompleter(new MuteCommand());

        final PluginCommand ban = getCommand("ban");
        assert ban != null;
        ban.setExecutor(new BanCommand());
        ban.setTabCompleter(new BanCommand());

        final PluginCommand kick = getCommand("kick");
        assert kick != null;
        kick.setExecutor(new KickCommand());
        kick.setTabCompleter(new KickCommand());

        final PluginCommand smod = getCommand("smod");
        assert smod != null;
        smod.setExecutor(new SModCommand());
        smod.setTabCompleter(new SModCommand());

        final PluginCommand logs = getCommand("modlogs");
        assert logs != null;
        logs.setExecutor(new ModLogsCommand());
        logs.setTabCompleter(new ModLogsCommand());

        final PluginCommand unmute = getCommand("unmute");
        assert unmute != null;
        unmute.setExecutor(new UnmuteCommand());
        unmute.setTabCompleter(new UnmuteCommand());

        final PluginCommand unban = getCommand("unban");
        assert unban != null;
        unban.setExecutor(new UnbanCommand());
        unban.setTabCompleter(new UnbanCommand());

        final PluginCommand invsee = getCommand("invsee");
        assert invsee != null;
        invsee.setExecutor(new InvseeCommand());
        invsee.setTabCompleter(new InvseeCommand());

        final PluginCommand ecsee = getCommand("enderchestsee");
        assert ecsee != null;
        ecsee.setExecutor(new EnderchestSeeCommand());
        ecsee.setTabCompleter(new EnderchestSeeCommand());

        container.load(SAVE_FILE);
    }

    @Override
    public void onDisable() {
        SModeration.container.save(SModeration.SAVE_FILE);
    }
}

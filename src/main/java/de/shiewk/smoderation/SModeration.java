package de.shiewk.smoderation;

import de.shiewk.smoderation.command.BanCommand;
import de.shiewk.smoderation.command.KickCommand;
import de.shiewk.smoderation.command.MuteCommand;
import de.shiewk.smoderation.event.CustomInventoryEvents;
import de.shiewk.smoderation.listener.PunishmentListener;
import de.shiewk.smoderation.storage.PunishmentContainer;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.Bukkit.getPluginManager;

public final class SModeration extends JavaPlugin {

    public static final PunishmentContainer container = new PunishmentContainer();
    public static SModeration PLUGIN = null;

    @Override
    public void onLoad() {
        PLUGIN = this;
    }

    @Override
    public void onEnable() {
        getPluginManager().registerEvents(new PunishmentListener(), this);
        getPluginManager().registerEvents(new CustomInventoryEvents(), this);

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
    }

    @Override
    public void onDisable() {

    }
}

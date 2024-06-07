package de.shiewk.smoderation;

import de.shiewk.smoderation.listener.PunishmentListener;
import de.shiewk.smoderation.storage.PunishmentContainer;
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
    }

    @Override
    public void onDisable() {

    }
}

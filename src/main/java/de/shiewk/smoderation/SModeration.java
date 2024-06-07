package de.shiewk.smoderation;

import de.shiewk.smoderation.storage.PunishmentContainer;
import org.bukkit.plugin.java.JavaPlugin;

public final class SModeration extends JavaPlugin {

    public static final PunishmentContainer container = new PunishmentContainer();

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

package de.shiewk.smoderation.paper.listener;

import de.shiewk.smoderation.paper.punishments.PunishmentManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static de.shiewk.smoderation.paper.SModerationPaper.LOGGER;
import static net.kyori.adventure.text.Component.translatable;

public class CacheListener implements Listener {

    private final PunishmentManager punishmentManager;

    public CacheListener(PunishmentManager punishmentManager) {
        this.punishmentManager = punishmentManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        try {
            punishmentManager.loadToCache(event.getPlayer().getUniqueId());
        } catch (Exception e) {
            LOGGER.error("Failed to load punishments", e);
            event.getPlayer().kick(translatable("mco.errorMessage.connectionFailure"));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        punishmentManager.removeFromCache(event.getPlayer().getUniqueId());
    }

}

package de.shiewk.smoderation.paper.listener;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import de.shiewk.smoderation.paper.punishments.Punishment;
import de.shiewk.smoderation.paper.punishments.PunishmentManager;
import de.shiewk.smoderation.paper.punishments.custom.TimedCustomPunishment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public final class CustomPunishmentEffectListener implements Listener {

    private final PunishmentManager manager;

    public CustomPunishmentEffectListener(PunishmentManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event){
        for (Punishment punishment : manager.byTargetUUID(event.getPlayer().getUniqueId(), p -> p instanceof TimedCustomPunishment c && c.isActive())) {
            TimedCustomPunishment custom = (TimedCustomPunishment) punishment;
            custom.applyEffects("join", custom.getCustomMetadata().effects().join());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPostRespawn(PlayerPostRespawnEvent event){
        for (Punishment punishment : manager.byTargetUUID(event.getPlayer().getUniqueId(), p -> p instanceof TimedCustomPunishment c && c.isActive())) {
            TimedCustomPunishment custom = (TimedCustomPunishment) punishment;
            custom.applyEffects("respawn", custom.getCustomMetadata().effects().respawn());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event){
        for (Punishment punishment : manager.byTargetUUID(event.getPlayer().getUniqueId(), p -> p instanceof TimedCustomPunishment c && c.isActive())) {
            TimedCustomPunishment custom = (TimedCustomPunishment) punishment;
            custom.applyEffects("death", custom.getCustomMetadata().effects().death());
        }
    }

}

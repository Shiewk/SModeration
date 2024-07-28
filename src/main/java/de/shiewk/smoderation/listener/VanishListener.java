package de.shiewk.smoderation.listener;

import de.shiewk.smoderation.SModeration;
import de.shiewk.smoderation.command.VanishCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class VanishListener implements Listener {

    @EventHandler public void onPlayerQuit(PlayerQuitEvent event){
        final Player player = event.getPlayer();
        if (VanishCommand.isVanished(player)){
            VanishCommand.toggleVanish(player);
        }
        for (Player vanishedPlayer : VanishCommand.getVanishedPlayers()) {
            // to clean up visibility status
            player.hideEntity(SModeration.PLUGIN, vanishedPlayer);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR) public void onPlayerJoin(PlayerJoinEvent event){
        Bukkit.getScheduler().scheduleSyncDelayedTask(SModeration.PLUGIN, () -> {
            final Player player = event.getPlayer();
            if (player.hasPermission("smod.vanish.see")){
                for (Player vanishedPlayer : VanishCommand.getVanishedPlayers()) {
                    // to show visible vanished players
                    player.showEntity(SModeration.PLUGIN, vanishedPlayer);
                }
            }
        });
    }

}

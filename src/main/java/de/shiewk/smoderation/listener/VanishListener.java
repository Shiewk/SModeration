package de.shiewk.smoderation.listener;

import de.shiewk.smoderation.SModeration;
import de.shiewk.smoderation.command.VanishCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static de.shiewk.smoderation.SModeration.SECONDARY_COLOR;
import static net.kyori.adventure.text.Component.text;

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
            final Player player = event.getPlayer().getPlayer();
            assert player != null;
            if (player.hasPermission("smod.vanish.see")){
                for (Player vanishedPlayer : VanishCommand.getVanishedPlayers()) {
                    // to show visible vanished players
                    player.showEntity(SModeration.PLUGIN, vanishedPlayer);
                }
                VanishCommand.listVanishedPlayersTo(player);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event){
        final Component message = event.deathMessage();
        if (VanishCommand.isVanished(event.getPlayer()) && message != null){
            event.deathMessage(null);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("smod.vanish.see")){
                    onlinePlayer.sendMessage(text("[VANISH] ").color(SECONDARY_COLOR).append(message));
                }
            }
        }
    }

}

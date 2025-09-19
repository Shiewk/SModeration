package de.shiewk.smoderation.paper.listener;

import de.shiewk.smoderation.paper.SModerationPaper;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class FoliaInventoryUpdatingListener implements Listener {

    public static final String METADATA_KEY = "smod_invtick";

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        ScheduledTask task = player.getScheduler().runAtFixedRate(
                SModerationPaper.PLUGIN,
                t -> CustomInventoryListener.tickForPlayer(player),
                null,
                1,
                1
        );
        player.setMetadata(METADATA_KEY, new FixedMetadataValue(
                SModerationPaper.PLUGIN,
                task
        ));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        for (MetadataValue meta : event.getPlayer().getMetadata("smod_invtick")) {
            if (meta.getOwningPlugin() == SModerationPaper.PLUGIN) {
                if (meta.value() instanceof ScheduledTask task) {
                    task.cancel();
                }
            }
        }
    }

}

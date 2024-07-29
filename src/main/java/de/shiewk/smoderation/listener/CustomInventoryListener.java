package de.shiewk.smoderation.listener;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import de.shiewk.smoderation.inventory.AutoUpdatingCustomInventory;
import de.shiewk.smoderation.inventory.CustomInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class CustomInventoryListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event){
        if (event.getInventory().getHolder() instanceof CustomInventory customInventory){
            event.setCancelled(true);
            customInventory.click(event.getCurrentItem(), event);
        }
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event){
        if (event.getInventory().getHolder() instanceof CustomInventory){
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerTickEnd(ServerTickEndEvent event){
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getOpenInventory().getTopInventory().getHolder() instanceof AutoUpdatingCustomInventory ci) {
                ci.refresh();
            }
        }
    }
}

package de.shiewk.smoderation.event;

import de.shiewk.smoderation.inventory.CustomInventory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CustomInventoryEvents implements Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event){
        if (event.getInventory().getHolder() instanceof CustomInventory customInventory){
            customInventory.click(event.getCurrentItem());
            event.setCancelled(true);
        }
    }
}

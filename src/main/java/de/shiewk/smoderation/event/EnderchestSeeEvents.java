package de.shiewk.smoderation.event;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.PlayerInventory;

public class EnderchestSeeEvents implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        final Inventory clicked = event.getView().getTopInventory();
        if (!(clicked instanceof PlayerInventory)){
            final InventoryHolder holder = clicked.getHolder();
            if (holder instanceof HumanEntity humanHolder){
                if (!event.getWhoClicked().hasPermission("smod.enderchestsee.modify")){
                    event.setCancelled(true);
                }
            }
        }
    }

}

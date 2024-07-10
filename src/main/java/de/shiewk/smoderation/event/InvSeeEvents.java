package de.shiewk.smoderation.event;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import java.util.Objects;

public class InvSeeEvents implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        final Inventory clicked = event.getView().getTopInventory();
        if (clicked instanceof PlayerInventory inventory){
            final HumanEntity holder = inventory.getHolder();
            if (Objects.equals(holder, event.getWhoClicked())){
                return;
            }
            if (!event.getWhoClicked().hasPermission("smod.invsee.modify")){
                event.setCancelled(true);
                return;
            }
            if (holder != null) {
                if (holder.hasPermission("smod.invsee.preventmodify")){
                    event.setCancelled(true);
                }
            }
        }
    }

}

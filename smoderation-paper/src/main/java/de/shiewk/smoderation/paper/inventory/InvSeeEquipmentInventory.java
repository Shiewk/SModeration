package de.shiewk.smoderation.paper.inventory;

import de.shiewk.smoderation.paper.SModerationPaper;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;

public class InvSeeEquipmentInventory implements AutoUpdatingCustomInventory {
    private final HumanEntity viewer;
    private final HumanEntity subject;
    private final Inventory inventory = Bukkit.createInventory(this, InventoryType.HOPPER, text("Player equipment"));
    private boolean changing = false;

    public InvSeeEquipmentInventory(HumanEntity viewer, HumanEntity subject) {
        this.viewer = viewer;
        this.subject = subject;
    }


    @Override
    public void refresh() {
        if (!changing){
            final EntityEquipment equipment = subject.getEquipment();
            inventory.setItem(0, equipment.getHelmet());
            inventory.setItem(1, equipment.getChestplate());
            inventory.setItem(2, equipment.getLeggings());
            inventory.setItem(3, equipment.getBoots());
            inventory.setItem(4, equipment.getItemInOffHand());
        }
    }

    @Override
    public void open() {
        refresh();
        viewer.openInventory(getInventory());
    }

    @Override
    public void click(ItemStack stack, InventoryClickEvent event) {
        if (viewer.hasPermission("smod.invsee.modify") && !subject.hasPermission("smod.invsee.preventmodify")){
            event.setCancelled(false);
            changing = true;
            Bukkit.getScheduler().scheduleSyncDelayedTask(SModerationPaper.PLUGIN, () -> {
                changing = false;
                final EntityEquipment equipment = subject.getEquipment();
                equipment.setHelmet(inventory.getItem(0));
                equipment.setChestplate(inventory.getItem(1));
                equipment.setLeggings(inventory.getItem(2));
                equipment.setBoots(inventory.getItem(3));
                equipment.setItemInOffHand(inventory.getItem(4));
            });
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}

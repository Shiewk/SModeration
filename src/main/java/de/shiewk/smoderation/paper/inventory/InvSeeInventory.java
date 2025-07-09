package de.shiewk.smoderation.paper.inventory;

import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;

public class InvSeeInventory implements AutoUpdatingCustomInventory {
    private final HumanEntity viewer;
    private final HumanEntity subject;
    private final Inventory subjectInventory;
    private final Inventory inventory;
    private boolean changing = false;

    public InvSeeInventory(HumanEntity viewer, HumanEntity subject) {
        this.viewer = viewer;
        this.subject = subject;
        this.subjectInventory = subject.getInventory();
        this.inventory = Bukkit.createInventory(this, 36, text("Player inventory"));
    }


    @Override
    public void refresh() {
        if (!changing){
            loadContents();
        }
    }

    private void loadContents() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, subjectInventory.getItem(i));
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
            SchedulerUtil.scheduleGlobal(SModerationPaper.PLUGIN, () -> {
                changing = false;
                applyChanges();
            });
        }
    }

    private void applyChanges() {
        for (int i = 0; i < inventory.getSize(); i++) {
            subjectInventory.setItem(i, inventory.getItem(i));
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}

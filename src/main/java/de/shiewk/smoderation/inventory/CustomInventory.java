package de.shiewk.smoderation.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface CustomInventory extends InventoryHolder {

    void refresh();
    void open();
    void click(ItemStack stack);

    default ItemStack createEmptyStack(){
        ItemStack stack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        stack.editMeta(meta -> meta.displayName(Component.empty()));
        return stack;
    }

    default Component applyFormatting(Component component){
        return component.decoration(TextDecoration.ITALIC, false);
    }
}

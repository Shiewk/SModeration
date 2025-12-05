package de.shiewk.smoderation.paper.inventory;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface CustomInventory extends InventoryHolder {

    void refresh();
    void open();
    void click(ItemStack stack, InventoryClickEvent event);

    default ItemStack createEmptyStack(){
        ItemStack stack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        stack.editMeta(meta -> meta.displayName(Component.empty()));
        return stack;
    }

    default Component applyFormatting(Component component){
        return component.decoration(TextDecoration.ITALIC, false);
    }

    static Component renderComponent(Player viewer, Component component){
        Component render = GlobalTranslator.render(component, viewer.locale());
        List<Component> oldChildren = render.children();
        List<Component> newChildren = new ObjectArrayList<>(oldChildren.size());
        for (Component oldChild : oldChildren) {
            Component e = renderComponent(viewer, oldChild);
            newChildren.add(e);
        }
        return render.children(newChildren);
    }
}

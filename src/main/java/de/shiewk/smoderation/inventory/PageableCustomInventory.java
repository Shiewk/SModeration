package de.shiewk.smoderation.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class PageableCustomInventory implements CustomInventory {
    public abstract int lastPage();
    public abstract void switchPage();
    private ItemStack previousStack = null;
    private ItemStack nextStack = null;
    private int page = 0;

    public int getPage(){
        return page;
    }

    @Override
    public void click(ItemStack stack, InventoryClickEvent event) {
        if (stack != null){
            if (stack.equals(previousStack)){
                previousPage();
            } else if (stack.equals(nextStack)) {
                nextPage();
            }
        }
    }

    public void nextPage(){
        if (page < lastPage()){
            page++;
            switchPage();
            refresh();
        }
    }

    public void previousPage(){
        if (page > 0){
            page--;
            switchPage();
            refresh();
        }
    }

    public ItemStack createPreviousPageStack(){
        boolean allowed = page > 0;
        TextColor color = allowed ? NamedTextColor.GREEN : NamedTextColor.RED;
        int skip = allowed ? page : page+1;
        ItemStack stack = new ItemStack(allowed ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        stack.editMeta(meta -> meta.displayName(applyFormatting(Component.text("Previous page (%s/%s)".formatted(skip, lastPage()+1)).color(color))));
        previousStack = stack;
        return stack;
    }

    public ItemStack createNextPageStack(){
        boolean allowed = page < lastPage();
        TextColor color = allowed ? NamedTextColor.GREEN : NamedTextColor.RED;
        int skip = allowed ? page+2 : page+1;
        ItemStack stack = new ItemStack(allowed ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        stack.editMeta(meta -> meta.displayName(applyFormatting(Component.text("Next page (%s/%s)".formatted(skip, lastPage()+1)).color(color))));
        nextStack = stack;
        return stack;
    }
}

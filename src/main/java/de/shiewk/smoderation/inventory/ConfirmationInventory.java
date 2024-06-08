package de.shiewk.smoderation.inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ConfirmationInventory implements CustomInventory {
    private final Inventory inventory;
    private final Player player;
    private final String prompt;
    private final ItemStack yesStack;
    private final ItemStack noStack;
    private final Runnable onAccept;
    private final Runnable onReject;
    private final boolean reversed;

    public ConfirmationInventory(Player player, String prompt, Runnable onAccept, Runnable onReject, boolean reversed) {
        this.player = player;
        this.prompt = prompt;
        this.onAccept = onAccept;
        this.onReject = onReject;
        this.reversed = reversed;
        inventory = Bukkit.createInventory(this, InventoryType.HOPPER, Component.text(this.prompt));
        yesStack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        noStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
    }

    @Override
    public void refresh() {
        yesStack.editMeta(meta -> meta.displayName(applyFormatting(Component.text("Yes").color(NamedTextColor.GREEN))));
        noStack.editMeta(meta -> meta.displayName(applyFormatting(Component.text("No").color(NamedTextColor.RED))));
        ItemStack confirmation = new ItemStack(Material.PAPER);
        confirmation.editMeta(meta -> meta.displayName(applyFormatting(Component.text(prompt).color(NamedTextColor.GOLD))));

        inventory.setItem(reversed ? 4 : 0, noStack);
        inventory.setItem(2, confirmation);
        inventory.setItem(reversed ? 0 : 4, yesStack);
    }

    @Override
    public void open() {
        refresh();
        player.openInventory(getInventory());
    }

    @Override
    public void click(ItemStack stack, InventoryClickEvent event) {
        if (yesStack.equals(stack)){
            inventory.close();
            onAccept.run();
        } else if (noStack.equals(stack)) {
            inventory.close();
            onReject.run();
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}

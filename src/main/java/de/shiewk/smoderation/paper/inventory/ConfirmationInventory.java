package de.shiewk.smoderation.paper.inventory;

import io.papermc.paper.datacomponent.DataComponentTypes;
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

import static de.shiewk.smoderation.paper.inventory.CustomInventory.renderComponent;
import static net.kyori.adventure.text.Component.translatable;

public class ConfirmationInventory implements CustomInventory {
    private final Inventory inventory;
    private final Player player;
    private final Component prompt;
    private final ItemStack yesStack;
    private final ItemStack noStack;
    private final Runnable onAccept;
    private final Runnable onReject;
    private final boolean reversed;

    public ConfirmationInventory(Player player, Component prompt, Runnable onAccept, Runnable onReject, boolean reversed) {
        this.player = player;
        this.prompt = prompt;
        this.onAccept = onAccept;
        this.onReject = onReject;
        this.reversed = reversed;
        inventory = Bukkit.createInventory(this, InventoryType.HOPPER, this.prompt);
        yesStack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        noStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
    }

    @Override
    public void refresh() {
        yesStack.setData(DataComponentTypes.ITEM_NAME, renderComponent(player, applyFormatting(translatable("smod.confirm.yes"))));
        noStack.setData(DataComponentTypes.ITEM_NAME, renderComponent(player, applyFormatting(translatable("smod.confirm.no"))));
        ItemStack confirmation = new ItemStack(Material.PAPER);
        confirmation.setData(DataComponentTypes.ITEM_NAME, renderComponent(player, prompt.colorIfAbsent(NamedTextColor.GOLD)));

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

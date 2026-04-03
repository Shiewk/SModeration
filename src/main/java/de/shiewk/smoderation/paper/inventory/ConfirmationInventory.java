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
    private final Runnable onAccept;
    private final Runnable onReject;

    public ConfirmationInventory(Player player, Component prompt, Runnable onAccept, Runnable onReject) {
        this.player = player;
        this.prompt = prompt;
        this.onAccept = onAccept;
        this.onReject = onReject;
        inventory = Bukkit.createInventory(this, InventoryType.HOPPER, this.prompt);
    }

    @Override
    public void refresh() {
        ItemStack accept = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemStack confirmation = new ItemStack(Material.PAPER);
        ItemStack reject = new ItemStack(Material.RED_STAINED_GLASS_PANE);

        accept.setData(DataComponentTypes.ITEM_NAME, renderComponent(player, applyFormatting(translatable("smod.confirm.yes"))));
        confirmation.setData(DataComponentTypes.ITEM_NAME, renderComponent(player, prompt.colorIfAbsent(NamedTextColor.GOLD)));
        reject.setData(DataComponentTypes.ITEM_NAME, renderComponent(player, applyFormatting(translatable("smod.confirm.no"))));

        inventory.setItem(0, accept);
        inventory.setItem(2, confirmation);
        inventory.setItem(4, reject);
    }

    @Override
    public void open() {
        refresh();
        player.openInventory(getInventory());
    }

    @Override
    public void click(InventoryClickEvent event) {
        if (event.getSlot() == 0){
            inventory.close();
            onAccept.run();
        } else if (event.getSlot() == 4) {
            inventory.close();
            onReject.run();
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}

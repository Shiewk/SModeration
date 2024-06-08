package de.shiewk.smoderation.inventory;

import de.shiewk.smoderation.punishments.Punishment;
import de.shiewk.smoderation.punishments.PunishmentType;
import de.shiewk.smoderation.storage.PunishmentContainer;
import de.shiewk.smoderation.util.PlayerUtil;
import de.shiewk.smoderation.util.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SModMenu extends PageableCustomInventory {

    private final Inventory inventory;
    private final Player player;
    private final ArrayList<Punishment> punishments;

    public SModMenu(Player player, PunishmentContainer container) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, Component.text("SMod Menu"));
        punishments = container.copy();
    }

    @Override
    public int lastPage() {
        return Math.max((punishments.size() - 1) / 45, 0);
    }

    @Override
    public void switchPage() {
        player.playSound(player, Sound.BLOCK_STONE_HIT, 0.75f, 1f);
    }

    private ItemStack createPunishmentItem(Punishment punishment){
        final NamedTextColor PRIMARY_COLOR = NamedTextColor.AQUA;
        final NamedTextColor SECONDARY_COLOR = NamedTextColor.GREEN;
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        stack.editMeta(meta -> {
            if (meta instanceof SkullMeta skullMeta){
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(punishment.to));
            }
            meta.displayName(applyFormatting(Component.text(punishment.type.name).color(NamedTextColor.RED).decorate(TextDecoration.BOLD)));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(applyFormatting(Component.text("Player: ").color(SECONDARY_COLOR).append(Component.text(PlayerUtil.offlinePlayerName(punishment.to)).color(PRIMARY_COLOR))));
            lore.add(applyFormatting(Component.text("Punished by: ").color(SECONDARY_COLOR).append(Component.text(PlayerUtil.offlinePlayerName(punishment.by)).color(PRIMARY_COLOR))));
            lore.add(applyFormatting(Component.text("Timestamp: ").color(SECONDARY_COLOR).append(Component.text(TimeUtil.calendarTimestamp(punishment.time)).color(PRIMARY_COLOR))));
            if (punishment.type != PunishmentType.KICK){
                lore.add(applyFormatting(Component.text("Duration: ").color(SECONDARY_COLOR).append(Component.text(TimeUtil.formatTimeLong(punishment.until - punishment.time)).color(PRIMARY_COLOR))));
                long remainingTime = punishment.until - System.currentTimeMillis();
                final String expires;
                if (remainingTime > 0){
                    expires = "in " + TimeUtil.formatTimeLong(remainingTime);
                } else {
                    remainingTime *= -1;
                    expires = TimeUtil.formatTimeLong(remainingTime) + " ago";
                }
                lore.add(applyFormatting(Component.text("Expires: ").color(SECONDARY_COLOR).append(Component.text(expires).color(PRIMARY_COLOR))));
            }
            lore.add(applyFormatting(Component.text("Reason: ").color(SECONDARY_COLOR).append(Component.text(punishment.reason).color(PRIMARY_COLOR))));
            meta.lore(lore);
        });
        return stack;
    }

    @Override
    public void refresh() {
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, createEmptyStack());
        }
        inventory.setItem(45, createPreviousPageStack());
        inventory.setItem(53, createNextPageStack());

        for (int i = 0; i < 45; i++) {
            int ci = i + (getPage() * 45);
            if (punishments.size() > ci){
                inventory.setItem(i, createPunishmentItem(punishments.get(ci)));
            } else {
                inventory.setItem(i, new ItemStack(Material.AIR));
            }
        }
    }

    @Override
    public void open() {
        refresh();
        player.openInventory(this.inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}

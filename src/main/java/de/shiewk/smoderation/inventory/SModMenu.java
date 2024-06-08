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
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class SModMenu extends PageableCustomInventory {

    public enum Filter {
        ACTIVE("Active punishments", Punishment::isActive),
        OLD("Old punishments", p -> !p.isActive()),
        ALL("All punishments", p -> true);

        public static final Material ICON = Material.HOPPER;

        public final String name;
        public final Predicate<Punishment> filter;
        Filter(String name, Predicate<Punishment> filter) {
            this.name = name;
            this.filter = filter;
        }
    }

    public enum Sort {
        EXPIRY("Expiry", Comparator.comparingLong(p -> p.until)),
        TIME("Date", Comparator.comparingLong(p -> p.time)),
        PLAYER_NAME("Player name", (p1, p2) -> String.CASE_INSENSITIVE_ORDER.compare(PlayerUtil.offlinePlayerName(p1.to), PlayerUtil.offlinePlayerName(p2.to))),
        MODERATOR_NAME("Moderator name", (p1, p2) -> String.CASE_INSENSITIVE_ORDER.compare(PlayerUtil.offlinePlayerName(p1.by), PlayerUtil.offlinePlayerName(p2.by)));

        public static final Material ICON = Material.COMPARATOR;

        public final String name;
        public final Comparator<Punishment> comparator;

        Sort(String name, Comparator<Punishment> comparator) {
            this.name = name;
            this.comparator = comparator;
        }
    }

    public static final NamedTextColor PRIMARY_COLOR = NamedTextColor.AQUA;
    public static final NamedTextColor SECONDARY_COLOR = NamedTextColor.GREEN;
    public static final NamedTextColor INACTIVE_COLOR = NamedTextColor.GRAY;
    private static final NamespacedKey PUNISHMENT_STORE_KEY = new NamespacedKey("smod", "punishmentid");

    private final Inventory inventory;
    private final Player player;
    private final PunishmentContainer container;
    private List<Punishment> punishments;
    private ItemStack sortStack = null;
    private ItemStack filterStack = null;
    private int sort = 0;
    private int filter = 0;

    public SModMenu(Player player, PunishmentContainer container) {
        this.player = player;
        this.container = container;
        this.inventory = Bukkit.createInventory(this, 54, Component.text("SMod Menu"));
        reload();
    }

    public Sort getSort(){
        return Sort.values()[sort];
    }

    public Filter getFilter(){
        return Filter.values()[filter];
    }

    private void reload(){
        this.punishments = container.copy().stream().filter(getFilter().filter).sorted(getSort().comparator).toList();
    }

    @Override
    public int lastPage() {
        return Math.max((punishments.size() - 1) / 45, 0);
    }

    public void cycleFilter(boolean backwards){
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, backwards ? 0.8f : 2f);
        if (backwards){
            if (filter <= 0){
                filter = Filter.values().length-1;
            } else {
                filter--;
            }
        } else {
            if (filter >= Filter.values().length-1){
                filter = 0;
            } else {
                filter++;
            }
        }
        reload();
        refresh();
    }

    public void cycleSort(boolean backwards){
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, backwards ? 0.8f : 2f);
        if (backwards){
            if (sort <= 0){
                sort = Sort.values().length-1;
            } else {
                sort--;
            }
        } else {
            if (sort >= Sort.values().length-1){
                sort = 0;
            } else {
                sort++;
            }
        }
        reload();
        refresh();
    }

    @Override
    public void switchPage() {
        player.playSound(player, Sound.BLOCK_STONE_HIT, 0.75f, 1f);
    }

    private ItemStack createFilterItem(){
        final Filter filter = getFilter();
        final ItemStack stack = new ItemStack(Filter.ICON);
        stack.editMeta(meta -> {
            meta.displayName(applyFormatting(Component.text("Filter: " + filter.name).color(SECONDARY_COLOR)));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            for (Filter value : Filter.values()) {
                final boolean selected = filter == value;
                Component filterText = applyFormatting(Component.text((selected ? "\u00BB " : "") + value.name).color(selected ? PRIMARY_COLOR : INACTIVE_COLOR));
                lore.add(filterText);
            }
            lore.add(Component.empty());
            lore.add(applyFormatting(Component.text("\u00BB Click to switch filter").color(NamedTextColor.GOLD)));
            meta.lore(lore);
        });
        filterStack = stack;
        return stack;
    }

    private ItemStack createSortItem(){
        final Sort sort = getSort();
        final ItemStack stack = new ItemStack(Sort.ICON);
        stack.editMeta(meta -> {
            meta.displayName(applyFormatting(Component.text("Sort by: " + sort.name).color(PRIMARY_COLOR)));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            for (Sort value : Sort.values()) {
                final boolean selected = sort == value;
                Component sortText = applyFormatting(Component.text((selected ? "\u00BB " : "") + value.name).color(selected ? SECONDARY_COLOR : INACTIVE_COLOR));
                lore.add(sortText);
            }
            lore.add(Component.empty());
            lore.add(applyFormatting(Component.text("\u00BB Click to switch sorting option").color(NamedTextColor.GOLD)));
            meta.lore(lore);
        });
        sortStack = stack;
        return stack;
    }

    private ItemStack createPunishmentItem(Punishment punishment){
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
            if (punishment.wasCancelled()){
                lore.add(applyFormatting(Component.text("Cancelled by: ").color(NamedTextColor.RED).append(Component.text(PlayerUtil.offlinePlayerName(punishment.cancelledBy())).color(NamedTextColor.GOLD))));
            } else if (punishment.isActive()) {
                if ((punishment.type == PunishmentType.BAN && player.hasPermission("smod.cancelBan")) || (punishment.type == PunishmentType.MUTE && player.hasPermission("smod.cancelMute"))){
                    lore.add(Component.empty());
                    lore.add(applyFormatting(Component.text("\u00BB Click to cancel punishment").color(NamedTextColor.GOLD)));
                }
            }
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
        inventory.setItem(50, createFilterItem());
        inventory.setItem(48, createSortItem());

        for (int i = 0; i < 45; i++) {
            int ci = i + (getPage() * 45);
            if (punishments.size() > ci){
                final Punishment punishment = punishments.get(ci);
                final ItemStack item = createPunishmentItem(punishment);
                if (punishment.isActive()){
                    if ((punishment.type == PunishmentType.BAN && player.hasPermission("smod.cancelBan")) || (punishment.type == PunishmentType.MUTE && player.hasPermission("smod.cancelMute"))) {
                        item.editMeta(meta -> meta.getPersistentDataContainer().set(PUNISHMENT_STORE_KEY, PersistentDataType.LONG, punishment.time));
                    }
                }
                inventory.setItem(i, item);
            } else {
                inventory.setItem(i, new ItemStack(Material.AIR));
            }
        }
    }

    @Override
    public void click(ItemStack stack, InventoryClickEvent event) {
        super.click(stack, event);
        if (stack != null) {
            if (stack.equals(filterStack)){
                cycleFilter(event.isRightClick());
            } else if (stack.equals(sortStack)){
                cycleSort(event.isRightClick());
            }
            final ItemMeta itemMeta = stack.getItemMeta();
            if (itemMeta != null) {
                final PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
                final Long timestamp = persistentDataContainer.get(PUNISHMENT_STORE_KEY, PersistentDataType.LONG);
                if (timestamp != null) {
                    final Punishment punishment = container.findByTimestamp(timestamp);
                    if (punishment != null) {
                        new ConfirmationInventory(player, "Do you want to cancel this punishment?", () -> {
                            punishment.cancel(player.getUniqueId());
                            punishment.broadcastCancellation(container);
                            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                            this.open();
                        }, this::open, false).open();
                    }
                }
            }
        }
    }

    @Override
    public void open() {
        reload();
        refresh();
        player.openInventory(this.inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}

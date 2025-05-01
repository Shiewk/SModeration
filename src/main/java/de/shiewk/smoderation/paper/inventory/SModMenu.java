package de.shiewk.smoderation.paper.inventory;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import de.shiewk.smoderation.paper.SkinTextureProvider;
import de.shiewk.smoderation.paper.input.ChatInput;
import de.shiewk.smoderation.paper.punishments.Punishment;
import de.shiewk.smoderation.paper.punishments.PunishmentType;
import de.shiewk.smoderation.paper.storage.PunishmentContainer;
import de.shiewk.smoderation.paper.util.PlayerUtil;
import de.shiewk.smoderation.paper.util.TimeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static de.shiewk.smoderation.paper.SModerationPaper.*;
import static net.kyori.adventure.text.Component.text;

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
    private static final NamespacedKey PUNISHMENT_STORE_KEY = new NamespacedKey("smod", "punishmentid");

    private final Inventory inventory;
    private final Player player;
    private final PunishmentContainer container;
    private List<Punishment> punishments;
    private ItemStack sortStack = null;
    private ItemStack filterStack = null;
    private ItemStack searchStack = null;
    private ItemStack typeStack = null;
    private int sort = 0;
    private int filter = 0;
    private int type = -1;
    private String searchQuery = null;

    public SModMenu(Player player, PunishmentContainer container) {
        this.player = player;
        this.container = container;
        this.inventory = Bukkit.createInventory(this, 54, text("SMod Menu"));
        reload();
    }

    public Sort getSort(){
        return Sort.values()[sort];
    }

    public Filter getFilter(){
        return Filter.values()[filter];
    }

    public PunishmentType getType(){
        return type == -1 ? null : PunishmentType.values()[type];
    }

    private void reload(){
        this.punishments = container.copy().stream()
                .filter(getFilter().filter)
                .filter(p -> getType() == null || p.type == getType())
                .filter(p -> p.matchesSearchQuery(searchQuery))
                .sorted(getSort().comparator).toList();
    }

    public void promptSearchQuery(){
        Bukkit.getScheduler().scheduleSyncDelayedTask(PLUGIN, player::closeInventory);
        ChatInput.prompt(player, component -> {
            if (component instanceof TextComponent text){
                this.searchQuery = text.content();
                // chat event is async
                Bukkit.getScheduler().scheduleSyncDelayedTask(PLUGIN, this::open);
            }
        }, text("Enter your search query in chat").color(SECONDARY_COLOR), 30);
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

    public void cycleType(boolean backwards){
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, backwards ? 0.8f : 2f);
        if (backwards){
            if (type <= -1){
                type = PunishmentType.values().length-1;
            } else {
                type--;
            }
        } else {
            if (type >= PunishmentType.values().length-1){
                type = -1;
            } else {
                type++;
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
            meta.displayName(applyFormatting(text("Filter: " + filter.name).color(PRIMARY_COLOR)));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            for (Filter value : Filter.values()) {
                final boolean selected = filter == value;
                Component filterText = applyFormatting(text((selected ? "\u00BB " : "") + value.name).color(selected ? SECONDARY_COLOR : INACTIVE_COLOR));
                lore.add(filterText);
            }
            lore.add(Component.empty());
            lore.add(applyFormatting(text("\u00BB Click to switch filter").color(NamedTextColor.GOLD)));
            meta.lore(lore);
        });
        filterStack = stack;
        return stack;
    }

    private ItemStack createTypeItem(){
        final PunishmentType type = getType();
        final ItemStack stack = new ItemStack(Material.CHEST);
        stack.editMeta(meta -> {
            meta.displayName(applyFormatting(text("Type: " + (type == null ? "All" : type.name)).color(PRIMARY_COLOR)));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            final Consumer<PunishmentType> addToLore = value -> {
                final boolean selected = type == value;
                Component typeText = applyFormatting(text((selected ? "\u00BB " : "") + (value == null ? "All" : value.name)).color(selected ? SECONDARY_COLOR : INACTIVE_COLOR));
                lore.add(typeText);
            };
            addToLore.accept(null);
            for (PunishmentType value : PunishmentType.values()) {
                addToLore.accept(value);
            }
            lore.add(Component.empty());
            lore.add(applyFormatting(text("\u00BB Click to switch type").color(NamedTextColor.GOLD)));
            meta.lore(lore);
        });
        return typeStack = stack;
    }

    private ItemStack createSortItem(){
        final Sort sort = getSort();
        final ItemStack stack = new ItemStack(Sort.ICON);
        stack.editMeta(meta -> {
            meta.displayName(applyFormatting(text("Sort by: " + sort.name).color(PRIMARY_COLOR)));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            for (Sort value : Sort.values()) {
                final boolean selected = sort == value;
                Component sortText = applyFormatting(text((selected ? "\u00BB " : "") + value.name).color(selected ? SECONDARY_COLOR : INACTIVE_COLOR));
                lore.add(sortText);
            }
            lore.add(Component.empty());
            lore.add(applyFormatting(text("\u00BB Click to switch sorting option").color(NamedTextColor.GOLD)));
            meta.lore(lore);
        });
        sortStack = stack;
        return stack;
    }

    private ItemStack createSearchItem(){
        final ItemStack stack = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        stack.editMeta(meta -> {
            meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
            meta.displayName(applyFormatting(text("Search").color(PRIMARY_COLOR)));
            final ArrayList<Component> lore = new ArrayList<>(List.of(
                    Component.empty(),
                    applyFormatting(text("Current search query: %s".formatted(searchQuery == null ? "None" : "\"" + searchQuery + "\"")).color(SECONDARY_COLOR)),
                    Component.empty(),
                    applyFormatting(text("\u00BB Click to enter new search query").color(NamedTextColor.GOLD))
            ));
            if (searchQuery != null){
                lore.add(applyFormatting(text("\u00BB Right click to remove search query").color(NamedTextColor.GOLD)));
            }
            meta.lore(lore);
        });
        return searchStack = stack;
    }

    private CompletableFuture<ItemStack> createPunishmentItem(Punishment punishment){
        SkinTextureProvider provider = getTextureProvider();
        if (provider != null) {
            return provider.textureProperty(punishment.to)
                    .thenApply(texture -> {
                        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
                        stack.editMeta(meta -> {
                            if (meta instanceof SkullMeta skullMeta){
                                PlayerProfile profile = Bukkit.createProfile(punishment.to);
                                profile.setProperty(new ProfileProperty(
                                        "textures",
                                        "ewogICJ0aW1lc3RhbXAiIDogMTc0NjA5MDc5MDc1NCwKICAicHJvZmlsZUlkIiA6ICJhOGY0YzVhOWFiMmM0YWVlODg2MWRlMDhkMmJmMzYyNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJlaW5lU3BlaXNlIiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzNiMDc0N2UzMTEyYjJiMmQ0MGE1M2Q5YjZlZTkxMDQ4ODQyMDc5MDllY2ZjMzdlZDdmYmZjM2FhMzBhNDE0NGQiCiAgICB9LAogICAgIkNBUEUiIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzVjMjk0MTAwNTdlMzJhYmVjMDJkODcwZWNiNTJlYzI1ZmI0NWVhODFlNzg1YTc4NTRhZTg0MjlkNzIzNmNhMjYiCiAgICB9CiAgfQp9"
                                ));
                                skullMeta.setPlayerProfile(profile);
                            }
                            addPunishmentInfo(punishment, meta);
                        });
                        return stack;
                    });
        } else {
            ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
            stack.editMeta(meta -> {
                if (meta instanceof SkullMeta skullMeta){
                    try {
                        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(punishment.to));
                    } catch (NullPointerException e) {
                        LOGGER.warn("Player {} has a punishment but was never on this server!", punishment.to);
                    }
                }
                addPunishmentInfo(punishment, meta);
            });
            return CompletableFuture.completedFuture(stack);
        }
    }

    private void addPunishmentInfo(Punishment punishment, ItemMeta meta) {
        meta.displayName(applyFormatting(text(punishment.type.name).color(NamedTextColor.RED).decorate(TextDecoration.BOLD)));
        ArrayList<Component> lore = new ArrayList<>();
        lore.add(applyFormatting(text("Player: ").color(SECONDARY_COLOR).append(text(PlayerUtil.offlinePlayerName(punishment.to)).color(PRIMARY_COLOR))));
        lore.add(applyFormatting(text("Punished by: ").color(SECONDARY_COLOR).append(text(PlayerUtil.offlinePlayerName(punishment.by)).color(PRIMARY_COLOR))));
        lore.add(applyFormatting(text("Timestamp: ").color(SECONDARY_COLOR).append(text(TimeUtil.calendarTimestamp(punishment.time)).color(PRIMARY_COLOR))));
        if (punishment.type != PunishmentType.KICK){
            lore.add(applyFormatting(text("Duration: ").color(SECONDARY_COLOR).append(text(TimeUtil.formatTimeLong(punishment.until - punishment.time)).color(PRIMARY_COLOR))));
            long remainingTime = punishment.until - System.currentTimeMillis();
            final String expires;
            if (remainingTime > 0){
                expires = "in " + TimeUtil.formatTimeLong(remainingTime);
            } else {
                remainingTime *= -1;
                expires = TimeUtil.formatTimeLong(remainingTime) + " ago";
            }
            lore.add(applyFormatting(text("Expires: ").color(SECONDARY_COLOR).append(text(expires).color(PRIMARY_COLOR))));
        }
        lore.add(applyFormatting(text("Reason: ").color(SECONDARY_COLOR).append(text(punishment.reason).color(PRIMARY_COLOR))));
        if (punishment.wasUndone()){
            lore.add(applyFormatting(text("Undone by: ").color(NamedTextColor.RED).append(text(PlayerUtil.offlinePlayerName(punishment.undoneBy())).color(NamedTextColor.GOLD))));
        } else if (punishment.isActive()) {
            if ((punishment.type == PunishmentType.BAN && player.hasPermission("smod.unban")) || (punishment.type == PunishmentType.MUTE && player.hasPermission("smod.unmute"))){
                lore.add(Component.empty());
                lore.add(applyFormatting(text("\u00BB Click to undo punishment").color(NamedTextColor.GOLD)));
            }
        }
        meta.lore(lore);
    }

    private int rfId = 0;
    @Override
    public void refresh() {
        int rfId = ++this.rfId;
        while (getPage() > lastPage()){
            previousPage();
        }
        inventory.clear();
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, createEmptyStack());
        }
        inventory.setItem(45, createPreviousPageStack());
        inventory.setItem(53, createNextPageStack());
        inventory.setItem(47, createSearchItem());
        inventory.setItem(48, createTypeItem());
        inventory.setItem(50, createFilterItem());
        inventory.setItem(51, createSortItem());

        for (int i = 0; i < 45; i++) {
            int ci = i + (getPage() * 45);
            if (punishments.size() > ci){
                final Punishment punishment = punishments.get(ci);
                int slot = i;
                createPunishmentItem(punishment).thenAccept(item -> {
                    if (rfId != this.rfId) return;
                    if (punishment.isActive()){
                        if ((punishment.type == PunishmentType.BAN && player.hasPermission("smod.unban")) || (punishment.type == PunishmentType.MUTE && player.hasPermission("smod.unmute"))) {
                            item.editMeta(meta -> meta.getPersistentDataContainer().set(PUNISHMENT_STORE_KEY, PersistentDataType.LONG, punishment.time));
                        }
                    }
                    inventory.setItem(slot, item);
                }).exceptionally(x -> {
                    LOGGER.warn("Error creating punishment item", x);
                    return null;
                });
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
            } else if (stack.equals(searchStack)){
                if (event.isRightClick() && searchQuery != null){
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 0.8f);
                    searchQuery = null;
                    reload();
                    refresh();
                } else {
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 2f);
                    promptSearchQuery();
                }
            } else if (stack.equals(typeStack)) {
                cycleType(event.isRightClick());
            }
            final ItemMeta itemMeta = stack.getItemMeta();
            if (itemMeta != null) {
                final PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
                final Long timestamp = persistentDataContainer.get(PUNISHMENT_STORE_KEY, PersistentDataType.LONG);
                if (timestamp != null) {
                    final Punishment punishment = container.findByTimestamp(timestamp);
                    if (punishment != null) {
                        new ConfirmationInventory(player, "Do you want to undo this punishment?", () -> {
                            punishment.undo(player.getUniqueId());
                            punishment.broadcastUndo(container);
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

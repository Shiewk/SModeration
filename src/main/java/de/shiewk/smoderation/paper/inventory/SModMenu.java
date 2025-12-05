package de.shiewk.smoderation.paper.inventory;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import de.shiewk.smoderation.paper.SkinTextureProvider;
import de.shiewk.smoderation.paper.input.ChatInput;
import de.shiewk.smoderation.paper.punishments.Punishment;
import de.shiewk.smoderation.paper.punishments.PunishmentType;
import de.shiewk.smoderation.paper.storage.PunishmentContainer;
import de.shiewk.smoderation.paper.util.PlayerUtil;
import de.shiewk.smoderation.paper.util.SchedulerUtil;
import de.shiewk.smoderation.paper.util.TimeUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static de.shiewk.smoderation.paper.SModerationPaper.*;
import static de.shiewk.smoderation.paper.inventory.CustomInventory.renderComponent;
import static net.kyori.adventure.text.Component.*;

public class SModMenu extends PageableCustomInventory {

    public enum Filter {
        ACTIVE(translatable("smod.menu.filter.active"), Punishment::isActive),
        OLD(translatable("smod.menu.filter.expired"), p -> !p.isActive()),
        ALL(translatable("smod.menu.filter.all"), p -> true);

        public static final Material ICON = Material.HOPPER;

        public final Component name;
        public final Predicate<Punishment> filter;
        Filter(Component name, Predicate<Punishment> filter) {
            this.name = name;
            this.filter = filter;
        }
    }

    public enum Sort {
        EXPIRY(translatable("smod.menu.sort.expiry"), Comparator.comparingLong(p -> p.until)),
        TIME(translatable("smod.menu.sort.time"), Comparator.comparingLong(p -> p.time)),
        PLAYER_NAME(translatable("smod.menu.sort.playerName"), (p1, p2) -> String.CASE_INSENSITIVE_ORDER.compare(PlayerUtil.offlinePlayerName(p1.to), PlayerUtil.offlinePlayerName(p2.to))),
        MODERATOR_NAME(translatable("smod.menu.sort.moderatorName"), (p1, p2) -> String.CASE_INSENSITIVE_ORDER.compare(PlayerUtil.offlinePlayerName(p1.by), PlayerUtil.offlinePlayerName(p2.by)));

        public static final Material ICON = Material.COMPARATOR;

        public final Component name;
        public final Comparator<Punishment> comparator;

        Sort(Component name, Comparator<Punishment> comparator) {
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
        this.inventory = Bukkit.createInventory(this, 54, translatable("smod.menu"));
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
        SchedulerUtil.scheduleForEntity(PLUGIN, player, player::closeInventory);
        ChatInput.prompt(player, component -> {
            if (component instanceof TextComponent text){
                this.searchQuery = text.content();
                // chat event is async
                SchedulerUtil.scheduleForEntity(PLUGIN, player, this::open);
            }
        }, translatable("smod.menu.search.query").color(SECONDARY_COLOR), 30);
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
        stack.setData(DataComponentTypes.ITEM_NAME, renderComponent(player, translatable("smod.menu.filter", filter.name).color(PRIMARY_COLOR)));
        ItemLore.Builder loreBuilder = ItemLore.lore();

        loreBuilder.addLine(empty());
        for (Filter value : Filter.values()) {
            final boolean selected = filter == value;
            Component filterText = renderComponent(player, applyFormatting(text((selected ? "\u00BB " : ""), selected ? SECONDARY_COLOR : INACTIVE_COLOR).append(value.name)));
            loreBuilder.addLine(filterText);
        }
        loreBuilder.addLine(empty());
        loreBuilder.addLine(renderComponent(player, applyFormatting(translatable("smod.menu.filter.switch", NamedTextColor.GOLD))));

        stack.setData(DataComponentTypes.LORE, loreBuilder.build());
        return filterStack = stack;
    }

    private ItemStack createTypeItem(){
        final PunishmentType type = getType();
        final ItemStack stack = new ItemStack(Material.CHEST);
        stack.setData(DataComponentTypes.ITEM_NAME, renderComponent(player, translatable("smod.menu.type", (type == null ? translatable("smod.menu.type.all") : type.name))).color(PRIMARY_COLOR));

        ItemLore.Builder loreBuilder = ItemLore.lore();
        loreBuilder.addLine(empty());
        final Consumer<PunishmentType> addToLore = value -> {
            final boolean selected = type == value;
            Component typeText = renderComponent(player, applyFormatting(text((selected ? "\u00BB " : ""), selected ? SECONDARY_COLOR : INACTIVE_COLOR).append(value == null ? translatable("smod.menu.type.all") : value.name)));
            loreBuilder.addLine(typeText);
        };
        addToLore.accept(null);
        for (PunishmentType value : PunishmentType.values()) {
            addToLore.accept(value);
        }

        loreBuilder.addLine(empty());
        loreBuilder.addLine(renderComponent(player, applyFormatting(translatable("smod.menu.type.switch", NamedTextColor.GOLD))));

        stack.setData(DataComponentTypes.LORE, loreBuilder);
        return typeStack = stack;
    }

    private ItemStack createSortItem(){
        final Sort sort = getSort();
        final ItemStack stack = new ItemStack(Sort.ICON);
        stack.setData(DataComponentTypes.ITEM_NAME, renderComponent(player, translatable("smod.menu.sort", sort.name).color(PRIMARY_COLOR)));

        ItemLore.Builder loreBuilder = ItemLore.lore();
        loreBuilder.addLine(empty());

        for (Sort value : Sort.values()) {
            final boolean selected = sort == value;
            Component sortText = renderComponent(player, applyFormatting(text((selected ? "\u00BB " : ""), selected ? SECONDARY_COLOR : INACTIVE_COLOR).append(value.name)));
            loreBuilder.addLine(sortText);
        }

        loreBuilder.addLine(empty());
        loreBuilder.addLine(renderComponent(player, applyFormatting(translatable("smod.menu.sort.switch", NamedTextColor.GOLD))));

        stack.setData(DataComponentTypes.LORE, loreBuilder);
        return sortStack = stack;
    }

    private ItemStack createSearchItem(){
        final ItemStack stack = new ItemStack(Material.FLOWER_BANNER_PATTERN);

        try {
            stack.setData(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP);
        } catch (NoSuchFieldError e) {
            // that component is no longer present under that name,
            // we just create the stack without it instead of throwing
        }

        stack.setData(DataComponentTypes.ITEM_NAME, renderComponent(player, translatable("smod.menu.search", PRIMARY_COLOR)));

        ItemLore.Builder loreBuilder = ItemLore.lore();
        loreBuilder.addLines(List.of(
                empty(),
                renderComponent(player, applyFormatting(translatable("smod.menu.search.current", searchQuery == null ? translatable("smod.menu.search.none") : text('"' + searchQuery + '"'))).color(SECONDARY_COLOR)),
                empty(),
                renderComponent(player, applyFormatting(translatable("smod.menu.search.new", NamedTextColor.GOLD)))
        ));
        if (searchQuery != null){
            loreBuilder.addLine(renderComponent(player, applyFormatting(translatable("smod.menu.search.remove", NamedTextColor.GOLD))));
        }
        stack.setData(DataComponentTypes.LORE, loreBuilder);
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
                                        texture
                                ));
                                skullMeta.setPlayerProfile(profile);
                            }
                        });
                        addPunishmentInfo(punishment, stack);
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
            });
            addPunishmentInfo(punishment, stack);
            return CompletableFuture.completedFuture(stack);
        }
    }

    private void addPunishmentInfo(Punishment punishment, ItemStack stack) {
        stack.setData(DataComponentTypes.CUSTOM_NAME, renderComponent(player, applyFormatting(punishment.type.name.color(NamedTextColor.RED).decorate(TextDecoration.BOLD))));
        ItemLore.Builder lore = ItemLore.lore();

        lore.addLine(renderComponent(player, applyFormatting(translatable("smod.menu.info.player", text(PlayerUtil.offlinePlayerName(punishment.to))))));
        lore.addLine(renderComponent(player, applyFormatting(translatable("smod.menu.info.punishedBy", text(PlayerUtil.offlinePlayerName(punishment.by))))));
        lore.addLine(renderComponent(player, applyFormatting(translatable("smod.menu.info.timestamp", TimeUtil.calendarTimestamp(punishment.time)))));

        if (punishment.type != PunishmentType.KICK){
            lore.addLine(renderComponent(player, applyFormatting(translatable("smod.menu.info.duration", TimeUtil.formatTimeLong(punishment.until - punishment.time)))));
            long remainingTime = punishment.until - System.currentTimeMillis();
            if (remainingTime > 0){
                lore.addLine(renderComponent(player, applyFormatting(translatable("smod.menu.info.expiry.future", TimeUtil.formatTimeLong(remainingTime)))));
            } else {
                lore.addLine(renderComponent(player, applyFormatting(translatable("smod.menu.info.expiry.past", TimeUtil.formatTimeLong(-remainingTime)))));
            }
        }

        lore.addLine(renderComponent(player, applyFormatting(translatable("smod.menu.info.reason", text(punishment.reason)))));

        if (punishment.wasUndone()){
            lore.addLine(renderComponent(player, applyFormatting(translatable("smod.menu.info.undone", text(PlayerUtil.offlinePlayerName(punishment.undoneBy()))))));
        } else if (punishment.isActive()) {
            if ((punishment.type == PunishmentType.BAN && player.hasPermission("smod.unban")) || (punishment.type == PunishmentType.MUTE && player.hasPermission("smod.unmute"))){
                lore.addLine(empty());
                lore.addLine(renderComponent(player, applyFormatting(translatable("smod.menu.info.click", NamedTextColor.GOLD))));
            }
        }
        stack.setData(DataComponentTypes.LORE, lore);
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
                        new ConfirmationInventory(player, translatable("smod.menu.undoConfirmation"), () -> {
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

package de.shiewk.smoderation.paper;

import com.google.gson.Gson;
import de.shiewk.smoderation.paper.command.*;
import de.shiewk.smoderation.paper.input.ChatInput;
import de.shiewk.smoderation.paper.input.ChatInputListener;
import de.shiewk.smoderation.paper.listener.*;
import de.shiewk.smoderation.paper.punishments.Ban;
import de.shiewk.smoderation.paper.punishments.Kick;
import de.shiewk.smoderation.paper.punishments.Mute;
import de.shiewk.smoderation.paper.punishments.PunishmentManager;
import de.shiewk.smoderation.paper.translation.TranslatorManager;
import de.shiewk.smoderation.paper.util.ColorPalette;
import de.shiewk.smoderation.paper.util.SModLegacy;
import de.shiewk.smoderation.paper.util.SchedulerUtil;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static de.shiewk.smoderation.paper.command.VanishCommand.isVanished;
import static de.shiewk.smoderation.paper.command.VanishCommand.toggleVanish;
import static org.bukkit.Bukkit.getPluginManager;

@SuppressWarnings("UnstableApiUsage") // Paper Brigadier API
public final class SModerationPaper extends JavaPlugin {

    public static final Gson gson = new Gson();
    public static ComponentLogger LOGGER = null;
    public static SModerationPaper PLUGIN = null;
    private static SkinTextureProvider textureProvider = null;
    private ColorPalette colors;

    private final TranslatorManager translatorManager = new TranslatorManager(
            Key.key("smoderation", "translations"),
            createMiniMessage(),
            "smoderation/translations/",
            new Locale[] {
                    Locale.forLanguageTag("en-US"),
                    Locale.forLanguageTag("de-DE")
            }
    );

    private PunishmentManager punishmentManager;

    public static FileConfiguration config() {
        return PLUGIN.getConfig();
    }

    public static ColorPalette colors() {
        return PLUGIN.colors;
    }

    @Override
    public void onLoad() {
        LOGGER = getComponentLogger();
        LOGGER.info("Folia: {}", SchedulerUtil.isFolia ? "yes" : "no");
        PLUGIN = this;
        LOGGER.info("Loading translations");
        if (config().getBoolean("custom-messages", false)) {
            translatorManager.loadCustomMessages(getDataPath().resolve("messages.json"));
        } else {
            translatorManager.load();
        }
        updateConfig();

        this.punishmentManager = new PunishmentManager(getDataPath().resolve("punishments.v2"));
        this.punishmentManager.registerType("mute", new Mute.Factory());
        this.punishmentManager.registerType("ban", new Ban.Factory());
        this.punishmentManager.registerType("kick", new Kick.Factory());

        this.colors = new ColorPalette(
                parseColor(config().getString("colors.primary")),
                parseColor(config().getString("colors.secondary")),
                parseColor(config().getString("colors.detail"))
        );

        SModLegacy.migrateV1PunishmentsFile(
                this.punishmentManager,
                getDataPath().resolve("container.gz"),
                getDataPath().resolve("v1-backup.gz")
        );
    }

    private TextColor parseColor(String string) {
        if (string == null || string.length() != 7) {
            throw new IllegalArgumentException("Color not formatted correctly: " + string);
        }
        TextColor color = TextColor.fromHexString(string);
        if (color == null) {
            throw new IllegalArgumentException("Color not formatted correctly: " + string);
        }
        return color;
    }

    public boolean isFeatureEnabled(String feature){
        return getConfig().getBoolean("features."+feature, true);
    }

    @Override
    public void onEnable() {
        if (isFeatureEnabled("punishments")) listen(new PunishmentListener(punishmentManager));
        if (isFeatureEnabled("punishments")) listen(new CacheListener(punishmentManager));
        if (isFeatureEnabled("invsee")) listen(new InvSeeListener());
        if (isFeatureEnabled("enderchestsee")) listen(new EnderchestSeeListener());
        if (isFeatureEnabled("socialspy")) listen(new SocialSpyListener());
        if (isFeatureEnabled("vanish")) listen(new VanishListener());

        listen(new CustomInventoryListener());
        listen(new ChatInputListener());

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();

            if (isFeatureEnabled("punishments")){
                registerCommand(commands, new KickCommand(punishmentManager));
                registerCommand(commands, new ModLogsCommand(punishmentManager));
                registerCommand(commands, new UnmuteCommand(punishmentManager));
                registerCommand(commands, new UnbanCommand(punishmentManager));
                registerCommand(commands, new MuteCommand(punishmentManager));
                registerCommand(commands, new BanCommand(punishmentManager));

                if (isFeatureEnabled("smodmenu")){
                    registerCommand(commands, new SModCommand(punishmentManager));
                }
            }

            if (isFeatureEnabled("invsee")) registerCommand(commands, new InvseeCommand());
            if (isFeatureEnabled("enderchestsee")) registerCommand(commands, new EnderchestSeeCommand());
            if (isFeatureEnabled("socialspy")) registerCommand(commands, new SocialSpyCommand());
            if (isFeatureEnabled("vanish")) registerCommand(commands, new VanishCommand());
            if (isFeatureEnabled("offlinetp")) registerCommand(commands, new OfflineTPCommand());
        });

        if (SchedulerUtil.isFolia){
            // Normal ticking logic can cause issues on Folia
            listen(new FoliaInventoryUpdatingListener());
        } else {
            SchedulerUtil.scheduleGlobalRepeating(PLUGIN, CustomInventoryListener::tickAllPaper, 1, 1);
        }

        SchedulerUtil.scheduleGlobalRepeating(PLUGIN, ChatInput::tickAll, 1, 1);
    }

    private void listen(Listener listener) {
        getPluginManager().registerEvents(listener, this);
    }

    private void registerCommand(Commands commands, CommandProvider provider){
        commands.register(
                provider.getCommandNode(),
                provider.getCommandDescription(),
                provider.getAliases()
        );
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // in case players are still vanished when the server shuts down
            if (isVanished(player)){
                toggleVanish(player);
            }
        }
    }

    public static SkinTextureProvider getTextureProvider() {
        return textureProvider;
    }

    public static void setTextureProvider(SkinTextureProvider textureProvider) {
        SModerationPaper.textureProvider = textureProvider;
    }

    private MiniMessage createMiniMessage() {
        return MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolver(TagResolver.resolver("primary", Tag.styling(style -> style.color(colors().primary()))))
                        .resolver(TagResolver.resolver("secondary", Tag.styling(style -> style.color(colors().secondary()))))
                        .resolver(TagResolver.resolver("detail", Tag.styling(style -> style.color(colors().detail()))))
                        .resolver(TagResolver.standard())
                        .build()
                )
                .build();
    }

    private void updateConfig() {
        LOGGER.info("Updating config");
        try {
            FileConfiguration config = getConfig();

            InputStream defaultConfigStream = getResource("default-config.yml");
            if (defaultConfigStream == null) {
                throw new IllegalStateException("Default config not found in JAR; could not load");
            }

            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));

            boolean changedSomething = false;
            for (String key : defaultConfig.getKeys(true)) {
                if (!config.contains(key)) {
                    // There's a new key in the default config
                    config.set(key, defaultConfig.get(key));
                    changedSomething = true;
                }

                List<String> defaultComments = new ArrayList<>(defaultConfig.getComments(key));
                List<String> comments = new ArrayList<>(config.getComments(key));
                defaultComments.removeIf(Objects::isNull);
                comments.removeIf(Objects::isNull);

                if (!defaultComments.equals(comments)) {
                    // Comments changed
                    config.setComments(key, defaultConfig.getComments(key));
                    changedSomething = true;
                }
            }

            // Save the updated configuration file
            if (changedSomething){
                LOGGER.info("Changing config file to add new options/documentation");
                saveConfig();
            }

        } catch (Exception e) {
            throw new RuntimeException("Could not update config", e);
        }
    }
}

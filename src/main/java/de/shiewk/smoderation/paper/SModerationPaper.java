package de.shiewk.smoderation.paper;

import com.google.gson.Gson;
import de.shiewk.smoderation.paper.command.*;
import de.shiewk.smoderation.paper.input.ChatInput;
import de.shiewk.smoderation.paper.input.ChatInputListener;
import de.shiewk.smoderation.paper.listener.*;
import de.shiewk.smoderation.paper.storage.PunishmentContainer;
import de.shiewk.smoderation.paper.translation.TranslatorManager;
import de.shiewk.smoderation.paper.util.SchedulerUtil;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
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

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import static de.shiewk.smoderation.paper.command.VanishCommand.isVanished;
import static de.shiewk.smoderation.paper.command.VanishCommand.toggleVanish;
import static net.kyori.adventure.text.Component.text;
import static org.bukkit.Bukkit.getPluginManager;

public final class SModerationPaper extends JavaPlugin {

    public static final Gson gson = new Gson();
    public static final PunishmentContainer container = new PunishmentContainer();
    public static ComponentLogger LOGGER = null;
    public static SModerationPaper PLUGIN = null;
    public static File SAVE_FILE = null;
    private static SkinTextureProvider textureProvider = null;

    public static final TextColor PRIMARY_COLOR = TextColor.color(212, 0, 255);
    public static final TextColor SECONDARY_COLOR = TextColor.color(52, 143, 255);
    public static final TextColor INACTIVE_COLOR = NamedTextColor.GRAY;
    public static final TextComponent CHAT_PREFIX = text("SM \u00BB ").color(PRIMARY_COLOR);

    private final TranslatorManager translatorManager = new TranslatorManager(
            Key.key("smoderation", "translations"),
            createMiniMessage(),
            "smoderation/translations/",
            new Locale[] {
                    Locale.forLanguageTag("en-US")
            }
    );

    public static FileConfiguration config() {
        return PLUGIN.getConfig();
    }

    @Override
    public void onLoad() {
        LOGGER = getComponentLogger();
        PLUGIN = this;
        SAVE_FILE = new File(this.getDataFolder().getAbsolutePath() + "/container.gz");
        LOGGER.info("Loading translations");
        translatorManager.load();
        updateConfig();
    }

    @Override
    public void onEnable() {
        listen(new PunishmentListener());
        listen(new CustomInventoryListener());
        listen(new InvSeeListener());
        listen(new EnderchestSeeListener());
        listen(new VanishListener());
        listen(new ChatInputListener());
        listen(new SocialSpyListener());

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();

            registerCommand(commands, new KickCommand());
            registerCommand(commands, new ModLogsCommand());
            registerCommand(commands, new SModCommand());
            registerCommand(commands, new InvseeCommand());
            registerCommand(commands, new EnderchestSeeCommand());
            registerCommand(commands, new SocialSpyCommand());
            registerCommand(commands, new VanishCommand());
            registerCommand(commands, new UnmuteCommand());
            registerCommand(commands, new UnbanCommand());
            registerCommand(commands, new MuteCommand());
            registerCommand(commands, new BanCommand());
            registerCommand(commands, new OfflineTPCommand());
        });

        if (SchedulerUtil.isFolia){
            // Normal ticking logic can cause issues on Folia
            listen(new FoliaInventoryUpdatingListener());
        } else {
            SchedulerUtil.scheduleGlobalRepeating(PLUGIN, CustomInventoryListener::tickAllPaper, 1, 1);
        }

        SchedulerUtil.scheduleGlobalRepeating(PLUGIN, ChatInput::tickAll, 1, 1);

        container.load(SAVE_FILE);

        LOGGER.info("Folia: {}", SchedulerUtil.isFolia ? "yes" : "no");
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
        SModerationPaper.container.save(SModerationPaper.SAVE_FILE);
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
                        .resolver(TagResolver.resolver("prefix", Tag.inserting(CHAT_PREFIX)))
                        .resolver(TagResolver.resolver("primary", Tag.styling(style -> style.color(PRIMARY_COLOR))))
                        .resolver(TagResolver.resolver("secondary", Tag.styling(style -> style.color(SECONDARY_COLOR))))
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
                if (!config.contains(key)) { // There's a new key in the default config
                    config.set(key, defaultConfig.get(key));
                    changedSomething = true;
                }
            }

            // Save the updated configuration file
            if (changedSomething) saveConfig();

        } catch (Exception e) {
            throw new RuntimeException("Could not update config", e);
        }
    }
}

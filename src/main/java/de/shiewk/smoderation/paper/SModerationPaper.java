package de.shiewk.smoderation.paper;

import de.shiewk.smoderation.paper.command.*;
import de.shiewk.smoderation.paper.config.SModerationConfig;
import de.shiewk.smoderation.paper.input.ChatInputListener;
import de.shiewk.smoderation.paper.listener.*;
import de.shiewk.smoderation.paper.storage.PunishmentContainer;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import static de.shiewk.smoderation.paper.command.VanishCommand.isVanished;
import static de.shiewk.smoderation.paper.command.VanishCommand.toggleVanish;
import static net.kyori.adventure.text.Component.text;
import static org.bukkit.Bukkit.getPluginManager;

public final class SModerationPaper extends JavaPlugin {

    public static final PunishmentContainer container = new PunishmentContainer();
    public static ComponentLogger LOGGER = null;
    public static SModerationPaper PLUGIN = null;
    public static SModerationConfig CONFIG = null;
    public static File SAVE_FILE = null;
    private static SkinTextureProvider textureProvider = null;

    public static final TextColor PRIMARY_COLOR = TextColor.color(212, 0, 255);
    public static final TextColor SECONDARY_COLOR = TextColor.color(52, 143, 255);
    public static final TextColor INACTIVE_COLOR = NamedTextColor.GRAY;
    public static final TextComponent CHAT_PREFIX = text("SM \u00BB ").color(PRIMARY_COLOR);

    @Override
    public void onLoad() {
        LOGGER = getComponentLogger();
        PLUGIN = this;
        CONFIG = new SModerationConfig(this.getConfig(), this);
        SAVE_FILE = new File(this.getDataFolder().getAbsolutePath() + "/container.gz");
    }

    @Override
    public void onEnable() {
        CONFIG.reload();

        getPluginManager().registerEvents(new PunishmentListener(), this);
        getPluginManager().registerEvents(new CustomInventoryListener(), this);
        getPluginManager().registerEvents(new InvSeeListener(), this);
        getPluginManager().registerEvents(new EnderchestSeeListener(), this);
        getPluginManager().registerEvents(new VanishListener(), this);
        getPluginManager().registerEvents(new ChatInputListener(), this);
        getPluginManager().registerEvents(new SocialSpyListener(), this);

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
        });

        container.load(SAVE_FILE);
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
}

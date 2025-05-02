package de.shiewk.smoderation.paper.config;

import de.shiewk.smoderation.paper.SModerationPaper;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class SModerationConfig {

    private final FileConfiguration config;
    private final SModerationPaper plugin;

    public SModerationConfig(FileConfiguration config, SModerationPaper plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    private List<String> socialSpyCommands = List.of(
            "w",
            "tell",
            "msg",
            "teammsg",
            "tm",
            "minecraft:w",
            "minecraft:tell",
            "minecraft:msg",
            "minecraft:teammsg",
            "minecraft:tm"
    );
    private boolean forceReason = false;

    public void reload(){
        socialSpyCommands = loadOrSetStringList("socialspy-commands", socialSpyCommands);
        forceReason = loadOrSetBoolean("force-reason", forceReason);
    }

    private List<String> loadOrSetStringList(String path, List<String> defaultValue) {
        if (!config.contains(path)){
            config.set(path, defaultValue);
            plugin.saveConfig();
        }
        return config.getStringList(path);
    }

    private boolean loadOrSetBoolean(String path, boolean defaultValue) {
        if (!config.contains(path)){
            config.set(path, defaultValue);
            plugin.saveConfig();
        }
        return config.getBoolean(path);
    }

    public List<String> getSocialSpyCommands(){
        return socialSpyCommands;
    }

    public boolean shouldForceReason(){
        return forceReason;
    }
}

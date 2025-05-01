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

    public List<String> getSocialSpyCommands(List<String> default_){
        final String path = "socialspy-commands";
        if (!config.contains(path)){
            config.set(path, default_);
            plugin.saveConfig();
        }
        return config.getStringList(path);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}

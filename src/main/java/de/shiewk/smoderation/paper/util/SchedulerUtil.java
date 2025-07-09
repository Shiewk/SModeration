package de.shiewk.smoderation.paper.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * This class provides some convenience methods that make Folia support easier
 */
public final class SchedulerUtil {
    private SchedulerUtil(){}

    public static final boolean isFolia;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        isFolia = folia;
    }

    public static void scheduleForEntity(Plugin plugin, Entity entity, Runnable task, int delayTicks){
        if (isFolia){
            entity.getScheduler().execute(plugin, task, null, delayTicks);
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task, delayTicks);
        }
    }

    public static void scheduleForEntity(Plugin plugin, Entity entity, Runnable task){
        if (isFolia){
            entity.getScheduler().run(plugin, t -> task.run(), null);
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task);
        }
    }

    public static void scheduleGlobalRepeating(Plugin plugin, Runnable task, int delay, int interval){
        if (isFolia){
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> task.run(), delay, interval);
        } else {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, task, delay, interval);
        }
    }

    public static void scheduleGlobal(Plugin plugin, Runnable task){
        if (isFolia){
            Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run());
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task);
        }
    }

}

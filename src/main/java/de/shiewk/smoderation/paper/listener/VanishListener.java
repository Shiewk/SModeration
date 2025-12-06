package de.shiewk.smoderation.paper.listener;

import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.command.VanishCommand;
import de.shiewk.smoderation.paper.util.SchedulerUtil;
import io.papermc.paper.event.entity.WardenAngerChangeEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;

import static de.shiewk.smoderation.paper.SModerationPaper.SECONDARY_COLOR;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public class VanishListener implements Listener {

    public static final Component PREFIX = text("[VANISH] ").color(SECONDARY_COLOR);

    @EventHandler(priority = EventPriority.HIGH) public void onPlayerQuit(PlayerQuitEvent event){
        final Player player = event.getPlayer();
        if (VanishCommand.isVanished(player)){
            player.getPersistentDataContainer().set(VanishCommand.KEY_VANISHED, PersistentDataType.BOOLEAN, true);
            VanishCommand.toggleVanishSilent(player, true);
            Component message = event.quitMessage();
            event.quitMessage(null);
            if (message != null){
                broadcast(message.color(null));
            }
        } else {
            player.getPersistentDataContainer().remove(VanishCommand.KEY_VANISHED);
        }
        for (Player vanishedPlayer : VanishCommand.getVanishedPlayers()) {
            // to clean up visibility status
            player.hideEntity(SModerationPaper.PLUGIN, vanishedPlayer);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR) public void onPlayerJoin(PlayerJoinEvent event){
        final Player player = event.getPlayer();
        if (player.getPersistentDataContainer().has(VanishCommand.KEY_VANISHED)){
            boolean state = VanishCommand.toggleVanishSilent(player, false);
            if (state){
                Component message = event.joinMessage();
                event.joinMessage(null);
                if (message != null){
                    broadcast(message.color(null));
                }
                SchedulerUtil.scheduleForEntity(SModerationPaper.PLUGIN, player, () -> {
                    player.sendMessage(translatable("smod.vanish.stillEnabled"));
                    player.playSound(Sound.sound(
                            Key.key("minecraft", "block.beacon.power_select"),
                            Sound.Source.MASTER,
                            1f,
                            1f
                    ), player);
                }, 20);
            } else {
                player.getPersistentDataContainer().remove(VanishCommand.KEY_VANISHED);
            }
        }
        SchedulerUtil.scheduleForEntity(SModerationPaper.PLUGIN, player, () -> {
            if (player.hasPermission("smod.vanish.see")){
                for (Player vanishedPlayer : VanishCommand.getVanishedPlayers()) {
                    // to show visible vanished players
                    player.showEntity(SModerationPaper.PLUGIN, vanishedPlayer);
                }
                VanishCommand.listVanishedPlayersTo(player);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event){
        final Component message = event.deathMessage();
        if (VanishCommand.isVanished(event.getPlayer()) && message != null){
            event.deathMessage(null);
            broadcast(message);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event){
        if (event.getTarget() instanceof Player pl && VanishCommand.isVanished(pl)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTarget(WardenAngerChangeEvent event){
        if (event.getTarget() instanceof Player pl && VanishCommand.isVanished(pl)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent event){
        Player pl = event.getPlayer();
        Component message = event.message();
        if (VanishCommand.isVanished(pl) && message != null) {
            broadcast(message);
            event.message(null);
        }
    }

    private static void broadcast(Component message) {
        Component result = PREFIX.append(message);
        Bukkit.getConsoleSender().sendMessage(result);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("smod.vanish.see")){
                onlinePlayer.sendMessage(result);
            }
        }
    }

}

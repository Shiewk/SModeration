package de.shiewk.smoderation.paper.listener;

import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.punishments.Ban;
import de.shiewk.smoderation.paper.punishments.Mute;
import de.shiewk.smoderation.paper.punishments.Punishment;
import de.shiewk.smoderation.paper.punishments.PunishmentManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

import static net.kyori.adventure.text.Component.translatable;

public class PunishmentListener implements Listener {

    private final PunishmentManager punishmentManager;

    public PunishmentListener(PunishmentManager punishmentManager) {
        this.punishmentManager = punishmentManager;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event){
        List<Punishment> list = punishmentManager.byTargetUUID(event.getUniqueId(), p -> p instanceof Ban ban && ban.isActive());
        if (!list.isEmpty()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, list.getFirst().infoMessage());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event){
        final Player player = event.getPlayer();
        List<Punishment> list = punishmentManager.byTargetUUID(player.getUniqueId(), p -> p instanceof Mute mute && mute.isActive());
        if (!list.isEmpty()) {
            event.setCancelled(true);
            player.sendMessage(list.getFirst().infoMessage().colorIfAbsent(SModerationPaper.colors().primary()));
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
        Player player = event.getPlayer();
        List<Punishment> list = punishmentManager.byTargetUUID(player.getUniqueId(), p -> p instanceof Mute mute && mute.isActive());
        if (!list.isEmpty()) { // Player is muted
            List<String> forbiddenCommands = SModerationPaper.config().getStringList("muted-forbidden-commands");
            final String message = event.getMessage();
            if (forbiddenCommands.stream().anyMatch(str ->
                    message.toLowerCase().startsWith("/"+str.toLowerCase()+" ")
                            || message.toLowerCase().startsWith(str.toLowerCase()+" ")
            )){
                Bukkit.getConsoleSender().sendMessage(player.getName() + " tried to run forbidden command while muted");
                player.sendMessage(translatable("smod.punishment.playerMessage.mute.chat", SModerationPaper.colors().primary()));
                event.setCancelled(true);
            }
        }

    }

}

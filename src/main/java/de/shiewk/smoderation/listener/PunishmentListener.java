package de.shiewk.smoderation.listener;

import de.shiewk.smoderation.SModeration;
import de.shiewk.smoderation.event.PunishmentIssueEvent;
import de.shiewk.smoderation.punishments.Punishment;
import de.shiewk.smoderation.punishments.PunishmentType;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PunishmentListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLogin(PlayerLoginEvent event){
        Punishment punishment = SModeration.container.find(p ->
                p.type == PunishmentType.BAN
                && p.to.equals(event.getPlayer().getUniqueId())
                && p.until >= System.currentTimeMillis());
        if (punishment != null){
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, punishment.playerMessage());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event){
        final Player player = event.getPlayer();
        final Punishment punishment = SModeration.container.find(p ->
                p.type == PunishmentType.MUTE
                        && p.to.equals(player.getUniqueId())
                        && p.until >= System.currentTimeMillis());
        if (punishment != null) {
            event.setCancelled(true);
            player.sendMessage(punishment.playerMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPunishmentIssue(PunishmentIssueEvent event){
        final Punishment punishment = event.getPunishment();
        switch (punishment.type){
            case KICK, BAN -> {
                final Player player = Bukkit.getPlayer(punishment.to);
                if (player != null) {
                    player.kick(punishment.playerMessage());
                }
            }
        }
    }
}

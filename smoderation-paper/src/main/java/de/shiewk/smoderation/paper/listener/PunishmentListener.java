package de.shiewk.smoderation.paper.listener;

import de.shiewk.smoderation.paper.SModerationPaper;
import de.shiewk.smoderation.paper.event.PunishmentIssueEvent;
import de.shiewk.smoderation.paper.punishments.Punishment;
import de.shiewk.smoderation.paper.punishments.PunishmentType;
import de.shiewk.smoderation.paper.storage.PunishmentContainer;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.world.WorldSaveEvent;

import static de.shiewk.smoderation.paper.SModerationPaper.CHAT_PREFIX;

public class PunishmentListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLogin(PlayerLoginEvent event){
        Punishment punishment = SModerationPaper.container.find(p ->
                p.type == PunishmentType.BAN
                && p.to.equals(event.getPlayer().getUniqueId())
                && p.isActive());
        if (punishment != null){
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, CHAT_PREFIX.append(punishment.playerMessage()));
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event){
        final Player player = event.getPlayer();
        final Punishment punishment = SModerationPaper.container.find(p ->
                p.type == PunishmentType.MUTE
                        && p.to.equals(player.getUniqueId())
                        && p.isActive());
        if (punishment != null) {
            event.setCancelled(true);
            player.sendMessage(CHAT_PREFIX.append(punishment.playerMessage()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPunishmentIssue(PunishmentIssueEvent event){
        final Punishment punishment = event.getPunishment();
        final PunishmentContainer container = event.getContainer();
        final Punishment duplicate = container.find(p -> p.to.equals(punishment.to) && p.type == punishment.type && p.isActive());
        if (duplicate != null){
            container.remove(duplicate);
            container.add(new Punishment(duplicate.type, duplicate.time, System.currentTimeMillis(), duplicate.by, duplicate.to, duplicate.reason));
        }
        switch (punishment.type){
            case KICK, BAN -> {
                final Player player = Bukkit.getPlayer(punishment.to);
                if (player != null) {
                    player.kick(CHAT_PREFIX.append(punishment.playerMessage()));
                }
            }
        }
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent event){
        if (event.getWorld().equals(Bukkit.getServer().getWorlds().get(0))){
            SModerationPaper.container.save(SModerationPaper.SAVE_FILE);
        }
    }
}

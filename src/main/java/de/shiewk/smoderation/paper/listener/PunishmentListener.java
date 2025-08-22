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
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.world.WorldSaveEvent;

import java.util.List;

import static de.shiewk.smoderation.paper.SModerationPaper.CHAT_PREFIX;
import static net.kyori.adventure.text.Component.translatable;

public class PunishmentListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event){
        // Have to use AsyncPlayerPreLoginEvent since PlayerLoginEvent is deprecated in newer versions
        // I would use the new PlayerConnectionValidateLoginEvent but there is literally no API available
        // there to get player's UUIDs
        Punishment punishment = SModerationPaper.container.find(p ->
                p.type == PunishmentType.BAN
                && p.to.equals(event.getUniqueId())
                && p.isActive());
        if (punishment != null){
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, CHAT_PREFIX.append(punishment.playerMessage()));
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

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
        Player player = event.getPlayer();
        final Punishment mute = SModerationPaper.container.find(p ->
                p.type == PunishmentType.MUTE
                        && p.to.equals(player.getUniqueId())
                        && p.isActive());

        if (mute != null) { // Player is muted
            List<String> forbiddenCommands = SModerationPaper.config().getStringList("muted-forbidden-commands");
            final String message = event.getMessage();
            if (forbiddenCommands.stream().anyMatch(str ->
                    message.toLowerCase().startsWith("/"+str.toLowerCase()+" ")
                            || message.toLowerCase().startsWith(str.toLowerCase()+" ")
            )){
                Bukkit.getConsoleSender().sendMessage(player.getName() + " tried to run forbidden command while muted");
                player.sendMessage(CHAT_PREFIX.append(translatable("smod.punishment.playerMessage.mute.chat")));
                event.setCancelled(true);
            }
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
        if (event.getWorld().equals(Bukkit.getServer().getWorlds().getFirst())){
            SModerationPaper.container.save(SModerationPaper.SAVE_FILE);
        }
    }
}

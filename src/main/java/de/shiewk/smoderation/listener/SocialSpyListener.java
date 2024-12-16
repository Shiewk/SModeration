package de.shiewk.smoderation.listener;

import de.shiewk.smoderation.SModeration;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

import static de.shiewk.smoderation.SModeration.PRIMARY_COLOR;
import static de.shiewk.smoderation.SModeration.SECONDARY_COLOR;
import static net.kyori.adventure.text.Component.text;

public class SocialSpyListener implements Listener {

    private static final List<String> defaultCommands = List.of(
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

    private static final NamespacedKey SAVE_KEY = new NamespacedKey("smoderation", "socialspy");
    private static final ObjectArrayList<CommandSender> targets = new ObjectArrayList<>();

    public static boolean toggle(CommandSender sender) {
        boolean enabledNow = isEnabled(sender);
        if (enabledNow){
            targets.remove(sender);
            if (sender instanceof Player player){
                player.getPersistentDataContainer().set(SAVE_KEY, PersistentDataType.BOOLEAN, false);
            }
            return false;
        } else {
            targets.add(sender);
            if (sender instanceof Player player){
                player.getPersistentDataContainer().set(SAVE_KEY, PersistentDataType.BOOLEAN, true);
            }
            return true;
        }
    }

    @EventHandler public void onPlayerJoin(PlayerJoinEvent event){
        final PersistentDataContainer pdc = event.getPlayer().getPersistentDataContainer();
        if (Boolean.TRUE.equals(pdc.get(SAVE_KEY, PersistentDataType.BOOLEAN))){
            targets.add(event.getPlayer());
        }
    }

    @EventHandler public void onPlayerQuit(PlayerQuitEvent event){
        targets.remove(event.getPlayer());
    }

    public static boolean isEnabled(CommandSender sender){
        return targets.contains(sender);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerSendCommand(PlayerCommandPreprocessEvent event){
        List<String> ssCommands = SModeration.CONFIG.getSocialSpyCommands(defaultCommands);
        final String message = event.getMessage();
        if (ssCommands.stream().anyMatch(str ->
                message.startsWith("/"+str+" ")
                || message.startsWith(str+" ")
        )){
            SocialSpyListener.command(event.getPlayer(), message);
        }
    }

    public static void command(Player player, String command){
        for (CommandSender target : targets) {
            target.sendMessage(text("[", PRIMARY_COLOR)
                    .append(text("SocialSpy", SECONDARY_COLOR))
                    .append(text("] "))
                    .append(player.displayName().colorIfAbsent(PRIMARY_COLOR))
                    .append(text(": ", PRIMARY_COLOR))
                    .append(text(command, SECONDARY_COLOR))
            );
        }
    }

}

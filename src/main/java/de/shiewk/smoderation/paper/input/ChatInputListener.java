package de.shiewk.smoderation.paper.input;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static de.shiewk.smoderation.paper.input.ChatInput.runningInputs;

public class ChatInputListener implements Listener {


    @EventHandler
    public void onAsyncChat(AsyncChatEvent event){
        final ChatInput input = runningInputs.remove(event.getPlayer());
        if (input != null){
            event.setCancelled(true);
            input.getAction().accept(event.message());
            event.getPlayer().clearTitle();
        }
    }

    @EventHandler public void onPlayerQuit(PlayerQuitEvent event){
        runningInputs.remove(event.getPlayer());
    }

}

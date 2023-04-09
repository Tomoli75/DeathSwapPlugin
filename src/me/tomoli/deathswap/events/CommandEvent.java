package me.tomoli.deathswap.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandEvent implements Listener {
    @EventHandler
    public void blockCommands(PlayerCommandPreprocessEvent event) {
        if(!(event.getPlayer().hasPermission("deathswap.commands") || event.getPlayer().isOp())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("no");
        }
    }
    @EventHandler
    public void blockChat(AsyncPlayerChatEvent event) {
        if(!(event.getPlayer().hasPermission("deathswap.commands") || event.getPlayer().isOp())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("no");
        }
    }
}

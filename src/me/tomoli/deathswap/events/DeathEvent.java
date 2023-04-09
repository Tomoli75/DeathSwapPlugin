package me.tomoli.deathswap.events;

import me.tomoli.deathswap.helpers.GameSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathEvent implements Listener {
    @EventHandler
    public void onPlayerDie(PlayerDeathEvent event) {
        event.getEntity().setGameMode(GameMode.SPECTATOR);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ChatColor.RED+event.getEntity().getDisplayName()+" has been eliminated!");
        }
        GameSystem.instance.checkWinCondition();
    }
}

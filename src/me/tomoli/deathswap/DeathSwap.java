package me.tomoli.deathswap;

import me.tomoli.deathswap.commands.DebugCommand;
import me.tomoli.deathswap.commands.DisableCommand;
import me.tomoli.deathswap.commands.EnableCommand;
import me.tomoli.deathswap.events.CommandEvent;
import me.tomoli.deathswap.events.DeathEvent;
import me.tomoli.deathswap.helpers.GameSystem;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class DeathSwap extends JavaPlugin {
    public static DeathSwap instance;
    @Override
    public void onEnable() {
        instance = this;
        new GameSystem();
        this.getCommand("startgame").setExecutor(new EnableCommand());
        this.getCommand("stopgame").setExecutor(new DisableCommand());
        this.getCommand("debuggame").setExecutor(new DebugCommand());
        Bukkit.getPluginManager().registerEvents(new DeathEvent(), this);
        Bukkit.getPluginManager().registerEvents(new CommandEvent(), this);
    }
}

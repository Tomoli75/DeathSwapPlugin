package me.tomoli.deathswap.commands;

import me.tomoli.deathswap.helpers.GameSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DebugCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender.hasPermission("deathswap.commands") || sender.isOp()) {
            sender.sendMessage(GameSystem.instance.debuggingData());
        } else {
            sender.sendMessage("no");
        }
        return true;
    }
}

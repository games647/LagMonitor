package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TpsHistoryCommand implements CommandExecutor {

    private final LagMonitor plugin;

    public TpsHistoryCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "TPS: " + plugin.getTpsHistoryTask().getLastTicks());
        return true;
    }
}

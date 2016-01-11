package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ThreadCommand implements CommandExecutor {

    private final LagMonitor plugin;

    public ThreadCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        for (Thread thread : allStackTraces.keySet()) {
            if (thread.getContextClassLoader() == null) {
                //ignore java system threads like reference handler
                continue;
            }

            sender.sendMessage(ChatColor.GOLD + thread.getName() + " State: " + thread.getState());
        }

        return true;
    }
}

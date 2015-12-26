package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SystemCommand implements CommandExecutor {

    private final LagMonitor plugin;

    public SystemCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.DARK_GREEN + "Free RAM: " + Runtime.getRuntime().freeMemory());
        sender.sendMessage(ChatColor.DARK_GREEN + "Threads: " + Thread.getAllStackTraces().size());
        sender.sendMessage(ChatColor.DARK_GREEN + "TPS: " + plugin.getTpsHistoryTask().getLastTicks());

        OperatingSystemMXBean operationBean = ManagementFactory.getOperatingSystemMXBean();
        sender.sendMessage(ChatColor.DARK_GREEN + "CPU Usage: " + operationBean.getSystemLoadAverage());
        return true;
    }
}

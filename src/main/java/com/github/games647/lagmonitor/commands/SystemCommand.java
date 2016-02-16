package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;

import org.bukkit.Bukkit;
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
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        String uptimeFormat = new SimpleDateFormat("HH 'hour(s)':mm 'minute(s)'").format(runtimeBean.getUptime());

        Runtime runtime = Runtime.getRuntime();
        sender.sendMessage(ChatColor.DARK_GREEN + "Uptime: " + uptimeFormat);
        sender.sendMessage(ChatColor.DARK_GREEN + "Free RAM (MB): " + convertBytesToMegaBytes(runtime.freeMemory()));
        sender.sendMessage(ChatColor.DARK_GREEN + "Threads: " + Thread.getAllStackTraces().size());
        sender.sendMessage(ChatColor.DARK_GREEN + "TPS: " + plugin.getTpsHistoryTask().getLastTicks());

        sender.sendMessage(ChatColor.DARK_GREEN + "Server version: " + Bukkit.getBukkitVersion());
        return true;
    }

    private double convertBytesToMegaBytes(long bytes) {
        return bytes / 1_000 / 1_000;
    }
}
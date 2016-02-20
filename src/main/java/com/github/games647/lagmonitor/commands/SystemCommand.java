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

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;
    private static final ChatColor SECONDARY_COLOR = ChatColor.GRAY;

    private final LagMonitor plugin;

    public SystemCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        long uptime = runtimeBean.getUptime() - 60 * 60 * 1000;
        String uptimeFormat = new SimpleDateFormat("HH 'hour' mm 'minutes' ss 'seconds'").format(uptime);

        int threadCount = ManagementFactory.getThreadMXBean().getThreadCount();

        Runtime runtime = Runtime.getRuntime();
        double maxMemoryFormatted = convertBytesToMegaBytes(runtime.totalMemory());
        double freeMemoryFormatted = convertBytesToMegaBytes(runtime.freeMemory());

        sender.sendMessage(PRIMARY_COLOR + "Uptime: " + SECONDARY_COLOR + uptimeFormat);

        sender.sendMessage(PRIMARY_COLOR + "Max RAM (MB): " + SECONDARY_COLOR + maxMemoryFormatted);
        sender.sendMessage(PRIMARY_COLOR + "Free RAM (MB): " + SECONDARY_COLOR + freeMemoryFormatted);

        sender.sendMessage(PRIMARY_COLOR + "Threads: " + SECONDARY_COLOR + threadCount);
        sender.sendMessage(PRIMARY_COLOR + "TPS: " + SECONDARY_COLOR + plugin.getTpsHistoryTask().getLastSample());

        sender.sendMessage(PRIMARY_COLOR + "Server version: " + SECONDARY_COLOR + Bukkit.getBukkitVersion());
        return true;
    }

    private double convertBytesToMegaBytes(long bytes) {
        return bytes / 1_024 / 1_024;
    }
}
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class SystemCommand implements CommandExecutor {

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;
    private static final ChatColor SECONDARY_COLOR = ChatColor.GRAY;

    private final LagMonitor plugin;

    public SystemCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        displayRuntimeInfo(sender);
        displayMinecraftInfo(sender);
        return true;
    }

    private void displayRuntimeInfo(CommandSender sender) {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        long uptime = runtimeBean.getUptime() - 60 * 60 * 1000;
        String uptimeFormat = new SimpleDateFormat("HH 'hour' mm 'minutes' ss 'seconds'").format(uptime);

        int threadCount = ManagementFactory.getThreadMXBean().getThreadCount();

        Runtime runtime = Runtime.getRuntime();
        double maxMemoryFormatted = convertBytesToMegaBytes(runtime.maxMemory());
        double freeMemoryFormatted = convertBytesToMegaBytes(runtime.maxMemory() - runtime.totalMemory());

        //runtime specific
        sender.sendMessage(PRIMARY_COLOR + "Uptime: " + SECONDARY_COLOR + uptimeFormat);

        sender.sendMessage(PRIMARY_COLOR + "Max RAM: " + SECONDARY_COLOR + maxMemoryFormatted + "MB");
        sender.sendMessage(PRIMARY_COLOR + "Free RAM: " + SECONDARY_COLOR + freeMemoryFormatted + "MB");

        sender.sendMessage(PRIMARY_COLOR + "Threads: " + SECONDARY_COLOR + threadCount);
    }

    private void displayMinecraftInfo(CommandSender sender) {
        //minecraft specific
        sender.sendMessage(PRIMARY_COLOR + "TPS: " + SECONDARY_COLOR + plugin.getTpsHistoryTask().getLastSample());

        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin[] plugins = pluginManager.getPlugins();
        sender.sendMessage(PRIMARY_COLOR + "Plugins: "
                + SECONDARY_COLOR + getEnabledPlugins(plugins) + '/' + plugins.length);

        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        sender.sendMessage(PRIMARY_COLOR + "Players: " + SECONDARY_COLOR + onlinePlayers + '/' + maxPlayers);

        sender.sendMessage(PRIMARY_COLOR + "Worlds: " + SECONDARY_COLOR + Bukkit.getWorlds().size());
        sender.sendMessage(PRIMARY_COLOR + "Server version: " + SECONDARY_COLOR + Bukkit.getVersion());
    }

    private int getEnabledPlugins(Plugin[] plugins) {
        int enabled = 0;
        for (Plugin toCheck : plugins) {
            if (toCheck.isEnabled()) {
                enabled++;
            }
        }

        return enabled;
    }

    private long convertBytesToMegaBytes(long bytes) {
        return bytes / 1_024 / 1_024;
    }
}
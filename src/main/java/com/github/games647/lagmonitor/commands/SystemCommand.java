package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.traffic.TrafficReader;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
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
        long maxMemory = runtime.maxMemory();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();

        //runtime specific
        sender.sendMessage(PRIMARY_COLOR + "Uptime: " + SECONDARY_COLOR + uptimeFormat);
        sender.sendMessage(PRIMARY_COLOR + "Arguments: " + SECONDARY_COLOR + runtimeBean.getInputArguments());

        sender.sendMessage(PRIMARY_COLOR + "Total RAM: " + SECONDARY_COLOR + readableByteCount(totalMemory, true));
        sender.sendMessage(PRIMARY_COLOR + "Max Heap RAM: " + SECONDARY_COLOR + readableByteCount(maxMemory, true));
        sender.sendMessage(PRIMARY_COLOR + "Free Heap RAM: " + SECONDARY_COLOR + readableByteCount(freeMemory, true));

        sender.sendMessage(PRIMARY_COLOR + "Threads: " + SECONDARY_COLOR + threadCount);
    }

    private void displayMinecraftInfo(CommandSender sender) {
        //minecraft specific
        sender.sendMessage(PRIMARY_COLOR + "TPS: " + SECONDARY_COLOR + plugin.getTpsHistoryTask().getLastSample());

        TrafficReader trafficReader = plugin.getTrafficReader();
        if (trafficReader != null) {
            String formattedIncoming = readableByteCount(trafficReader.getIncomingBytes().get(), true);
            String formattedOutgoing = readableByteCount(trafficReader.getOutgoingBytes().get(), true);
            sender.sendMessage(PRIMARY_COLOR + "Incoming Traffic: " + SECONDARY_COLOR + formattedIncoming);
            sender.sendMessage(PRIMARY_COLOR + "Outgoing Traffic: " + SECONDARY_COLOR + formattedOutgoing);
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin[] plugins = pluginManager.getPlugins();
        sender.sendMessage(PRIMARY_COLOR + "Loaded Plugins: "
                + SECONDARY_COLOR + getEnabledPlugins(plugins) + '/' + plugins.length);

        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        sender.sendMessage(PRIMARY_COLOR + "Players: " + SECONDARY_COLOR + onlinePlayers + '/' + maxPlayers);

        displayWorldInfo(sender);
        sender.sendMessage(PRIMARY_COLOR + "Server version: " + SECONDARY_COLOR + Bukkit.getVersion());
    }

    private void displayWorldInfo(CommandSender sender) {
        List<World> worlds = Bukkit.getWorlds();
        int entities = 0;
        int chunks = 0;
        int livingEntities = 0;

        long usedWorldSize = 0;

        for (World world : worlds) {
            livingEntities += world.getLivingEntities().size();
            entities += world.getEntities().size();
            chunks += world.getLoadedChunks().length;

            File worldFolder = Bukkit.getWorld(world.getUID()).getWorldFolder();
            usedWorldSize += getFolderSize(worldFolder);
        }

        sender.sendMessage(PRIMARY_COLOR + "Entities: " + SECONDARY_COLOR + livingEntities + '/' + entities);
        sender.sendMessage(PRIMARY_COLOR + "Loaded chunks: " + SECONDARY_COLOR + chunks);
        sender.sendMessage(PRIMARY_COLOR + "Worlds: " + SECONDARY_COLOR + Bukkit.getWorlds().size());
        sender.sendMessage(PRIMARY_COLOR + "World size: " + SECONDARY_COLOR + readableByteCount(usedWorldSize, true));
    }

    private long getFolderSize(File folder) {
        long size = 0;

        for (File file : folder.listFiles()) {
            if (file == null) {
                continue;
            }

            if (file.isFile()) {
                size += file.length();
            } else {
                size += getFolderSize(file);
            }
        }

        return size;
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

    private String readableByteCount(long bytes, boolean si) {
        //https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }

        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
    }
}

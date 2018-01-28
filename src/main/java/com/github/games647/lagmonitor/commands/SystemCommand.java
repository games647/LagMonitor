package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.LagUtils;
import com.github.games647.lagmonitor.traffic.TrafficReader;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import static com.github.games647.lagmonitor.LagUtils.readableByteCount;

public class SystemCommand extends LagCommand {

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;
    private static final ChatColor SECONDARY_COLOR = ChatColor.GRAY;

    public SystemCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, command)) {
            sender.sendMessage(org.bukkit.ChatColor.DARK_RED + "Not whitelisted");
            return true;
        }

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

        // runtime specific
        sender.sendMessage(PRIMARY_COLOR + "Uptime: " + SECONDARY_COLOR + uptimeFormat);
        sender.sendMessage(PRIMARY_COLOR + "Arguments: " + SECONDARY_COLOR + runtimeBean.getInputArguments());


        sender.sendMessage(PRIMARY_COLOR + "Max Heap RAM: " + SECONDARY_COLOR + readableByteCount(maxMemory));
        sender.sendMessage(PRIMARY_COLOR + "Total RAM: " + SECONDARY_COLOR + readableByteCount(totalMemory));
        sender.sendMessage(PRIMARY_COLOR + "Free Heap RAM: " + SECONDARY_COLOR + readableByteCount(freeMemory));

        sender.sendMessage(PRIMARY_COLOR + "Threads: " + SECONDARY_COLOR + threadCount);
    }

    private void displayMinecraftInfo(CommandSender sender) {
        //minecraft specific
        sender.sendMessage(PRIMARY_COLOR + "TPS: " + SECONDARY_COLOR + plugin.getTpsHistoryTask().getLastSample());

        TrafficReader trafficReader = plugin.getTrafficReader();
        if (trafficReader != null) {
            String formattedIncoming = readableByteCount(trafficReader.getIncomingBytes().get());
            String formattedOutgoing = readableByteCount(trafficReader.getOutgoingBytes().get());
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
        int tileEntities = 0;

        long usedWorldSize = 0;

        for (World world : worlds) {
            for (Chunk loadedChunk : world.getLoadedChunks()) {
                tileEntities += loadedChunk.getTileEntities().length;
            }

            livingEntities += world.getLivingEntities().size();
            entities += world.getEntities().size();
            chunks += world.getLoadedChunks().length;

            File worldFolder = Bukkit.getWorld(world.getUID()).getWorldFolder();
            usedWorldSize += LagUtils.getFolderSize(plugin.getLogger(), worldFolder.toPath());
        }

        sender.sendMessage(PRIMARY_COLOR + "Entities: " + SECONDARY_COLOR + livingEntities + '/' + entities);
        sender.sendMessage(PRIMARY_COLOR + "Tile Entities: " + SECONDARY_COLOR + tileEntities);
        sender.sendMessage(PRIMARY_COLOR + "Loaded Chunks: " + SECONDARY_COLOR + chunks);
        sender.sendMessage(PRIMARY_COLOR + "Worlds: " + SECONDARY_COLOR + Bukkit.getWorlds().size());
        sender.sendMessage(PRIMARY_COLOR + "World Size: " + SECONDARY_COLOR + readableByteCount(usedWorldSize));
    }

    private int getEnabledPlugins(Plugin[] plugins) {
        return (int) Stream.of(plugins).filter(Plugin::isEnabled).count();
    }
}

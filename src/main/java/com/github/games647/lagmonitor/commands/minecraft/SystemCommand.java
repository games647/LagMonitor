package com.github.games647.lagmonitor.commands.minecraft;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.LagUtils;
import com.github.games647.lagmonitor.commands.LagCommand;
import com.github.games647.lagmonitor.traffic.TrafficReader;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import static com.github.games647.lagmonitor.LagUtils.readableBytes;

public class SystemCommand extends LagCommand {

    public SystemCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, command)) {
            sendError(sender, "Not whitelisted");
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
        sendMessage(sender, "Uptime", uptimeFormat);
        sendMessage(sender, "Arguments", runtimeBean.getInputArguments().toString());

        sendMessage(sender, "Max Heap RAM", readableBytes(maxMemory));
        sendMessage(sender, "Total RAM", readableBytes(totalMemory));
        sendMessage(sender, "Free Heap RAM", readableBytes(freeMemory));

        sendMessage(sender, "Threads", String.valueOf(threadCount));
    }

    private void displayMinecraftInfo(CommandSender sender) {
        //Minecraft specific
        sendMessage(sender, "TPS", String.valueOf(plugin.getTpsHistoryTask().getLastSample()));

        TrafficReader trafficReader = plugin.getTrafficReader();
        if (trafficReader != null) {
            String formattedIncoming = readableBytes(trafficReader.getIncomingBytes().get());
            String formattedOutgoing = readableBytes(trafficReader.getOutgoingBytes().get());
            sendMessage(sender, "Incoming Traffic", formattedIncoming);
            sendMessage(sender, "Outgoing Traffic", formattedOutgoing);
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin[] plugins = pluginManager.getPlugins();
        sendMessage(sender, "Loaded Plugins", String.valueOf(getEnabledPlugins(plugins) + '/' + plugins.length));

        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        sendMessage(sender, "Players", String.valueOf(onlinePlayers + '/' + maxPlayers));

        displayWorldInfo(sender);
        sendMessage(sender, "Server version", Bukkit.getVersion());
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

        sendMessage(sender, "Entities", String.valueOf(livingEntities + '/' + entities));
        sendMessage(sender, "Tile Entities", String.valueOf(tileEntities));
        sendMessage(sender, "Loaded Chunks", String.valueOf(chunks));
        sendMessage(sender, "Worlds", String.valueOf(Bukkit.getWorlds().size()));
        sendMessage(sender, "World Size", readableBytes(usedWorldSize));
    }

    private int getEnabledPlugins(Plugin[] plugins) {
        return (int) Stream.of(plugins).filter(Plugin::isEnabled).count();
    }
}

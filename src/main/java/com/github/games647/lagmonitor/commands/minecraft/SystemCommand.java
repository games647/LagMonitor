package com.github.games647.lagmonitor.commands.minecraft;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.commands.LagCommand;
import com.github.games647.lagmonitor.traffic.TrafficReader;
import com.github.games647.lagmonitor.utils.LagUtils;
import com.google.common.base.StandardSystemProperty;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import oshi.software.os.OSProcess;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import static com.github.games647.lagmonitor.utils.LagUtils.readableBytes;

public class SystemCommand extends LagCommand {

    public SystemCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canExecute(sender, command)) {
            return true;
        }

        displayRuntimeInfo(sender, ManagementFactory.getRuntimeMXBean());
        displayThreadInfo(sender, ManagementFactory.getThreadMXBean());
        displayProcessInfo(sender);
        displayUserInfo(sender);
        displayMinecraftInfo(sender);
        return true;
    }

    private void displayUserInfo(CommandSender sender) {
        sender.sendMessage(PRIMARY_COLOR + "User");

        sendMessage(sender, "    Timezone", System.getProperty("user.timezone", "Unknown"));
        sendMessage(sender, "    Country", System.getProperty("user.country", "Unknown"));
        sendMessage(sender, "    Language", System.getProperty("user.language", "Unknown"));
        sendMessage(sender, "    Home", StandardSystemProperty.USER_HOME.value());
        sendMessage(sender, "    Name", StandardSystemProperty.USER_NAME.value());
    }

    private void displayProcessInfo(CommandSender sender) {
        sender.sendMessage(PRIMARY_COLOR + "Process:");

        Optional<OSProcess> optProcess = plugin.getNativeData().getProcess();
        if (optProcess.isPresent()) {
            OSProcess process = optProcess.get();

            sendMessage(sender, "    PID", String.valueOf(process.getProcessID()));
            sendMessage(sender, "    Name", process.getName());
            sendMessage(sender, "    Path", process.getPath());
            sendMessage(sender, "    Working directory", process.getCurrentWorkingDirectory());
            sendMessage(sender, "    User", process.getUser());
            sendMessage(sender, "    Group", process.getGroup());
        } else {
            sendError(sender, NATIVE_NOT_FOUND);
        }
    }

    private void displayRuntimeInfo(CommandSender sender, RuntimeMXBean runtimeBean) {
        long uptime = runtimeBean.getUptime();
        String uptimeFormat = DurationFormatUtils.formatDurationWords(uptime, false, false);

        displayMemoryInfo(sender, Runtime.getRuntime());

        // runtime specific
        sendMessage(sender, "Uptime", uptimeFormat);
        sendMessage(sender, "Arguments", runtimeBean.getInputArguments().toString());
        sendMessage(sender, "Classpath", runtimeBean.getClassPath());
        sendMessage(sender, "Library path", runtimeBean.getLibraryPath());
    }

    private void displayThreadInfo(CommandSender sender, ThreadMXBean threadBean) {
        sendMessage(sender, "Threads", String.valueOf(threadBean.getThreadCount()));
        sendMessage(sender, "Peak threads", String.valueOf(threadBean.getPeakThreadCount()));
        sendMessage(sender, "Daemon threads", String.valueOf(threadBean.getDaemonThreadCount()));
        sendMessage(sender, "Total started threads", String.valueOf(threadBean.getTotalStartedThreadCount()));
    }

    private void displayMemoryInfo(CommandSender sender, Runtime runtime) {
        long maxMemory = runtime.maxMemory();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();

        sendMessage(sender, "Reserved used RAM", readableBytes(totalMemory - freeMemory));
        sendMessage(sender, "Reserved free RAM", readableBytes(freeMemory));
        sendMessage(sender, "Reserved RAM", readableBytes(totalMemory));
        sendMessage(sender, "Max RAM", readableBytes(maxMemory));
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

        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        sendMessage(sender, "Loaded Plugins", String.format("%d/%d", getEnabledPlugins(plugins), plugins.length));

        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        sendMessage(sender, "Players", String.format("%d/%d", onlinePlayers, maxPlayers));

        displayWorldInfo(sender);
        sendMessage(sender, "Server version", Bukkit.getVersion());
    }

    private void displayWorldInfo(CommandSender sender) {
        int entities = 0;
        int chunks = 0;
        int livingEntities = 0;
        int tileEntities = 0;

        long usedWorldSize = 0;

        List<World> worlds = Bukkit.getWorlds();
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

        sendMessage(sender, "Entities", String.format("%d/%d", livingEntities, entities));
        sendMessage(sender, "Tile Entities", String.valueOf(tileEntities));
        sendMessage(sender, "Loaded Chunks", String.valueOf(chunks));
        sendMessage(sender, "Worlds", String.valueOf(worlds.size()));
        sendMessage(sender, "World Size", readableBytes(usedWorldSize));
    }

    private int getEnabledPlugins(Plugin[] plugins) {
        return (int) Stream.of(plugins).filter(Plugin::isEnabled).count();
    }
}

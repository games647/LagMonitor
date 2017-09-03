package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.NativeData;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jsoftbiz.utils.OS;

public class EnvironmentCommand implements CommandExecutor {

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;
    private static final ChatColor SECONDARY_COLOR = ChatColor.GRAY;

    private final LagMonitor plugin;

    public EnvironmentCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.isAllowed(sender, command)) {
            sender.sendMessage(ChatColor.DARK_RED + "Not whitelisted");
            return true;
        }

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        //os general info
        sender.sendMessage(PRIMARY_COLOR + "OS Name: " + SECONDARY_COLOR + osBean.getName());
        sender.sendMessage(PRIMARY_COLOR + "Platform name: " + SECONDARY_COLOR + OS.getOs().getPlatformName());

        sender.sendMessage(PRIMARY_COLOR + "OS Version: " + SECONDARY_COLOR + osBean.getVersion());
        sender.sendMessage(PRIMARY_COLOR + "OS Arch: " + SECONDARY_COLOR + osBean.getArch());

        //CPU
        sender.sendMessage(PRIMARY_COLOR + "Cores: " + SECONDARY_COLOR + osBean.getAvailableProcessors());
        sender.sendMessage(PRIMARY_COLOR + "CPU: " + SECONDARY_COLOR + System.getenv("PROCESSOR_IDENTIFIER"));
        sender.sendMessage(PRIMARY_COLOR + "Load Average: " + SECONDARY_COLOR + osBean.getSystemLoadAverage());
        printExtendOsInfo(sender);

        displayDiskSpace(sender);
        return true;
    }

    private void printExtendOsInfo(CommandSender sender) {
        NativeData nativeData = plugin.getNativeData();

        //cpu
        double systemCpuLoad = nativeData.getCPULoad();
        double processCpuLoad = nativeData.getProcessCPULoad();

        //these numbers are in percent (0.01 -> 1%)
        //we want to to have four places in a human readable percent value to multiple it with 100
        DecimalFormat decimalFormat = new DecimalFormat("###.#### %");
        decimalFormat.setMultiplier(100);
        String systemLoadFormat = decimalFormat.format(systemCpuLoad);
        String processLoadFormat = decimalFormat.format(processCpuLoad);

        sender.sendMessage(PRIMARY_COLOR + "System Usage: " + SECONDARY_COLOR + systemLoadFormat);
        sender.sendMessage(PRIMARY_COLOR + "Process Usage: " + SECONDARY_COLOR + processLoadFormat);

        //swap
        long totalSwap = nativeData.getTotalSwap();
        long freeSwap = nativeData.getFreeSwap();
        sender.sendMessage(PRIMARY_COLOR + "Total Swap: " + SECONDARY_COLOR + readableByteCount(totalSwap));
        sender.sendMessage(PRIMARY_COLOR + "Free Swap: " + SECONDARY_COLOR + readableByteCount(freeSwap));

        //RAM
        long totalMemory = nativeData.getTotalMemory();
        long freeMemory = nativeData.getFreeMemory();
        sender.sendMessage(PRIMARY_COLOR + "Total OS RAM: " + SECONDARY_COLOR + readableByteCount(totalMemory));
        sender.sendMessage(PRIMARY_COLOR + "Free OS RAM: " + SECONDARY_COLOR + readableByteCount(freeMemory));
    }

    private void displayDiskSpace(CommandSender sender) {
        long freeSpace = 0;
        long totalSpace = 0;
        try {
            FileStore fileStore = Files.getFileStore(Paths.get("."));
            freeSpace = fileStore.getUsableSpace();
            totalSpace = fileStore.getTotalSpace();
        } catch (IOException ioEx) {
            plugin.getLogger().log(Level.WARNING, "Cannot calculate free/total disk space", ioEx);
        }

        //Disk info
        sender.sendMessage(PRIMARY_COLOR + "Disk Size: " + SECONDARY_COLOR + readableByteCount(totalSpace));
        sender.sendMessage(PRIMARY_COLOR + "Free Space: " + SECONDARY_COLOR + readableByteCount(freeSpace));
    }

    private String readableByteCount(long bytes) {
        //https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
        int unit = 1024;
        if (bytes < unit) {
            return bytes + " B";
        }

        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "i";
        return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
    }
}

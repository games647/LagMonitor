package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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
        sender.sendMessage(PRIMARY_COLOR + "OS Version: " + SECONDARY_COLOR + osBean.getVersion());
        sender.sendMessage(PRIMARY_COLOR + "OS Arch: " + SECONDARY_COLOR + osBean.getArch());

        //CPU
        sender.sendMessage(PRIMARY_COLOR + "Cores: " + SECONDARY_COLOR + osBean.getAvailableProcessors());
        sender.sendMessage(PRIMARY_COLOR + "CPU: " + SECONDARY_COLOR + System.getenv("PROCESSOR_IDENTIFIER"));
        sender.sendMessage(PRIMARY_COLOR + "Load Average: " + SECONDARY_COLOR + osBean.getSystemLoadAverage());
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            printExtendOsInfo((com.sun.management.OperatingSystemMXBean) osBean, sender);
        }

        displayDiskSpace(sender);
        return true;
    }

    private void printExtendOsInfo(com.sun.management.OperatingSystemMXBean sunOsBean, CommandSender sender) {
        //cpu
        double systemCpuLoad = sunOsBean.getSystemCpuLoad();
        double processCpuLoad = sunOsBean.getProcessCpuLoad();

        //these numbers are in percent (0.01 -> 1%)
        //we want to to have four places in a human readable percent value to multiple it wiht 100
        DecimalFormat decimalFormat = new DecimalFormat("###.#### %");
        decimalFormat.setMultiplier(100);
        String systemLoadFormat = decimalFormat.format(systemCpuLoad);
        String processLoadFormat = decimalFormat.format(processCpuLoad);

        sender.sendMessage(PRIMARY_COLOR + "System Usage: " + SECONDARY_COLOR + systemLoadFormat);
        sender.sendMessage(PRIMARY_COLOR + "Process Usage: " + SECONDARY_COLOR + processLoadFormat);

        //swap
        long totalSwap = sunOsBean.getTotalSwapSpaceSize();
        long freeSwap = sunOsBean.getFreeSwapSpaceSize();
        sender.sendMessage(PRIMARY_COLOR + "Total Swap: " + SECONDARY_COLOR + reabableByteCount(totalSwap, true));
        sender.sendMessage(PRIMARY_COLOR + "Free Swap: " + SECONDARY_COLOR + reabableByteCount(freeSwap, true));

        //RAM
        long totalMemory = sunOsBean.getTotalPhysicalMemorySize();
        long freeMemory = sunOsBean.getFreePhysicalMemorySize();
        sender.sendMessage(PRIMARY_COLOR + "Total OS RAM: " + SECONDARY_COLOR + reabableByteCount(totalMemory, true));
        sender.sendMessage(PRIMARY_COLOR + "Free OS RAM: " + SECONDARY_COLOR + reabableByteCount(freeMemory, true));
    }

    private void displayDiskSpace(CommandSender sender) {
        File[] listRoots = File.listRoots();
        long totalSpace = 0;
        long freeSpace = 0;
        for (File rootFile : listRoots) {
            freeSpace += rootFile.getFreeSpace();
            totalSpace += rootFile.getTotalSpace();
        }

        //Disk info
        sender.sendMessage(PRIMARY_COLOR + "Disk Size: " + SECONDARY_COLOR + reabableByteCount(totalSpace, true));
        sender.sendMessage(PRIMARY_COLOR + "Free Space: " + SECONDARY_COLOR + reabableByteCount(freeSpace, true));
    }

    private String reabableByteCount(long bytes, boolean si) {
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

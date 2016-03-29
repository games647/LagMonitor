package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
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
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        //os general info
        sender.sendMessage(PRIMARY_COLOR + "OS Name: " + SECONDARY_COLOR + osBean.getName());
        sender.sendMessage(PRIMARY_COLOR + "OS Version: " + SECONDARY_COLOR + osBean.getVersion());
        sender.sendMessage(PRIMARY_COLOR + "OS Arch: " + SECONDARY_COLOR + osBean.getArch());

        //java info
        sender.sendMessage(PRIMARY_COLOR + "Java VM: " + SECONDARY_COLOR + runtimeBean.getVmName());
        sender.sendMessage(PRIMARY_COLOR + "Java Version: " + SECONDARY_COLOR + System.getProperty("java.version"));
        sender.sendMessage(PRIMARY_COLOR + "Java Vendor: " + SECONDARY_COLOR
                + runtimeBean.getVmVendor() + ' ' + runtimeBean.getVmVersion());

        //CPU
        sender.sendMessage(PRIMARY_COLOR + "Cores: " + SECONDARY_COLOR + osBean.getAvailableProcessors());
        sender.sendMessage(PRIMARY_COLOR + "CPU: " + SECONDARY_COLOR + System.getenv("PROCESSOR_IDENTIFIER"));
        sender.sendMessage(PRIMARY_COLOR + "Load: " + SECONDARY_COLOR + osBean.getSystemLoadAverage());
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;

            //these numbers are in percent (0.01 -> 1%)
            //we want to to have four places in a human readable percent value to multiple it wiht 100
            DecimalFormat decimalFormat = new DecimalFormat("###.#### %");
            decimalFormat.setMultiplier(100);
            double systemCpuLoad = sunOsBean.getSystemCpuLoad();
            double processCpuLoad = sunOsBean.getProcessCpuLoad();
            String systemLoadFormat = decimalFormat.format(systemCpuLoad);
            String processLoadFormat = decimalFormat.format(processCpuLoad);

            sender.sendMessage(PRIMARY_COLOR + "System usage: " + SECONDARY_COLOR + systemLoadFormat);
            sender.sendMessage(PRIMARY_COLOR + "Process usage: " + SECONDARY_COLOR + processLoadFormat);

            //RAM
            //include swap memory?
            long totalMemorySize = sunOsBean.getTotalPhysicalMemorySize();
            long freeMemorySize = sunOsBean.getFreePhysicalMemorySize();
            int totalRamFormatted = convertBytesToMega(totalMemorySize);
            sender.sendMessage(PRIMARY_COLOR + "Total OS RAM: " + SECONDARY_COLOR + totalRamFormatted + "MB");
            int freeRamFormatted = convertBytesToMega(freeMemorySize);
            sender.sendMessage(PRIMARY_COLOR + "Free OS RAM: " + SECONDARY_COLOR + freeRamFormatted + "MB");
        }

        displayDiskSpace(sender);
        return true;
    }

    private void displayDiskSpace(CommandSender sender) {
        File[] listRoots = File.listRoots();
        long totalSpace = 0;
        long freeSpace = 0;
        for (File rootFile : listRoots) {
            freeSpace += rootFile.getUsableSpace();
            totalSpace += rootFile.getTotalSpace();
        }

        //Disk info
        sender.sendMessage(PRIMARY_COLOR + "Disk size: " + SECONDARY_COLOR + humanReadableByteCount(totalSpace, true));
        sender.sendMessage(PRIMARY_COLOR + "Free space: " + SECONDARY_COLOR + humanReadableByteCount(freeSpace, true));
    }

    private int convertBytesToMega(long bytes) {
        return (int) (bytes / 1_024 / 1_024);
    }

    private String humanReadableByteCount(long bytes, boolean si) {
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

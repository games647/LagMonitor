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

    private final LagMonitor plugin;

    public EnvironmentCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        //os general info
        sender.sendMessage(ChatColor.DARK_GREEN + "OS Name: " + osBean.getName());
        sender.sendMessage(ChatColor.DARK_GREEN + "OS Version: " + osBean.getVersion());
        sender.sendMessage(ChatColor.DARK_GREEN + "OS Arch: " + osBean.getArch());

        //java info
        sender.sendMessage(ChatColor.DARK_GREEN + "Java VM: " + runtimeBean.getVmName());
        sender.sendMessage(ChatColor.DARK_GREEN + "Java Version: " + runtimeBean.getVmVersion());
        sender.sendMessage(ChatColor.DARK_GREEN + "Java Vendor: " + runtimeBean.getVmVendor());

        //CPU
        sender.sendMessage(ChatColor.DARK_GREEN + "Cores: " + osBean.getAvailableProcessors());
        sender.sendMessage(ChatColor.DARK_GREEN + "CPU Name: " + System.getenv("PROCESSOR_IDENTIFIER"));
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;

            //these numbers are in percent (0.01 -> 1%)
            //we want to to have four places in a human readable percent value to multiple it wiht 100
            DecimalFormat decimalFormat = new DecimalFormat("###.####%");
            decimalFormat.setMultiplier(100);
            double systemCpuLoad = sunOsBean.getSystemCpuLoad();
            double processCpuLoad = sunOsBean.getProcessCpuLoad();

            sender.sendMessage(ChatColor.DARK_GREEN + "System load: " + decimalFormat.format(systemCpuLoad));
            sender.sendMessage(ChatColor.DARK_GREEN + "Process load: " + decimalFormat.format(processCpuLoad));

            //planned todo -> use sigar API?
//            sender.sendMessage(ChatColor.DARK_GREEN + "CPU Speed: " + osBean.getName());
//            sender.sendMessage(ChatColor.DARK_GREEN + "CPU Physical: " + osBean.getName());
//            sender.sendMessage(ChatColor.DARK_GREEN + "CPU Logical: " + osBean.getName());

            //RAM
            //include swap memory?
            long totalMemorySize = sunOsBean.getTotalPhysicalMemorySize();
            long freeMemorySize = sunOsBean.getFreePhysicalMemorySize();
            sender.sendMessage(ChatColor.DARK_GREEN + "Total OS RAM: " + convertBytesToMegaBytes(totalMemorySize));
            sender.sendMessage(ChatColor.DARK_GREEN + "Free OS RAM: " + convertBytesToMegaBytes(freeMemorySize));
            //planned todo -> use sigar API
//            sender.sendMessage(ChatColor.DARK_GREEN + "RAM speed: " + osBean.getName());
        }

        File[] listRoots = File.listRoots();
        long totalSpace = 0;
        long freeSpace = 0;
        for (File rootFile : listRoots) {
            freeSpace += rootFile.getUsableSpace();
            totalSpace += rootFile.getTotalSpace();
        }

        //Disk info
        sender.sendMessage(ChatColor.DARK_GREEN + "Disk size: " + humanReadableByteCount(totalSpace, true));
        sender.sendMessage(ChatColor.DARK_GREEN + "Free space: " + humanReadableByteCount(freeSpace, true));
        return true;
    }

    private double convertBytesToMegaBytes(long bytes) {
        return bytes / 1_000 / 1_000;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
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

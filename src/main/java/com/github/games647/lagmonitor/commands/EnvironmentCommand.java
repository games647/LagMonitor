package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

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

        sender.sendMessage(ChatColor.DARK_GREEN + "OS Name: " + osBean.getName());
        sender.sendMessage(ChatColor.DARK_GREEN + "OS Version: " + osBean.getVersion());
        sender.sendMessage(ChatColor.DARK_GREEN + "OS Arch: " + osBean.getArch());

        sender.sendMessage(ChatColor.DARK_GREEN + "Java VM: " + runtimeBean.getVmName());
        sender.sendMessage(ChatColor.DARK_GREEN + "Java Version: " + runtimeBean.getVmVersion());
        sender.sendMessage(ChatColor.DARK_GREEN + "Java Vendor: " + runtimeBean.getVmVendor());

//include swap mememore?
//        sender.sendMessage(ChatColor.DARK_GREEN + "Used OS RAM: " + osBean.getName());
//        sender.sendMessage(ChatColor.DARK_GREEN + "Total OS RAM: " + osBean.getName());
//        sender.sendMessage(ChatColor.DARK_GREEN + "RAM speed: " + osBean.getName());
        sender.sendMessage(ChatColor.DARK_GREEN + "CPU Usage: " + osBean.getSystemLoadAverage());
        sender.sendMessage(ChatColor.DARK_GREEN + "Cores: " + osBean.getAvailableProcessors());
        sender.sendMessage(ChatColor.DARK_GREEN + "CPU Name: " + System.getenv("PROCESSOR_IDENTIFIER"));
//        sender.sendMessage(ChatColor.DARK_GREEN + "CPU Speed: " + osBean.getName());
//        sender.sendMessage(ChatColor.DARK_GREEN + "CPU Physical: " + osBean.getName());
//        sender.sendMessage(ChatColor.DARK_GREEN + "CPU Logical: " + osBean.getName());

        File[] listRoots = File.listRoots();
        long totalSpace = 0;
        long freeSpace = 0;
        for (File rootFile : listRoots) {
            freeSpace += rootFile.getUsableSpace();
            totalSpace += rootFile.getTotalSpace();
        }

        sender.sendMessage(ChatColor.DARK_GREEN + "Disk size: " + humanReadableByteCount(totalSpace, true));
        sender.sendMessage(ChatColor.DARK_GREEN + "Free space: " + humanReadableByteCount(freeSpace, true));
        return true;
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

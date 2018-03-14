package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.NativeData;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import oshi.SystemInfo;

import static com.github.games647.lagmonitor.LagUtils.readableBytes;

public class EnvironmentCommand extends LagCommand {

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;
    private static final ChatColor SECONDARY_COLOR = ChatColor.GRAY;

    public EnvironmentCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, command)) {
            sender.sendMessage(ChatColor.DARK_RED + "Not whitelisted");
            return true;
        }

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        //os general info
        sender.sendMessage(PRIMARY_COLOR + "OS Name: " + SECONDARY_COLOR + osBean.getName());

        SystemInfo systemInfo = plugin.getNativeData().getSystemInfo();
        if (systemInfo != null) {
            String codeName = systemInfo.getOperatingSystem().getVersion().getCodeName();
            System.out.println(systemInfo.getOperatingSystem().getVersion());
            sender.sendMessage(PRIMARY_COLOR + "Platform name: " + SECONDARY_COLOR + codeName);
        }

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
        sender.sendMessage(PRIMARY_COLOR + "Total Swap: " + SECONDARY_COLOR + readableBytes(totalSwap));
        sender.sendMessage(PRIMARY_COLOR + "Free Swap: " + SECONDARY_COLOR + readableBytes(freeSwap));

        //RAM
        long totalMemory = nativeData.getTotalMemory();
        long freeMemory = nativeData.getFreeMemory();
        sender.sendMessage(PRIMARY_COLOR + "Total OS RAM: " + SECONDARY_COLOR + readableBytes(totalMemory));
        sender.sendMessage(PRIMARY_COLOR + "Free OS RAM: " + SECONDARY_COLOR + readableBytes(freeMemory));
    }

    private void displayDiskSpace(CommandSender sender) {
        long freeSpace = plugin.getNativeData().getFreeSpace();
        long totalSpace = plugin.getNativeData().getTotalSpace();

        //Disk info
        sender.sendMessage(PRIMARY_COLOR + "Disk Size: " + SECONDARY_COLOR + readableBytes(totalSpace));
        sender.sendMessage(PRIMARY_COLOR + "Free Space: " + SECONDARY_COLOR + readableBytes(freeSpace));
    }
}

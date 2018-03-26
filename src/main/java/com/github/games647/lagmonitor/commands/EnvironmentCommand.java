package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.NativeData;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import oshi.SystemInfo;

import static com.github.games647.lagmonitor.LagUtils.readableBytes;

public class EnvironmentCommand extends LagCommand {

    public EnvironmentCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, command)) {
            sendError(sender, "Not whitelisted");
            return true;
        }

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        //os general info
        sendMessage(sender, "OS Name", osBean.getName());

        SystemInfo systemInfo = plugin.getNativeData().getSystemInfo();
        String family = systemInfo.getOperatingSystem().getFamily();
        sendMessage(sender, "Platform name", family);

        sendMessage(sender, "OS Version", osBean.getVersion());
        sendMessage(sender, "OS Arch", osBean.getArch());

        //CPU
        sendMessage(sender, "Cores", String.valueOf(osBean.getAvailableProcessors()));
        sendMessage(sender, "CPU", System.getenv("PROCESSOR_IDENTIFIER"));
        sendMessage(sender, "Load Average", String.valueOf(osBean.getSystemLoadAverage()));
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

        sendMessage(sender,"System Usage", systemLoadFormat);
        sendMessage(sender,"Process Usage", processLoadFormat);

        //swap
        long totalSwap = nativeData.getTotalSwap();
        long freeSwap = nativeData.getFreeSwap();
        sendMessage(sender, "Total Swap", readableBytes(totalSwap));
        sendMessage(sender, "Free Swap", readableBytes(freeSwap));

        //RAM
        long totalMemory = nativeData.getTotalMemory();
        long freeMemory = nativeData.getFreeMemory();
        sendMessage(sender, "Total OS RAM", readableBytes(totalMemory));
        sendMessage(sender, "Free OS RAM", readableBytes(freeMemory));
    }

    private void displayDiskSpace(CommandSender sender) {
        long freeSpace = plugin.getNativeData().getFreeSpace();
        long totalSpace = plugin.getNativeData().getTotalSpace();

        //Disk info
        sendMessage(sender,"Disk Size", readableBytes(totalSpace));
        sendMessage(sender,"Free Space", readableBytes(freeSpace));
    }
}

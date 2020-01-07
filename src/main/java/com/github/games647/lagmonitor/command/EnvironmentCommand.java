package com.github.games647.lagmonitor.command;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.NativeManager;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;
import java.util.Map.Entry;
import java.util.Optional;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.ProcessorIdentifier;
import oshi.software.os.OperatingSystem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import static com.github.games647.lagmonitor.util.LagUtils.readableBytes;

public class EnvironmentCommand extends LagCommand {

    public EnvironmentCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canExecute(sender, command)) {
            return true;
        }

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        //os general info
        sendMessage(sender, "OS Name", osBean.getName());
        sendMessage(sender, "OS Arch", osBean.getArch());

        Optional<SystemInfo> optInfo = plugin.getNativeData().getSystemInfo();
        if (optInfo.isPresent()) {
            SystemInfo systemInfo = optInfo.get();

            OperatingSystem osInfo = systemInfo.getOperatingSystem();
            sendMessage(sender, "OS family", osInfo.getFamily());
            sendMessage(sender, "OS version", osInfo.getVersionInfo().toString());
            sendMessage(sender, "OS Manufacturer", osInfo.getManufacturer());

            sendMessage(sender, "Total processes", String.valueOf(osInfo.getProcessCount()));
            sendMessage(sender, "Total threads", String.valueOf(osInfo.getThreadCount()));
        }

        //CPU
        sender.sendMessage(PRIMARY_COLOR + "CPU:");
        if (optInfo.isPresent()) {
            CentralProcessor processor = optInfo.get().getHardware().getProcessor();
            ProcessorIdentifier identifier = processor.getProcessorIdentifier();

            sendMessage(sender, "    Vendor", identifier.getVendor());
            sendMessage(sender, "    Family", identifier.getFamily());
            sendMessage(sender, "    Name", identifier.getName());
            sendMessage(sender, "    Model", identifier.getModel());
            sendMessage(sender, "    Id", identifier.getIdentifier());
            sendMessage(sender, "    Vendor freq", String.valueOf(identifier.getVendorFreq()));
            sendMessage(sender, "    Physical Cores", String.valueOf(processor.getPhysicalProcessorCount()));
        }

        sendMessage(sender, "    Logical Cores", String.valueOf(osBean.getAvailableProcessors()));
        sendMessage(sender, "    Endian", System.getProperty("sun.cpu.endian", "Unknown"));

        sendMessage(sender, "Load Average", String.valueOf(osBean.getSystemLoadAverage()));
        printExtendOsInfo(sender);

        displayDiskSpace(sender);

        NativeManager nativeData = plugin.getNativeData();
        sendMessage(sender, "Open file descriptors", String.valueOf(nativeData.getOpenFileDescriptors()));
        sendMessage(sender, "Max file descriptors", String.valueOf(nativeData.getMaxFileDescriptors()));

        sender.sendMessage(PRIMARY_COLOR + "Variables:");
        for (Entry<String, String> variable : System.getenv().entrySet()) {
            sendMessage(sender, "    " + variable.getKey(), variable.getValue());
        }

        return true;
    }

    private void printExtendOsInfo(CommandSender sender) {
        NativeManager nativeData = plugin.getNativeData();

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

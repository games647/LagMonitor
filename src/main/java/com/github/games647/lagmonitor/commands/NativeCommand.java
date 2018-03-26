package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.LagUtils;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;
import oshi.SystemInfo;
import oshi.hardware.Baseboard;
import oshi.hardware.ComputerSystem;
import oshi.hardware.Firmware;
import oshi.hardware.HWDiskStore;
import oshi.hardware.NetworkIF;
import oshi.hardware.Sensors;
import oshi.software.os.OSFileStore;

public class NativeCommand extends LagCommand {

    public NativeCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, command)) {
            sendError(sender, "Not whitelisted");
            return true;
        }

        SystemInfo systemInfo = plugin.getNativeData().getSystemInfo();

        //swap and load is already available in the environment command because MBeans already supports this
        long uptime = systemInfo.getHardware().getProcessor().getSystemUptime();
        sendMessage(sender, "OS Updatime", formatUptime(uptime));

        long mhz = systemInfo.getHardware().getProcessor().getVendorFreq();
        sendMessage(sender, "CPU MHZ", String.valueOf(mhz));

        // //IO wait
        // double wait = cpuPerc.getWait();
        // sender.sendMessage(PRIMARY_COLOR + "CPU Wait (I/O): " + SECONDARY_COLOR + wait + '%');
        //
        // Mem mem = sigar.getMem();
        // //included cache
        // long actualUsed = mem.getActualUsed();
        // long used = mem.getUsed();
        //
        // long cache = used - actualUsed;
        // sender.sendMessage(PRIMARY_COLOR + "Memory Cache: " + SECONDARY_COLOR + Sigar.formatSize(cache));

        printNetworkInfo(sender, systemInfo);
        printDiskInfo(sender, systemInfo);
        printSensorsInfo(sender, systemInfo);
        printBoardInfo(sender, systemInfo);

        return true;
    }

    private void printBoardInfo(CommandSender sender, SystemInfo systemInfo) {
        ComputerSystem computerSystem = systemInfo.getHardware().getComputerSystem();
        sendMessage(sender, "System Manufacturer", computerSystem.getManufacturer());
        sendMessage(sender, "System model", computerSystem.getModel());
        sendMessage(sender, "Serial number", computerSystem.getSerialNumber());

        sender.sendMessage(PRIMARY_COLOR + "Baseboard:");
        Baseboard baseboard = computerSystem.getBaseboard();
        sendMessage(sender, "    Manufacturer", baseboard.getManufacturer());
        sendMessage(sender, "    Model", baseboard.getModel());
        sendMessage(sender, "    Serial", baseboard.getVersion());
        sendMessage(sender, "    Version", baseboard.getVersion());

        sender.sendMessage(PRIMARY_COLOR + "BIOS Firmware:");
        Firmware firmware = computerSystem.getFirmware();
        sendMessage(sender, "    Manufacturer", firmware.getManufacturer());
        sendMessage(sender, "    Name", firmware.getName());
        sendMessage(sender, "    Description", firmware.getDescription());
        sendMessage(sender, "    Version", firmware.getVersion());

        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        sendMessage(sender, "    Release date", firmware.getReleaseDate().format(formatter));
    }

    private void printSensorsInfo(CommandSender sender, SystemInfo systemInfo) {
        Sensors sensors = systemInfo.getHardware().getSensors();
        double cpuTemperature = sensors.getCpuTemperature();
        sendMessage(sender, "CPU Temp °C", String.valueOf(LagUtils.round(cpuTemperature)));
        sendMessage(sender, "Voltage", String.valueOf(LagUtils.round(sensors.getCpuVoltage())));

        int[] fanSpeeds = sensors.getFanSpeeds();
        sendMessage(sender, "Fan speed (rpm)", Arrays.toString(fanSpeeds));
    }

    private void printDiskInfo(CommandSender sender, SystemInfo systemInfo) {
        //disk read write
        HWDiskStore[] diskStores = systemInfo.getHardware().getDiskStores();
        long diskReads = Arrays.stream(diskStores).mapToLong(HWDiskStore::getReadBytes).sum();
        long diskWrites = Arrays.stream(diskStores).mapToLong(HWDiskStore::getWriteBytes).sum();

        sendMessage(sender, "Disk read bytes", LagUtils.readableBytes(diskReads));
        sendMessage(sender, "Disk write bytes", LagUtils.readableBytes(diskWrites));

        sender.sendMessage(PRIMARY_COLOR + "Filesystems:");
        for (OSFileStore fileStore : systemInfo.getOperatingSystem().getFileSystem().getFileStores()) {
            sendMessage(sender, "    " + fileStore.getMount(), fileStore.getType());
        }
    }

    private void printNetworkInfo(CommandSender sender, SystemInfo info) {
        //net upload download
        NetworkIF[] networkIfs = info.getHardware().getNetworkIFs();
        if (networkIfs.length > 0) {
            NetworkIF networkInterface = networkIfs[0];

            String receivedBytes = LagUtils.readableBytes(networkInterface.getBytesRecv());
            String sentBytes = LagUtils.readableBytes(networkInterface.getBytesSent());
            sendMessage(sender, "Net Rec", receivedBytes);
            sendMessage(sender, "Net Sent", sentBytes);
        }
    }

    private String formatUptime(long uptime) {
        long days = uptime / (60 * 60 * 24);

        long minutes = uptime / 60;
        long hours = minutes / 60;
        hours %= 24;
        minutes %= 60;
        return days + " days " + hours + " hours " + minutes + " Minutes";
    }
}

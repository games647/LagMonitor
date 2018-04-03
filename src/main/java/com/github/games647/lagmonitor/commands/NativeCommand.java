package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.NativeData;
import com.github.games647.lagmonitor.utils.LagUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import oshi.SystemInfo;
import oshi.hardware.Baseboard;
import oshi.hardware.ComputerSystem;
import oshi.hardware.Firmware;
import oshi.hardware.HWDiskStore;
import oshi.hardware.Sensors;
import oshi.software.os.OSFileStore;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

public class NativeCommand extends LagCommand {

    public NativeCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canExecute(sender, command)) {
            return true;
        }

        Optional<SystemInfo> optInfo = plugin.getNativeData().getSystemInfo();
        if (optInfo.isPresent()) {
            displayNativeInfo(sender, optInfo.get());
        } else {
            sendError(sender, NATIVE_NOT_FOUND);
        }

        return true;
    }

    private void displayNativeInfo(CommandSender sender, SystemInfo systemInfo) {
        //swap and load is already available in the environment command because MBeans already supports this
        long uptime = TimeUnit.SECONDS.toMillis(systemInfo.getHardware().getProcessor().getSystemUptime());
        String uptimeFormat = DurationFormatUtils.formatDurationWords(uptime, false, false);
        sendMessage(sender, "OS Uptime", uptimeFormat);

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

        //disk
        printDiskInfo(sender, systemInfo);

        printSensorsInfo(sender, systemInfo.getHardware().getSensors());
        printBoardInfo(sender, systemInfo.getHardware().getComputerSystem());
    }

    private void printBoardInfo(CommandSender sender, ComputerSystem computerSystem) {
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

    private void printSensorsInfo(CommandSender sender, Sensors sensors) {
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

        sender.sendMessage(PRIMARY_COLOR + "Disks:");
        for (HWDiskStore disk : systemInfo.getHardware().getDiskStores()) {
            String size = LagUtils.readableBytes(disk.getSize());
            sendMessage(sender, "    " + disk.getName(), disk.getModel() + ' ' + size);
        }

        NativeData nativeData = plugin.getNativeData();
        sendMessage(sender, "Open file descriptors", String.valueOf(nativeData.getOpenFileDescriptors()));
        sendMessage(sender, "Max file descriptors", String.valueOf(nativeData.getMaxFileDescriptors()));

        sender.sendMessage(PRIMARY_COLOR + "Mounts:");
        for (OSFileStore fileStore : systemInfo.getOperatingSystem().getFileSystem().getFileStores()) {
            printMountInfo(sender, fileStore);
        }
    }

    private void printMountInfo(CommandSender sender, OSFileStore fileStore) {
        String type = fileStore.getType();
        String desc = fileStore.getDescription();

        long totalSpaceBytes = fileStore.getTotalSpace();
        String totalSpace = LagUtils.readableBytes(totalSpaceBytes);
        String usedSpace = LagUtils.readableBytes(totalSpaceBytes - fileStore.getUsableSpace());

        String format = desc + ' ' + type + ' ' + usedSpace + '/' + totalSpace;
        sendMessage(sender, "    " + fileStore.getMount(), format);
    }
}

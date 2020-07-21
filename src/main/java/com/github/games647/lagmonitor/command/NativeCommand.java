package com.github.games647.lagmonitor.command;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.util.LagUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import oshi.SystemInfo;
import oshi.demo.DetectVM;
import oshi.hardware.Baseboard;
import oshi.hardware.ComputerSystem;
import oshi.hardware.Firmware;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;
import oshi.hardware.Sensors;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

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
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();

        //swap and load is already available in the environment command because MBeans already supports this
        long uptime = TimeUnit.SECONDS.toMillis(operatingSystem.getSystemUptime());
        String uptimeFormat = LagMonitor.formatDuration(Duration.ofMillis(uptime));
        sendMessage(sender, "OS Uptime", uptimeFormat);

        String startTime = LagMonitor.formatDuration(Duration.ofMillis(uptime));
        sendMessage(sender, "OS Start time", startTime);

        sendMessage(sender, "CPU Freq", Arrays.toString(hardware.getProcessor().getCurrentFreq()));
        sendMessage(sender, "CPU Max Freq", String.valueOf(hardware.getProcessor().getMaxFreq()));
        sendMessage(sender, "VM Hypervisor", DetectVM.identifyVM());

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
        printDiskInfo(sender, hardware.getDiskStores());
        displayMounts(sender, operatingSystem.getFileSystem().getFileStores());

        printSensorsInfo(sender, hardware.getSensors());
        printBoardInfo(sender, hardware.getComputerSystem());

        printRAMInfo(sender, hardware.getMemory().getPhysicalMemory());
    }

    private void printRAMInfo(CommandSender sender, List<PhysicalMemory> physicalMemories) {
        sender.sendMessage(PRIMARY_COLOR + "Memory:");
        for (PhysicalMemory memory : physicalMemories) {
            sendMessage(sender, "    Label", memory.getBankLabel());
            sendMessage(sender, "    Manufacturer", memory.getManufacturer());
            sendMessage(sender, "    Type", memory.getMemoryType());
            sendMessage(sender, "    Capacity", LagUtils.readableBytes(memory.getCapacity()));
            sendMessage(sender, "    Clock speed", String.valueOf(memory.getClockSpeed()));
        }
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

        sendMessage(sender, "    Release date", firmware.getReleaseDate());
    }

    private void printSensorsInfo(CommandSender sender, Sensors sensors) {
        double cpuTemperature = sensors.getCpuTemperature();
        sendMessage(sender, "CPU Temp °C", String.valueOf(LagUtils.round(cpuTemperature)));
        sendMessage(sender, "Voltage", String.valueOf(LagUtils.round(sensors.getCpuVoltage())));

        int[] fanSpeeds = sensors.getFanSpeeds();
        sendMessage(sender, "Fan speed (rpm)", Arrays.toString(fanSpeeds));
    }

    private void printDiskInfo(CommandSender sender, List<HWDiskStore> diskStores) {
        //disk read write
        long diskReads = diskStores.stream().mapToLong(HWDiskStore::getReadBytes).sum();
        long diskWrites = diskStores.stream().mapToLong(HWDiskStore::getWriteBytes).sum();

        sendMessage(sender, "Disk read bytes", LagUtils.readableBytes(diskReads));
        sendMessage(sender, "Disk write bytes", LagUtils.readableBytes(diskWrites));

        sender.sendMessage(PRIMARY_COLOR + "Disks:");
        for (HWDiskStore disk : diskStores) {
            String size = LagUtils.readableBytes(disk.getSize());
            sendMessage(sender, "    " + disk.getName(), disk.getModel() + ' ' + size);
        }
    }

    private void displayMounts(CommandSender sender, List<OSFileStore> fileStores) {
        sender.sendMessage(PRIMARY_COLOR + "Mounts:");
        for (OSFileStore fileStore : fileStores) {
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

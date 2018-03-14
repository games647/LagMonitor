package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.LagUtils;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSFileStore;

public class NativeCommand extends LagCommand {

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;
    private static final ChatColor SECONDARY_COLOR = ChatColor.GRAY;

    public NativeCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, command)) {
            sender.sendMessage(org.bukkit.ChatColor.DARK_RED + "Not whitelisted");
            return true;
        }

        if (!plugin.getConfig().getBoolean("native-library")) {
            sender.sendMessage(ChatColor.DARK_RED + "Native support is disabled");
            return true;
        }

        SystemInfo systemInfo = plugin.getNativeData().getSystemInfo();

        //swap and load is already available in the environment command because MBeans already supports this
        long uptime = systemInfo.getHardware().getProcessor().getSystemUptime();
        sender.sendMessage(PRIMARY_COLOR + "OS Uptime: " + SECONDARY_COLOR + formatUptime(uptime));

        long mhz = systemInfo.getHardware().getProcessor().getVendorFreq();
        sender.sendMessage(PRIMARY_COLOR + "CPU MHZ: " + SECONDARY_COLOR + mhz);

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

        //disk read write
        HWDiskStore[] diskStores = systemInfo.getHardware().getDiskStores();
        long diskReads = Arrays.stream(diskStores).mapToLong(HWDiskStore::getReadBytes).sum();
        long diskWrites = Arrays.stream(diskStores).mapToLong(HWDiskStore::getWriteBytes).sum();

        sender.sendMessage(PRIMARY_COLOR + "Disk read bytes: " + SECONDARY_COLOR + LagUtils.readableBytes(diskReads));
        sender.sendMessage(PRIMARY_COLOR + "Disk write bytes: " + SECONDARY_COLOR + LagUtils.readableBytes(diskWrites));

        sender.sendMessage(PRIMARY_COLOR + "Filesystems:");
        for (OSFileStore fileStore : systemInfo.getOperatingSystem().getFileSystem().getFileStores()) {
            sender.sendMessage(PRIMARY_COLOR + fileStore.getMount() + " - " + SECONDARY_COLOR + fileStore.getType());
        }

        return true;
    }

    private void printNetworkInfo(CommandSender sender, SystemInfo info) {
        //net upload download
        NetworkIF[] networkIfs = info.getHardware().getNetworkIFs();
        if (networkIfs.length > 0) {
            NetworkIF networkInterface = networkIfs[0];

            String receivedBytes = LagUtils.readableBytes(networkInterface.getBytesRecv());
            String sentBytes = LagUtils.readableBytes(networkInterface.getBytesSent());
            sender.sendMessage(PRIMARY_COLOR + "Net Rec: " + SECONDARY_COLOR + receivedBytes);
            sender.sendMessage(PRIMARY_COLOR + "Net Sent: " + SECONDARY_COLOR + sentBytes);
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

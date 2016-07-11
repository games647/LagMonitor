package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class NativeCommand implements CommandExecutor {

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;
    private static final ChatColor SECONDARY_COLOR = ChatColor.GRAY;

    private final LagMonitor plugin;

    public NativeCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.isAllowed(sender, command)) {
            sender.sendMessage(org.bukkit.ChatColor.DARK_RED + "Not whitelisted");
            return true;
        }

        if (!plugin.getConfig().getBoolean("native-library")) {
            sender.sendMessage(ChatColor.DARK_RED + "Native support is disabled");
            return true;
        }

        //swap and load is already avaiable in the environment command because MBeans already supports this
        Sigar sigar = plugin.getSigar();
        try {
            int uptime = (int) sigar.getUptime().getUptime();
            sender.sendMessage(PRIMARY_COLOR + "OS Uptime: " + SECONDARY_COLOR + formatUptime(uptime));

            CpuInfo[] cpuInfoList = sigar.getCpuInfoList();
            int mhz = cpuInfoList[0].getMhz();
            sender.sendMessage(PRIMARY_COLOR + "CPU MHZ: " + SECONDARY_COLOR + mhz);

            CpuPerc cpuPerc = sigar.getCpuPerc();
            //IO wait
            double wait = cpuPerc.getWait();
            sender.sendMessage(PRIMARY_COLOR + "CPU Wait (I/O): " + SECONDARY_COLOR + wait + '%');

            Mem mem = sigar.getMem();
            //included cache
            long actualUsed = mem.getActualUsed();
            long used = mem.getUsed();

            long cache = used - actualUsed;
            sender.sendMessage(PRIMARY_COLOR + "Memory Cache: " + SECONDARY_COLOR + Sigar.formatSize(cache));

            printNetworkInfo(sender, sigar);

            //disk read write
            String rootFileSystem = File.listRoots()[0].getAbsolutePath();
            FileSystemUsage fileSystemUsage = sigar.getFileSystemUsage(rootFileSystem);
            long diskReadBytes = fileSystemUsage.getDiskReadBytes();
            long diskWriteBytes = fileSystemUsage.getDiskWriteBytes();
            sender.sendMessage(PRIMARY_COLOR + "Disk Read: " + SECONDARY_COLOR + Sigar.formatSize(diskReadBytes));
            sender.sendMessage(PRIMARY_COLOR + "Disk Write: " + SECONDARY_COLOR + Sigar.formatSize(diskWriteBytes));
        } catch (SigarException sigarException) {
            plugin.getLogger().log(Level.SEVERE, null, sigarException);
        }
        
        return true;
    }

    private void printNetworkInfo(CommandSender sender, Sigar sigar) throws SigarException {
        //net upload download
        NetInterfaceStat usedNetInterfaceStat = null;
        String[] netInterfaceList = sigar.getNetInterfaceList();
        for (String interfaceName : netInterfaceList) {
            NetInterfaceStat interfaceStat = sigar.getNetInterfaceStat(interfaceName);
            if (interfaceStat.getRxBytes() != 0) {
                usedNetInterfaceStat = interfaceStat;
                break;
            }
        }

        if (usedNetInterfaceStat != null) {
            long speed = usedNetInterfaceStat.getSpeed();
            sender.sendMessage(PRIMARY_COLOR + "Net Speed: " + SECONDARY_COLOR + Sigar.formatSize(speed));

            long receivedBytes = usedNetInterfaceStat.getRxBytes();
            long sentBytes = usedNetInterfaceStat.getTxBytes();
            sender.sendMessage(PRIMARY_COLOR + "Net Rec: " + SECONDARY_COLOR + Sigar.formatSize(receivedBytes));
            sender.sendMessage(PRIMARY_COLOR + "Net Sent: " + SECONDARY_COLOR + Sigar.formatSize(sentBytes));
        }
    }

    private String formatUptime(int uptime) {
        int days = uptime / (60 * 60 * 24);

        int minutes = uptime / 60;
        int hours = minutes / 60;
        hours %= 24;
        minutes %= 60;
        return days + " days " + hours + " hours " + minutes + " Minutes";
    }
}

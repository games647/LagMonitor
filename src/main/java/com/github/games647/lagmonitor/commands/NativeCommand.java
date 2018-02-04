package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.DiskUsage;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;

import static java.util.stream.Collectors.toList;

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

        //swap and load is already available in the environment command because MBeans already supports this
        Sigar sigar = plugin.getNativeData().getSigar();
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
            List<String> diskNames = Arrays.stream(sigar.getFileSystemList())
                    .map(FileSystem::getDevName)
                    .filter(name -> name.startsWith("/dev/sd"))
                    .distinct()
                    .collect(toList());

            Collection<DiskUsage> diskUsages = new ArrayList<>();
            for (String diskName : diskNames) {
                diskUsages.add(sigar.getDiskUsage(diskName));
            }

            long diskReads = diskUsages.stream().mapToLong(DiskUsage::getReadBytes).sum();
            long diskWrites = diskUsages.stream().mapToLong(DiskUsage::getWriteBytes).sum();


            String diskReadBytes = Sigar.formatSize(diskReads);
            String diskWriteBytes = Sigar.formatSize(diskWrites);

            sender.sendMessage(PRIMARY_COLOR + "Disk read bytes: " + SECONDARY_COLOR + diskReadBytes);
            sender.sendMessage(PRIMARY_COLOR + "Disk write bytes: " + SECONDARY_COLOR + diskWriteBytes);

            sender.sendMessage(PRIMARY_COLOR + "Filesystems:");
            for (FileSystem fileSystem : sigar.getFileSystemList()) {
                String dirName = fileSystem.getDirName();
                String typeName = fileSystem.getSysTypeName();
                sender.sendMessage(PRIMARY_COLOR + dirName + " - " + SECONDARY_COLOR + typeName);
            }
        } catch (SigarException sigarException) {
            plugin.getLogger().log(Level.SEVERE, null, sigarException);
        }

        return true;
    }

    private void printNetworkInfo(CommandSender sender, SigarProxy sigar) throws SigarException {
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

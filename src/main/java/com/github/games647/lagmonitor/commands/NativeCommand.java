package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.io.File;
import java.text.SimpleDateFormat;
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
import org.hyperic.sigar.SigarLoader;

public class NativeCommand implements CommandExecutor {

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;
    private static final ChatColor SECONDARY_COLOR = ChatColor.GRAY;

    private static final String DRIVER_NAME = SigarLoader.getNativeLibraryName();

    private final LagMonitor plugin;

    public NativeCommand(LagMonitor plugin) {
        this.plugin = plugin;

        //setting the location where sigar can find the library
        System.setProperty("org.hyperic.sigar.path", plugin.getDataFolder().getPath());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("native-library")) {
            sender.sendMessage(ChatColor.DARK_RED + "Native support is disabled");
            return true;
        }

        //we have to check this manually because the load statement in sigar is static
        File driverFile = new File(plugin.getDataFolder(), DRIVER_NAME);
        if (!driverFile.exists()) {
            sender.sendMessage(ChatColor.DARK_RED + "Couldn't find driver in the plugin folder");
            sender.sendMessage(ChatColor.DARK_RED + "You need: " + DRIVER_NAME);
            sender.sendMessage(ChatColor.DARK_RED + "and place it here: " + plugin.getDataFolder().getPath());
            return true;
        }

        Sigar sigar = new Sigar();
        try {
            double uptime = sigar.getUptime().getUptime() - 60 * 60 * 1000;
            String uptimeFormat = new SimpleDateFormat("HH 'hour' mm 'minutes' ss 'seconds'").format(uptime);
            sender.sendMessage(PRIMARY_COLOR + "OS Updtime: " + SECONDARY_COLOR + uptimeFormat);

            CpuInfo[] cpuInfoList = sigar.getCpuInfoList();
            int mhz = cpuInfoList[0].getMhz();
            sender.sendMessage(PRIMARY_COLOR + "CPU MHZ: " + SECONDARY_COLOR + mhz);

            CpuPerc cpuPerc = sigar.getCpuPerc();
            //IO wait
            double wait = cpuPerc.getWait();
//            double combined = cpuPerc.getCombined();
            sender.sendMessage(PRIMARY_COLOR + "CPU Wait (I/O): " + SECONDARY_COLOR + wait + '%');

            Mem mem = sigar.getMem();
            //included cache
            long actualUsed = mem.getActualUsed();
            long used = mem.getUsed();

            long cache = actualUsed - used;
            sender.sendMessage(PRIMARY_COLOR + "Memory Cache: " + SECONDARY_COLOR + Sigar.formatSize(cache));

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
                sender.sendMessage(PRIMARY_COLOR + "Net name" + SECONDARY_COLOR + usedNetInterfaceStat);
                sender.sendMessage(PRIMARY_COLOR + "Net speed" + SECONDARY_COLOR + usedNetInterfaceStat.getSpeed());

                long receivedBytes = usedNetInterfaceStat.getRxBytes();
                long sentBytes = usedNetInterfaceStat.getTxBytes();
                sender.sendMessage(PRIMARY_COLOR + "Net Received" + SECONDARY_COLOR + Sigar.formatSize(receivedBytes));
                sender.sendMessage(PRIMARY_COLOR + "Net Sent" + SECONDARY_COLOR + Sigar.formatSize(sentBytes));
            }

            //*should* already included in environment - because its querable with mbeans too
//            Swap swap = sigar.getSwap();
//            long swapTotal = swap.getTotal();
//            long swapUsed = swap.getUsed();

            //load - only available in linux/Unix
//            System.out.println(Arrays.toString(sigar.getLoadAverage()));

            //disk read write
            String rootFileSystem = File.listRoots()[0].getAbsolutePath();
            FileSystemUsage fileSystemUsage = sigar.getFileSystemUsage(rootFileSystem);
            long diskReadBytes = fileSystemUsage.getDiskReadBytes();
            long diskWriteBytes = fileSystemUsage.getDiskWriteBytes();
            sender.sendMessage(PRIMARY_COLOR + "Disk Read" + SECONDARY_COLOR + Sigar.formatSize(diskReadBytes));
            sender.sendMessage(PRIMARY_COLOR + "Disk Write" + SECONDARY_COLOR + Sigar.formatSize(diskWriteBytes));
        } catch (SigarException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        } finally {
            sigar.close();
        }

        return true;
    }
}

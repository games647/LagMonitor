package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.hyperic.jni.ArchLoader;
import org.hyperic.jni.ArchNotSupportedException;
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

        //setting the location where sigar can find the library
        //otherwise it would lookup the library path of Java
        System.setProperty("org.hyperic.sigar.path", plugin.getDataFolder().getPath());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("native-library")) {
            sender.sendMessage(ChatColor.DARK_RED + "Native support is disabled");
            return true;
        }

        //we have to check this manually because the load statement in sigar is static
        String driverName = null;
        try {
            ArchLoader archLoader = new ArchLoader();
            archLoader.setName("sigar");
            driverName = ArchLoader.getLibraryPrefix()
                    + archLoader.getArchLibName()
                    + ArchLoader.getLibraryExtension();
        } catch (ArchNotSupportedException archNotSupportedException) {
            plugin.getLogger().log(Level.SEVERE, null, archNotSupportedException);
        }

        File driverFile = new File(plugin.getDataFolder(), driverName);
        if (driverFile == null || !driverFile.exists()) {
            sender.sendMessage(ChatColor.DARK_RED + "Couldn't find driver in the plugin folder");
            sender.sendMessage(ChatColor.DARK_RED + "You need: " + driverName);
            sender.sendMessage(ChatColor.DARK_RED + "and place it here: " + plugin.getDataFolder().getPath());
            return true;
        }

        Sigar sigar = new Sigar();
        try {
            int uptime = (int) sigar.getUptime().getUptime();
            sender.sendMessage(PRIMARY_COLOR + "OS Updtime: " + SECONDARY_COLOR + formatUptime(uptime));

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

            long cache = used - actualUsed;
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
                long speed = usedNetInterfaceStat.getSpeed();
                sender.sendMessage(PRIMARY_COLOR + "Net Speed: " + SECONDARY_COLOR + Sigar.formatSize(speed));

                long receivedBytes = usedNetInterfaceStat.getRxBytes();
                long sentBytes = usedNetInterfaceStat.getTxBytes();
                sender.sendMessage(PRIMARY_COLOR + "Net Rec: " + SECONDARY_COLOR + Sigar.formatSize(receivedBytes));
                sender.sendMessage(PRIMARY_COLOR + "Net Sent: " + SECONDARY_COLOR + Sigar.formatSize(sentBytes));
            }

            //*should* already included in environment - because its queryable with mbeans too
//            Swap swap = sigar.getSwap();
//            long swapTotal = swap.getTotal();
//            long swapUsed = swap.getUsed();

            //load - only available in linux/Unix already included in environment
//            System.out.println(Arrays.toString(sigar.getLoadAverage()));

            //disk read write
            String rootFileSystem = File.listRoots()[0].getAbsolutePath();
            FileSystemUsage fileSystemUsage = sigar.getFileSystemUsage(rootFileSystem);
            long diskReadBytes = fileSystemUsage.getDiskReadBytes();
            long diskWriteBytes = fileSystemUsage.getDiskWriteBytes();
            sender.sendMessage(PRIMARY_COLOR + "Disk Read: " + SECONDARY_COLOR + Sigar.formatSize(diskReadBytes));
            sender.sendMessage(PRIMARY_COLOR + "Disk Write: " + SECONDARY_COLOR + Sigar.formatSize(diskWriteBytes));
        } catch (SigarException sigarException) {
            plugin.getLogger().log(Level.SEVERE, null, sigarException);
        } finally {
            sigar.close();
        }

        return true;
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

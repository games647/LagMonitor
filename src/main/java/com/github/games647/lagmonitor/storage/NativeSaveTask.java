package com.github.games647.lagmonitor.storage;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.traffic.TrafficReader;

import java.io.File;
import java.util.logging.Level;

import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

public class NativeSaveTask implements Runnable {

    private final LagMonitor plugin;

    public NativeSaveTask(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        TrafficReader trafficReader = plugin.getTrafficReader();
        int mcRead = 0;
        int mcWrite = 0;
        if (trafficReader != null) {
            mcRead = byteToMega(trafficReader.getIncomingBytes().get());
            mcWrite = byteToMega(trafficReader.getOutgoingBytes().get());
        }

        File[] listRoots = File.listRoots();
        int totalSpace = 0;
        int freeSpace = 0;
        for (File rootFile : listRoots) {
            freeSpace += rootFile.getUsableSpace();
            totalSpace += rootFile.getTotalSpace();
        }

        totalSpace = byteToMega(totalSpace);
        freeSpace = byteToMega(freeSpace);

        //4 decimal places -> Example: 0.2456
        float freeSpacePct = Math.round(((float) freeSpace / totalSpace) * 10000) / 10000;

        int diskRead = 0;
        int diskWrite = 0;
        int netRead = 0;
        int netWrite = 0;

        Sigar sigar = plugin.getSigar();
        if (sigar != null) {
            try {
                String rootFileSystem = File.listRoots()[0].getAbsolutePath();
                FileSystemUsage fileSystemUsage = sigar.getFileSystemUsage(rootFileSystem);
                diskRead = byteToMega(fileSystemUsage.getDiskReadBytes());
                diskWrite = byteToMega(fileSystemUsage.getDiskWriteBytes());

                NetInterfaceStat usedNetInterfaceStat = findNetworkInterface(sigar);

                if (usedNetInterfaceStat != null) {
                    netRead = byteToMega(usedNetInterfaceStat.getRxBytes());
                    netWrite = byteToMega(usedNetInterfaceStat.getTxBytes());
                }
            } catch (SigarException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get the disk read/writer for database monitoring", ex);
            }
        }

        plugin.getStorage().saveNative(mcRead, mcWrite, freeSpace, freeSpacePct, diskRead, diskWrite
                , netRead, netWrite);
    }

    private NetInterfaceStat findNetworkInterface(Sigar sigar) throws SigarException {
        NetInterfaceStat usedNetInterfaceStat = null;
        String[] netInterfaceList = sigar.getNetInterfaceList();
        for (String interfaceName : netInterfaceList) {
            NetInterfaceStat interfaceStat = sigar.getNetInterfaceStat(interfaceName);
            if (interfaceStat.getRxBytes() != 0) {
                usedNetInterfaceStat = interfaceStat;
                break;
            }
        }
        return usedNetInterfaceStat;
    }

    private int byteToMega(long bytes) {
        return (int) (bytes / (1024 * 1024));
    }
}

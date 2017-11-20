package com.github.games647.lagmonitor.storage;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.LagUtils;
import com.github.games647.lagmonitor.traffic.TrafficReader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;

import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;

import static com.github.games647.lagmonitor.LagUtils.round;

public class NativeSaveTask implements Runnable {

    private final LagMonitor plugin;

    private Instant lastCheck = Instant.now();

    private int lastMcRead;
    private int lastMcWrite;
    private int lastDiskRead;
    private int lastDiskWrite;
    private int lastNetRead;
    private int lastNetWrite;

    public NativeSaveTask(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Instant currentTime = Instant.now();
        int timeDiff = (int) Duration.between(lastCheck, currentTime).getSeconds();

        TrafficReader trafficReader = plugin.getTrafficReader();
        int mcReadDiff = 0;
        int mcWriteDiff = 0;
        if (trafficReader != null) {
            int mcRead = LagUtils.byteToMega(trafficReader.getIncomingBytes().get());
            mcReadDiff = (mcRead - lastMcRead) / timeDiff;
            lastMcRead = mcRead;

            int mcWrite = LagUtils.byteToMega(trafficReader.getOutgoingBytes().get());
            mcWriteDiff = (mcWrite - lastMcWrite) / timeDiff;
            lastMcWrite = mcWrite;
        }

        int totalSpace = LagUtils.byteToMega(plugin.getNativeData().getTotalSpace());
        int freeSpace = LagUtils.byteToMega(plugin.getNativeData().getFreeSpace());

        //4 decimal places -> Example: 0.2456
        float freeSpacePct = round((freeSpace * 100 / (float) totalSpace), 4);

        int diskReadDiff = 0;
        int diskWriteDiff = 0;
        int netReadDiff = 0;
        int netWriteDiff = 0;

        Sigar sigar = plugin.getNativeData().getSigar();
        if (sigar != null) {
            try {
                Path root = Paths.get(".").getRoot();
                if (root != null) {
                    String rootFileSystem = root.toAbsolutePath().toString();
                    FileSystemUsage fileSystemUsage = sigar.getFileSystemUsage(rootFileSystem);
                    int diskRead = LagUtils.byteToMega(fileSystemUsage.getDiskReadBytes());
                    diskReadDiff = (diskRead - lastDiskRead) / timeDiff;
                    lastDiskRead = diskRead;

                    int diskWrite = LagUtils.byteToMega(fileSystemUsage.getDiskWriteBytes());
                    diskWriteDiff = (diskWrite - lastDiskWrite) / timeDiff;
                    lastDiskWrite = diskRead;
                }

                NetInterfaceStat usedNetInterfaceStat = findNetworkInterface(sigar);
                if (usedNetInterfaceStat != null) {
                    int netRead = LagUtils.byteToMega(usedNetInterfaceStat.getRxBytes());
                    netReadDiff = (netRead - lastNetRead) / timeDiff;
                    lastNetRead = netRead;

                    int netWrite = LagUtils.byteToMega(usedNetInterfaceStat.getTxBytes());
                    netWriteDiff = (netWrite - lastNetWrite) / timeDiff;
                    lastNetWrite = netWrite;
                }
            } catch (SigarException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get the disk read/writer for database monitoring", ex);
            }
        }

        lastCheck = currentTime;

        plugin.getStorage().saveNative(mcReadDiff, mcWriteDiff, freeSpace, freeSpacePct, diskReadDiff, diskWriteDiff
                , netReadDiff, netWriteDiff);
    }

    private NetInterfaceStat findNetworkInterface(SigarProxy sigar) throws SigarException {
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
}

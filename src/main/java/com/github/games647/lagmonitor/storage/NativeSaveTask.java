package com.github.games647.lagmonitor.storage;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.utils.LagUtils;
import com.github.games647.lagmonitor.traffic.TrafficReader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import oshi.SystemInfo;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSProcess;

import static com.github.games647.lagmonitor.utils.LagUtils.round;

public class NativeSaveTask implements Runnable {

    private final LagMonitor plugin;
    private final Storage storage;

    private Instant lastCheck = Instant.now();

    private int lastMcRead;
    private int lastMcWrite;
    private int lastDiskRead;
    private int lastDiskWrite;
    private int lastNetRead;
    private int lastNetWrite;

    public NativeSaveTask(LagMonitor plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
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

        SystemInfo systemInfo = plugin.getNativeData().getSystemInfo();
        Path root = Paths.get(".").getRoot();
        if (root != null) {
            String rootFileSystem = root.toAbsolutePath().toString();

            OSProcess process = plugin.getNativeData().getProcess();
            int diskRead = LagUtils.byteToMega(process.getBytesRead());
            diskReadDiff = (diskRead - lastDiskRead) / timeDiff;
            lastDiskRead = diskRead;

            int diskWrite = LagUtils.byteToMega(process.getBytesWritten());
            diskWriteDiff = (diskWrite - lastDiskWrite) / timeDiff;
            lastDiskWrite = diskRead;

            NetworkIF[] networkIfs = systemInfo.getHardware().getNetworkIFs();
            if (networkIfs.length > 0) {
                NetworkIF networkInterface = networkIfs[0];

                int netRead = LagUtils.byteToMega(networkInterface.getBytesRecv());
                netReadDiff = (netRead - lastNetRead) / timeDiff;
                lastNetRead = netRead;

                int netWrite = LagUtils.byteToMega(networkInterface.getBytesSent());
                netWriteDiff = (netWrite - lastNetWrite) / timeDiff;
                lastNetWrite = netWrite;
            }
        }

        lastCheck = currentTime;
        storage.saveNative(mcReadDiff, mcWriteDiff, freeSpace, freeSpacePct, diskReadDiff, diskWriteDiff
                , netReadDiff, netWriteDiff);
    }
}

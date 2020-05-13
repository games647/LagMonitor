package com.github.games647.lagmonitor.storage;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.traffic.TrafficReader;
import com.github.games647.lagmonitor.util.LagUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import oshi.SystemInfo;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSProcess;

import static com.github.games647.lagmonitor.util.LagUtils.round;

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

        int mcReadDiff = 0;
        int mcWriteDiff = 0;

        TrafficReader trafficReader = plugin.getTrafficReader();
        if (trafficReader != null) {
            int mcRead = LagUtils.byteToMega(trafficReader.getIncomingBytes().longValue());
            mcReadDiff = getDifference(mcRead, lastMcRead, timeDiff);
            lastMcRead = mcRead;

            int mcWrite = LagUtils.byteToMega(trafficReader.getOutgoingBytes().longValue());
            mcWriteDiff = getDifference(mcWrite, lastMcWrite, timeDiff);
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

        Optional<SystemInfo> systemInfo = plugin.getNativeData().getSystemInfo();
        if (systemInfo.isPresent()) {
            NetworkIF[] networkIfs = systemInfo.get().getHardware().getNetworkIFs();
            if (networkIfs.length > 0) {
                NetworkIF networkInterface = networkIfs[0];

                int netRead = LagUtils.byteToMega(networkInterface.getBytesRecv());
                netReadDiff = getDifference(netRead, lastNetRead, timeDiff);
                lastNetRead = netRead;

                int netWrite = LagUtils.byteToMega(networkInterface.getBytesSent());
                netWriteDiff = getDifference(netWrite, lastNetWrite, timeDiff);
                lastNetWrite = netWrite;
            }

            Path root = Paths.get(".").getRoot();
            Optional<OSProcess> optProcess = plugin.getNativeData().getProcess();
            if (root != null && optProcess.isPresent()) {
                OSProcess process = optProcess.get();
                String rootFileSystem = root.toAbsolutePath().toString();

                int diskRead = LagUtils.byteToMega(process.getBytesRead());
                diskReadDiff = getDifference(diskRead, lastDiskRead, timeDiff);
                lastDiskRead = diskRead;

                int diskWrite = LagUtils.byteToMega(process.getBytesWritten());
                diskWriteDiff = getDifference(diskWrite, lastDiskWrite, timeDiff);
                lastDiskWrite = diskWrite;
            }
        }

        lastCheck = currentTime;
        storage.saveNative(mcReadDiff, mcWriteDiff, freeSpace, freeSpacePct, diskReadDiff, diskWriteDiff
                , netReadDiff, netWriteDiff);
    }

    private int getDifference(long newVal, long oldVal, long timeDiff) {
        return (int) ((newVal - oldVal) / timeDiff);
    }
}

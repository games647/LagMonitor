package com.github.games647.lagmonitor;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSProcess;

public class NativeData {

    private final Logger logger;

    private final SystemInfo info;
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

    public NativeData(Logger logger, SystemInfo info) {
        this.logger = logger;
        this.info = info;

        if (info == null && !(osBean instanceof com.sun.management.OperatingSystemMXBean)) {
            logger.severe("You're not using Oracle Java nor using the native library. " +
                    "You wan't be able to read native data");
        }
    }

    public SystemInfo getSystemInfo() {
        return info;
    }

    public double getProcessCPULoad() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean nativeOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return nativeOsBean.getProcessCpuLoad();
        } else if (info != null) {
            int pid = info.getOperatingSystem().getProcessId();
            // return info.getOperatingSystem().getProcess(pid).get
        }

        return -1;
    }

    public OSProcess getProces() {
        return null;
    }

    public double getCPULoad() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean nativeOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return nativeOsBean.getSystemCpuLoad();
        } else if (info != null) {
            return info.getHardware().getProcessor().getSystemCpuLoad();
        }

        return -1;
    }

    public long getTotalMemory() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean nativeOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return nativeOsBean.getTotalPhysicalMemorySize();
        } else if (info != null) {
            return info.getHardware().getMemory().getTotal();
        }

        return -1;
    }

    public long getFreeMemory() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean nativeOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return nativeOsBean.getFreePhysicalMemorySize();
        } else if (info != null) {
            return getTotalMemory() - info.getHardware().getMemory().getAvailable();
        }

        return -1;
    }

    public long getFreeSwap() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean nativeOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return nativeOsBean.getFreeSwapSpaceSize();
        } else if (info != null) {
            GlobalMemory memory = info.getHardware().getMemory();
            return memory.getSwapTotal() - memory.getSwapUsed();
        }

        return -1;
    }

    public long getTotalSwap() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean nativeOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return nativeOsBean.getTotalSwapSpaceSize();
        } else if (info != null) {
            return info.getHardware().getMemory().getSwapTotal();
        }

        return -1;
    }

    public long getFreeSpace() {
        long freeSpace = 0;
        try {
            FileStore fileStore = Files.getFileStore(Paths.get("."));
            freeSpace = fileStore.getUsableSpace();
        } catch (IOException ioEx) {
            logger.log(Level.WARNING, "Cannot calculate free/total disk space", ioEx);
        }

        return freeSpace;
    }

    public long getTotalSpace() {
        long totalSpace = 0;
        try {
            FileStore fileStore = Files.getFileStore(Paths.get("."));
            totalSpace = fileStore.getTotalSpace();
        } catch (IOException ioEx) {
            logger.log(Level.WARNING, "Cannot calculate free disk space", ioEx);
        }

        return totalSpace;
    }
}

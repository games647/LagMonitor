package com.github.games647.lagmonitor;

import com.sun.management.UnixOperatingSystemMXBean;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSProcess;

public class NativeData {

    private final Logger logger;

    private final int pid;

    private final SystemInfo info;
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

    public NativeData(Logger logger, SystemInfo info) {
        this.logger = logger;
        this.info = info;

        if (info == null && !(osBean instanceof com.sun.management.OperatingSystemMXBean)) {
            logger.severe("You're not using Oracle Java nor using the native library. " +
                    "You won't be able to read some native data");
        }

        if (info == null) {
            pid = -1;
        } else {
            pid = info.getOperatingSystem().getProcessId();
        }
    }

    public Optional<SystemInfo> getSystemInfo() {
        return Optional.ofNullable(info);
    }

    public double getProcessCPULoad() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean nativeOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return nativeOsBean.getProcessCpuLoad();
        // } else if (info != null) {
            // return info.getOperatingSystem().getProcess(pid).getState();
        }

        return -1;
    }

    public Optional<OSProcess> getProcess() {
        if (info == null) {
            return Optional.empty();
        }

        return Optional.of(info.getOperatingSystem().getProcess(pid));
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

    public long getOpenFileDescriptors() {
        if (osBean instanceof com.sun.management.UnixOperatingSystemMXBean) {
            return ((UnixOperatingSystemMXBean) osBean).getOpenFileDescriptorCount();
        } else if (info != null) {
            return info.getOperatingSystem().getFileSystem().getOpenFileDescriptors();
        }

        return -1;
    }

    public long getMaxFileDescriptors() {
        if (osBean instanceof com.sun.management.UnixOperatingSystemMXBean) {
            return ((UnixOperatingSystemMXBean) osBean).getMaxFileDescriptorCount();
        } else if (info != null) {
            return info.getOperatingSystem().getFileSystem().getMaxFileDescriptors();
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
            return memory.getAvailable();
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

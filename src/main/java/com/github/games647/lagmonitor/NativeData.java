package com.github.games647.lagmonitor;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;

public class NativeData {

    private final Logger logger;

    private final Sigar sigar;
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

    private final long pid;

    public NativeData(Logger logger, Sigar sigar) {
        this.logger = logger;
        this.sigar = sigar;

        if (sigar == null) {
            pid = -1;
        } else {
            pid = sigar.getPid();
        }

        if (sigar == null && !(osBean instanceof com.sun.management.OperatingSystemMXBean)) {
            logger.severe("You're not using Oracle Java nor using the native library. "
                    + "You wan't be able to read native data");
        }
    }

    public Sigar getSigar() {
        return sigar;
    }

    public double getProcessCPULoad() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean nativeOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return nativeOsBean.getProcessCpuLoad();
        } else if (sigar != null) {
            try {
                ProcCpu procCpu = sigar.getProcCpu(pid);
                return procCpu.getPercent();
            } catch (SigarException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        return -1;
    }

    public double getCPULoad() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean nativeOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return nativeOsBean.getSystemCpuLoad();
        } else if (sigar != null) {
            try {
                CpuPerc systemCpu = sigar.getCpuPerc();
                return systemCpu.getCombined();
            } catch (SigarException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        return -1;
    }

    public long getTotalMemory() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean nativeOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return nativeOsBean.getTotalPhysicalMemorySize();
        } else if (sigar != null) {
            try {
                Mem mem = sigar.getMem();
                return mem.getTotal();
            } catch (SigarException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        return -1;
    }

    public long getFreeMemory() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean nativeOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return nativeOsBean.getFreePhysicalMemorySize();
        } else if (sigar != null) {
            try {
                Mem mem = sigar.getMem();
                return mem.getFree();
            } catch (SigarException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        return -1;
    }

    public long getFreeSwap() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean nativeOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return nativeOsBean.getFreeSwapSpaceSize();
        } else if (sigar != null) {
            try {
                Swap swap = sigar.getSwap();
                return swap.getFree();
            } catch (SigarException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        return -1;
    }

    public long getTotalSwap() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean nativeOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return nativeOsBean.getTotalSwapSpaceSize();
        } else if (sigar != null) {
            try {
                Swap swap = sigar.getSwap();
                return swap.getTotal();
            } catch (SigarException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
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

    public void close() {
        if (sigar != null) {
            sigar.close();
        }
    }
}

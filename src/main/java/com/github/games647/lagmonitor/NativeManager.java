package com.github.games647.lagmonitor;

import com.sun.management.UnixOperatingSystemMXBean;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSProcess;

import org.bukkit.Bukkit;

public class NativeManager {

    private static final String JNA_FILE = "jna-4.4.0.jar";

    private final Logger logger;
    private final Path dataFolder;

    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private SystemInfo info;

    private int pid = -1;

    public NativeManager(Logger logger, Path dataFolder) {
        this.logger = logger;
        this.dataFolder = dataFolder;
    }

    public void setupNativeAdapter() {
        if (!doesServerIncludeJNA()) {
            try {
                if (!loadExternalJNI()) {
                    if (!(osBean instanceof com.sun.management.OperatingSystemMXBean)) {
                        logger.severe("You're not using Oracle Java nor using the native library. " +
                                "You won't be able to read some native data");
                    }

                    return;
                }
            } catch (IOException ioEx) {
                logger.log(Level.WARNING, "Cannot load JNA library. We continue without it", ioEx);
            }
        }

        logger.info("Found JNA native library. Enabling extended native data support to display more data");
        try {
            info = new SystemInfo();

            //make a test call
            pid = info.getOperatingSystem().getProcessId();
        } catch (UnsatisfiedLinkError | NoClassDefFoundError linkError) {
            logger.log(Level.INFO, "Cannot load native library. Continuing without it...", linkError);
            info = null;
        }
    }

    private boolean loadExternalJNI() throws IOException {
        Path jnaPath = dataFolder.resolve(JNA_FILE);
        if (Files.exists(jnaPath)) {
            extractJNI(jnaPath);
            return true;
        } else {
            logger.info("JNA not found. " +
                    "Please download the this to the folder of this plugin to display more data about your setup");
            logger.info("https://repo1.maven.org/maven2/net/java/dev/jna/jna/4.4.0/jna-4.4.0.jar");
        }

        return false;
    }

    public Optional<SystemInfo> getSystemInfo() {
        return Optional.ofNullable(info);
    }

    private void extractJNI(Path jnaPath) throws IOException {
        URLClassLoader jnaLoader = new URLClassLoader(new URL[]{jnaPath.toUri().toURL()});

        String libName = "/com/sun/jna/" + com.sun.jna.Platform.RESOURCE_PREFIX
                + '/' + System.mapLibraryName("jnidispatch").replace(".dylib", ".jnilib");
        com.sun.jna.Native.extractFromResourcePath(libName, jnaLoader);
    }

    private boolean doesServerIncludeJNA() {
        try {
            Class.forName("com.sun.jna.Platform", true, Bukkit.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException classNotFoundEx) {
            return false;
        }
    }

    public double getProcessCPULoad() {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean nativeOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return nativeOsBean.getProcessCpuLoad();
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

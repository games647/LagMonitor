package com.github.games647.lagmonitor.graphs;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import org.bukkit.map.MapCanvas;

public class CpuGraph extends GraphRenderer {

    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

    public CpuGraph() {
        super("CPU Usage");
    }

    @Override
    public int renderGraphTick(MapCanvas canvas, int nextPosX) {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;

            int systemLoad = (int) (sunOsBean.getSystemCpuLoad() * 100);
            int processLoad = (int) (sunOsBean.getProcessCpuLoad() * 100);

            canvas.setPixel(nextPosX, systemLoad, MAX_COLOR);
            canvas.setPixel(nextPosX, processLoad, USED_COLOR);

            //set max height as 100%
            return 100;
        }

        return -1;
    }
}

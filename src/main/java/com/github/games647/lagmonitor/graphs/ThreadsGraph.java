package com.github.games647.lagmonitor.graphs;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.bukkit.map.MapCanvas;

public class ThreadsGraph extends GraphRenderer {

    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    public ThreadsGraph() {
        super("Thread activity");
    }

    @Override
    public int renderGraphTick(MapCanvas canvas, int nextPosX) {
        int threadCount = threadBean.getThreadCount();
        int daemonCount = threadBean.getDaemonThreadCount();

        //round up to the nearest multiple of 5
        int roundedMax = (int) (5 * (Math.ceil((float) threadCount / 5)));
        int threadHeight = getHeightPercent(roundedMax, threadCount);
        int daemonHeight = getHeightPercent(roundedMax, daemonCount);

        canvas.setPixel(nextPosX, threadHeight, MAX_COLOR);
        canvas.setPixel(nextPosX, daemonHeight, USED_COLOR);

        //these is the max number of all threads
        return threadCount;
    }
}

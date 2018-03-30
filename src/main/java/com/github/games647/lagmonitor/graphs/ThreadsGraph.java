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
        int threadHeight = getHeightScaled(roundedMax, threadCount);
        int daemonHeight = getHeightScaled(roundedMax, daemonCount);

        fillBar(canvas, nextPosX, MAX_HEIGHT - threadHeight, MAX_COLOR);
        fillBar(canvas, nextPosX, MAX_HEIGHT - daemonHeight, USED_COLOR);

        //these is the max number of all threads
        return threadCount;
    }
}

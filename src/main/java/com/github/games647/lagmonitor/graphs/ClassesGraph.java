package com.github.games647.lagmonitor.graphs;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;

import org.bukkit.map.MapCanvas;

public class ClassesGraph extends GraphRenderer {

    private final ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();

    public ClassesGraph() {
        super("Classes");
    }

    @Override
    public int renderGraphTick(MapCanvas canvas, int nextPosX) {
        int loadedClasses = classBean.getLoadedClassCount();

        //round up to the nearest multiple of 5
        int roundedMax = (int) (5 * (Math.ceil((float) loadedClasses / 5)));
        int loadedHeight = getHeightScaled(roundedMax, loadedClasses);

        for (int yPos = MAX_HEIGHT - loadedHeight; yPos < 128; yPos++) {
            canvas.setPixel(nextPosX, yPos, MAX_COLOR);
        }

        //these is the max number
        return loadedClasses;
    }
}

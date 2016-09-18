package com.github.games647.lagmonitor.graphs;

import com.github.games647.lagmonitor.NativeData;

import org.bukkit.Bukkit;
import org.bukkit.map.MapCanvas;
import org.bukkit.plugin.Plugin;

public class CpuGraph extends GraphRenderer {

    private final Plugin plugin;
    private final NativeData nativeData;

    private final Object lock = new Object();

    private int systemHeight = 0;
    private int processHeight = 0;

    public CpuGraph(Plugin plugin, NativeData nativeData) {
        super("CPU Usage");

        this.plugin = plugin;
        this.nativeData = nativeData;
    }

    @Override
    public int renderGraphTick(MapCanvas canvas, int nextPosX) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int systemLoad = (int) (nativeData.getCPULoad() * 100);
            int procsssLoad = (int) (nativeData.getProcessCPULoad() * 100);

            int localSystemHeight = getHeightScaled(100, systemLoad);
            int localProcessHeight = getHeightScaled(100, procsssLoad);

            //flush updates
            synchronized (lock) {
                this.systemHeight = localSystemHeight;
                this.processHeight = localProcessHeight;
            }
        });

        //read it only one time
        int localSystemHeight;
        int localProcessHeight;
        synchronized (lock) {
            localSystemHeight = this.systemHeight;
            localProcessHeight = this.processHeight;
        }

        for (int yPos = MAX_HEIGHT - localSystemHeight; yPos < 128; yPos++) {
            canvas.setPixel(nextPosX, yPos, MAX_COLOR);
        }

        for (int yPos = MAX_HEIGHT - localProcessHeight; yPos < 128; yPos++) {
            canvas.setPixel(nextPosX, yPos, USED_COLOR);
        }

        //set max height as 100%
        return 100;
    }
}

package com.github.games647.lagmonitor.graphs;

import com.github.games647.lagmonitor.NativeData;

import org.bukkit.Bukkit;
import org.bukkit.map.MapCanvas;
import org.bukkit.plugin.Plugin;

public class CpuGraph extends GraphRenderer {

    private final Plugin plugin;
    private final NativeData nativeData;

    private final Object lock = new Object();

    private int systemHeight;
    private int processHeight;

    public CpuGraph(Plugin plugin, NativeData nativeData) {
        super("CPU Usage");

        this.plugin = plugin;
        this.nativeData = nativeData;
    }

    @Override
    public int renderGraphTick(MapCanvas canvas, int nextPosX) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int systemLoad = (int) (nativeData.getCPULoad() * 100);
            int processLOad = (int) (nativeData.getProcessCPULoad() * 100);

            int localSystemHeight = getHeightScaled(100, systemLoad);
            int localProcessHeight = getHeightScaled(100, processLOad);

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

        fillBar(canvas, nextPosX, MAX_HEIGHT - localSystemHeight, MAX_COLOR);
        fillBar(canvas, nextPosX, MAX_HEIGHT - localProcessHeight, USED_COLOR);

        //set max height as 100%
        return 100;
    }
}

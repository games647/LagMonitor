package com.github.games647.lagmonitor.storage;

import com.github.games647.lagmonitor.LagMonitor;
import org.bukkit.Bukkit;

public class TpsSaveTask implements Runnable {

    private final LagMonitor plugin;

    public TpsSaveTask(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        final float lastSample = plugin.getTpsHistoryTask().getLastSample();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getStorage().saveTps(lastSample);
            }
        });
    }
}

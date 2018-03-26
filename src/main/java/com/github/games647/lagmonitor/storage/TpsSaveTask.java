package com.github.games647.lagmonitor.storage;

import com.github.games647.lagmonitor.LagMonitor;

import org.bukkit.Bukkit;

public class TpsSaveTask implements Runnable {

    private final LagMonitor plugin;
    private final Storage storage;

    public TpsSaveTask(LagMonitor plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    @Override
    public void run() {
        float lastSample = plugin.getTpsHistoryTask().getLastSample();
        if (lastSample > 0 && lastSample < 50) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> storage.saveTps(lastSample));
        }
    }
}

package com.github.games647.lagmonitor.storage;

import com.github.games647.lagmonitor.tasks.TpsHistoryTask;

public class TpsSaveTask implements Runnable {

    private final TpsHistoryTask tpsHistoryTask;
    private final Storage storage;

    public TpsSaveTask(TpsHistoryTask tpsHistoryTask, Storage storage) {
        this.tpsHistoryTask = tpsHistoryTask;
        this.storage = storage;
    }

    @Override
    public void run() {
        float lastSample = tpsHistoryTask.getLastSample();
        if (lastSample > 0 && lastSample < 50) {
            storage.saveTps(lastSample);
        }
    }
}

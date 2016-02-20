package com.github.games647.lagmonitor;

import com.google.common.collect.Lists;

import java.util.List;

public class TpsHistoryTask implements Runnable {

    private final List<Float> lastSeconds = Lists.newArrayListWithExpectedSize(60);

    //the last time we updated the ticks
    private long lastCheck = System.nanoTime();

    public float getLastSample() {
        if (lastSeconds.isEmpty()) {
            return 20F;
        }

        return lastSeconds.get(lastSeconds.size() - 1);
    }

    public List<Float> getLastSeconds() {
        return lastSeconds;
    }

    @Override
    public void run() {
        //nanoTime is more accurate
        long currentTime = System.nanoTime();
        long timeSpent = currentTime - lastCheck;
        //update the last check
        lastCheck = currentTime;

        //how many ticks passed since the last check * 1000 to convert to seconds
        float tps = 1 * 20 * 1000.0F / (timeSpent / (1000 * 1000));
        if (tps >= 0.0F && tps < 25.0F) {
            //Prevent all invalid values
            lastSeconds.add(tps);
            if (lastSeconds.size() >= 61) {
                lastSeconds.remove(0);
            }
        }
    }
}

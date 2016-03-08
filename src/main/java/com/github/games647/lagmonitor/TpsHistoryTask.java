package com.github.games647.lagmonitor;

import com.google.common.collect.Lists;

import java.util.LinkedList;
import java.util.List;

public class TpsHistoryTask implements Runnable {

    private static final int ONE_MINUTE = 60;

    private final LinkedList<Float> lastSeconds = Lists.newLinkedList();

    //the last time we updated the ticks
    private long lastCheck = System.nanoTime();

    public float getLastSample() {
        if (lastSeconds.isEmpty()) {
            return 20F;
        }

        return lastSeconds.getLast();
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
            if (lastSeconds.size() >= ONE_MINUTE + 1) {
                lastSeconds.removeFirst();
            }
        }
    }
}

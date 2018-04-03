package com.github.games647.lagmonitor.tasks;

import com.github.games647.lagmonitor.utils.RollingOverHistory;

public class TpsHistoryTask implements Runnable {

    public static final int RUN_INTERVAL = 20;
    private static final int ONE_MINUTE = 60;

    private final RollingOverHistory minuteSample = new RollingOverHistory(ONE_MINUTE, 20.0F);
    private final RollingOverHistory quarterSample = new RollingOverHistory(ONE_MINUTE * 15, 20.0F);
    private final RollingOverHistory halfHourSample = new RollingOverHistory(ONE_MINUTE * 30, 20.0F);

    //the last time we updated the ticks
    private long lastCheck = System.nanoTime();

    public RollingOverHistory getMinuteSample() {
        return minuteSample;
    }

    public RollingOverHistory getQuarterSample() {
        return quarterSample;
    }

    public RollingOverHistory getHalfHourSample() {
        return halfHourSample;
    }

    public float getLastSample() {
        synchronized (this) {
            int lastPos = minuteSample.getCurrentPosition();
            return minuteSample.getSamples()[lastPos];
        }
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
            synchronized (this) {
                minuteSample.add(tps);
                quarterSample.add(tps);
                halfHourSample.add(tps);
            }
        }
    }
}

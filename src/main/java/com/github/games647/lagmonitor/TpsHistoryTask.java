package com.github.games647.lagmonitor;

public class TpsHistoryTask implements Runnable {

    private float lastTicks = 20.0F;

    //the last time we updated the ticks
    private long lastCheck;

    /**
     * Get the ticks count of the last check. 20 Ticks should pass per second
     *
     * @return the ticks count of the last check
     */
    public float getLastTicks() {
        return lastTicks;
    }

    @Override
    public void run() {
        //nanoTime is more accurate
        long currentTime = System.nanoTime();
        long timeSpent = currentTime - lastCheck;
        //update the last check
        lastCheck = currentTime;

        //how many ticks passed since the last check * 1000 to convert to seconds
        float tps = 3 * 20 * 1000.0F / (timeSpent / (1000 * 1000));
        if (tps >= 0.0F && tps < 20.0F) {
            //Prevent all invalid values
            lastTicks = tps;
        }
    }
}

package com.github.games647.lagmonitor;

public class RollingOverHistory {

    private final float[] samples;
    private float total;

    private int currentPosition = 1;
    private int currentSize = 1;

    public RollingOverHistory(int size, float firstValue) {
        this.samples = new float[size];
        samples[0] = firstValue;
    }

    public void add(float sample) {
        currentPosition++;
        if (currentPosition >= samples.length) {
            //we reached the end - go back to the beginning
            currentPosition = 0;
        }

        if (currentSize < samples.length) {
            //array is not full yet
            currentSize++;
        } else {
            //delete the latest sample which wil be overridden
            total -= samples[currentPosition];
        }

        total += sample;
        samples[currentPosition] = sample;
    }

    public float getAverage() {
        return total / currentSize;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public float getLastSample() {
        int lastPos = currentPosition - 1;
        if (lastPos < 0) {
            lastPos = samples.length - 1;
        }

        return samples[lastPos];
    }

    public float[] getSamples() {
        return samples;
    }
}

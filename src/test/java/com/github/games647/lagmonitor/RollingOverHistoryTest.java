package com.github.games647.lagmonitor;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class RollingOverHistoryTest {

    @Test
    public void testGetAverage() {
        RollingOverHistory history = new RollingOverHistory(4, 1);

        Assert.assertEquals(1, history.getAverage(), 0.1);
        history.add(3);
        Assert.assertEquals(2, history.getAverage(), 0.1);
        history.add(2);
        Assert.assertEquals(2, history.getAverage(), 0.1);
        history.add(3);
        Assert.assertEquals(2.25, history.getAverage(), 0.1);
    }

    @Test
    public void testGetCurrentPosition() {
        RollingOverHistory history = new RollingOverHistory(2, 1);

        Assert.assertEquals(0, history.getCurrentPosition());
        history.add(2);

        Assert.assertEquals(1, history.getCurrentPosition());
        history.add(2);
        //reached the max size
        Assert.assertEquals(0, history.getCurrentPosition());
    }

    @Test
    public void testGetLastSample() {
        RollingOverHistory history = new RollingOverHistory(3, 1);

        Assert.assertEquals(1, history.getLastSample(), 0.1);
        history.add(2);
        System.out.println(Arrays.toString(history.getSamples()));
        Assert.assertEquals(2, history.getLastSample(), 0.1);
        history.add(3);
        Assert.assertEquals(3, history.getLastSample(), 0.1);
        history.add(2);
        Assert.assertEquals(2, history.getLastSample(), 0.1);
    }

    @Test
    public void testGetSamples() {
        RollingOverHistory history = new RollingOverHistory(1, 1);

        history.add(2);
        float[] expected = new float[]{2.0F};
        Assert.assertArrayEquals(expected, history.getSamples(), 0.1F);
    }
}

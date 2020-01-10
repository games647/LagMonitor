package com.github.games647.lagmonitor;

import com.github.games647.lagmonitor.util.RollingOverHistory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RollingOverHistoryTest {

    @Test
    public void testGetAverage() {
        RollingOverHistory history = new RollingOverHistory(4, 1);

        assertEquals(1.0F, history.getAverage());
        history.add(3);
        assertEquals(2.0F, history.getAverage());
        history.add(2);
        assertEquals(2.0F, history.getAverage());
        history.add(3);
        assertEquals(2.25F, history.getAverage());
    }

    @Test
    public void testGetCurrentPosition() {
        RollingOverHistory history = new RollingOverHistory(2, 1);

        assertEquals(0, history.getCurrentPosition());
        history.add(2);

        assertEquals(1, history.getCurrentPosition());
        history.add(2);
        //reached the max size
        assertEquals(0, history.getCurrentPosition());
    }

    @Test
    public void testGetLastSample() {
        RollingOverHistory history = new RollingOverHistory(3, 1);

        assertEquals(1.0, history.getLastSample());
        history.add(2);
        assertEquals(2.0, history.getLastSample());
        history.add(3);
        assertEquals(3.0, history.getLastSample());
        history.add(2);
        assertEquals(2.0, history.getLastSample());
    }

    @Test
    public void testGetSamples() {
        RollingOverHistory history = new RollingOverHistory(1, 1);

        history.add(2);
        assertArrayEquals(new float[]{2.0F}, history.getSamples());
    }
}

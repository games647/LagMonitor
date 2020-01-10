package com.github.games647.lagmonitor;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LagMonitorTest {

    @Test
    public void testEmptyDuration() {
        assertEquals("'0' days '0' hours '0' minutes '0' seconds'", LagMonitor.formatDuration(Duration.ZERO));
    }

    @Test
    public void testOverYearDuration() {
        String expected = "'362' days '0' hours '0' minutes '0' seconds'";
        assertEquals(expected, LagMonitor.formatDuration(Duration.ofDays(362)));
    }

    @Test
    public void testValidSecondDuration() {
        String expected = "'0' days '0' hours '0' minutes '1' seconds'";
        assertEquals(expected, LagMonitor.formatDuration(Duration.ofSeconds(1)));
    }

    @Test
    public void testOverSecondDuration() {
        String expected = "'0' days '0' hours '1' minutes '15' seconds'";
        assertEquals(expected, LagMonitor.formatDuration(Duration.ofSeconds(75)));
    }

    @Test
    public void testFormattingCombined() {
        String expected = "'0' days '0' hours '13' minutes '15' seconds'";
        assertEquals(expected, LagMonitor.formatDuration(Duration.ofSeconds(75).plusMinutes(12)));
    }
}

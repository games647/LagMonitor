package com.github.games647.lagmonitor;

import java.time.Duration;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.*;

public class LagMonitorTest {

    @Test
    public void testEmptyDuration() {
        assertThat(LagMonitor.formatDuration(Duration.ZERO), is("'0' days '0' hours '0' minutes '0' seconds'"));
    }

    @Test
    public void testOverYearDuration() {
        assertThat(LagMonitor.formatDuration(Duration.ofDays(362)), is("'362' days '0' hours '0' minutes '0' seconds'"));
    }

    @Test
    public void testValidSecondDuration() {
        assertThat(LagMonitor.formatDuration(Duration.ofSeconds(1)), is("'0' days '0' hours '0' minutes '1' seconds'"));
    }

    @Test
    public void testOverSecondDuration() {
        String expected = "'0' days '0' hours '1' minutes '15' seconds'";
        assertThat(LagMonitor.formatDuration(Duration.ofSeconds(75)), is(expected));
    }

    @Test
    public void testFormattingCombined() {
        String expected = "'0' days '0' hours '13' minutes '15' seconds'";
        assertThat(LagMonitor.formatDuration(Duration.ofSeconds(75).plusMinutes(12)), is(expected));
    }

}

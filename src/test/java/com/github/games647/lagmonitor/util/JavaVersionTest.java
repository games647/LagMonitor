package com.github.games647.lagmonitor.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavaVersionTest {

    @Test
    public void detectDeveloperVersion() {
        assertNotNull(JavaVersion.detect());
    }

    @Test
    public void parseJava8() {
        JavaVersion version = new JavaVersion("1.8.0_161");
        assertEquals(8, version.getMajor());
        assertEquals(8, version.getMinor());
        assertEquals(1, version.getSecurity());
        assertTrue(version.isOutdated());
    }

    @Test
    public void parseJava9() {
        JavaVersion version = new JavaVersion("9.0.4");
        assertEquals(9, version.getMajor());
        assertEquals(0, version.getMinor());
        assertEquals(4, version.getSecurity());
        assertTrue(version.isOutdated());
    }

    @Test
    public void parseJava9EarlyAccess() {
        JavaVersion version = new JavaVersion("9-ea");
        assertEquals(9, version.getMajor());
        assertEquals(0, version.getMinor());
        assertEquals(0, version.getSecurity());
        assertTrue(version.isPreRelease());
        assertTrue(version.isOutdated());
    }

    @Test
    public void parseJava9WithVendorSuffix() {
        JavaVersion version = new JavaVersion("9-Ubuntu");
        assertEquals(9, version.getMajor());
        assertEquals(0, version.getMinor());
        assertEquals(0, version.getSecurity());
        assertTrue(version.isOutdated());
    }

    @Test
    public void parseJava14() {
        JavaVersion version = new JavaVersion("14.0.1");
        assertEquals(14, version.getMajor());
        assertEquals(0, version.getMinor());
        assertEquals(1, version.getSecurity());
        assertFalse(version.isOutdated());
    }

    @Test
    public void parseJava10Internal() {
        JavaVersion version = new JavaVersion("10-internal");
        assertEquals(10, version.getMajor());
        assertEquals(0, version.getMinor());
        assertEquals(0, version.getSecurity());
        assertTrue(version.isPreRelease());
        assertTrue(version.isOutdated());
    }

    @Test
    public void comparePreRelease() {
        JavaVersion lower = new JavaVersion("10-internal");
        JavaVersion higher = new JavaVersion("10");
        assertEquals(-1, lower.compareTo(higher));
    }

    @Test
    public void compareMinor() {
        JavaVersion lower = new JavaVersion("9.0.3");
        JavaVersion higher = new JavaVersion("9.0.4");
        assertEquals(1, higher.compareTo(lower));
    }

    @Test
    public void compareMajor() {
        JavaVersion lower = new JavaVersion("1.8.0_161");
        JavaVersion higher = new JavaVersion("10");
        assertEquals(1, higher.compareTo(lower));
    }

    @Test
    public void compareEqual() {
        JavaVersion lower = new JavaVersion("10-Ubuntu");
        JavaVersion higher = new JavaVersion("10");
        assertEquals(0, lower.compareTo(higher));
    }
}

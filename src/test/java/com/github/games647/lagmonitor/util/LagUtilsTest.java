package com.github.games647.lagmonitor.util;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LagUtilsTest {

    @Test
    public void byteToMega() {
        assertEquals(1, LagUtils.byteToMega(1024 * 1024));
        assertEquals(0, LagUtils.byteToMega(1000 * 1000));
    }

    @Test
    public void readableBytes() {
        //make tests that have a constant floating point separator (, vs .)
        Locale.setDefault(Locale.ENGLISH);

        assertEquals("1.00 kiB", LagUtils.readableBytes(1024));
        assertEquals("64 B", LagUtils.readableBytes(64));
        assertEquals("1.00 MiB", LagUtils.readableBytes(1024 * 1024 + 12));
    }
}

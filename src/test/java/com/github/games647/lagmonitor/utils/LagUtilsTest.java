package com.github.games647.lagmonitor.utils;

import java.util.Locale;

import org.junit.Test;

import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertThat;

public class LagUtilsTest {

    @Test
    public void byteToMega() {
        assertThat(LagUtils.byteToMega(1024 * 1024), is(1));
        assertThat(LagUtils.byteToMega(1000 * 1000), is(0));
    }

    @Test
    public void readableBytes() {
        //make tests that have a constant floating point separator (, vs .)
        Locale.setDefault(Locale.ENGLISH);

        assertThat(LagUtils.readableBytes(1024), is("1.00 kiB"));
        assertThat(LagUtils.readableBytes(64), is("64 B"));
        assertThat(LagUtils.readableBytes(1024 * 1024 + 12), is("1.00 MiB"));
    }
}

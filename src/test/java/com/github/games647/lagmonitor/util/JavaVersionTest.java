package com.github.games647.lagmonitor.util;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

import static org.junit.Assert.assertThat;

public class JavaVersionTest {

    @Test
    public void detectDeveloperVersion() {
        assertThat(JavaVersion.detect(), notNullValue());
    }

    @Test
    public void parseJava8() {
        JavaVersion version = new JavaVersion("1.8.0_161");
        assertThat(version.getMajor(), is(8));
        assertThat(version.getMinor(), is(8));
        assertThat(version.getSecurity(), is(1));
        assertThat(version.isOutdated(), is(true));
    }

    @Test
    public void parseJava9() {
        JavaVersion version = new JavaVersion("9.0.4");
        assertThat(version.getMajor(), is(9));
        assertThat(version.getMinor(), is(0));
        assertThat(version.getSecurity(), is(4));
        assertThat(version.isOutdated(), is(true));
    }

    @Test
    public void parseJava9EarlyAccess() {
        JavaVersion version = new JavaVersion("9-ea");
        assertThat(version.getMajor(), is(9));
        assertThat(version.getMinor(), is(0));
        assertThat(version.getSecurity(), is(0));
        assertThat(version.isPreRelease(), is(true));
        assertThat(version.isOutdated(), is(true));
    }

    @Test
    public void parseJava9WithVendorSuffix() {
        JavaVersion version = new JavaVersion("9-Ubuntu");
        assertThat(version.getMajor(), is(9));
        assertThat(version.getMinor(), is(0));
        assertThat(version.getSecurity(), is(0));
        assertThat(version.isOutdated(), is(true));
    }

    @Test
    public void parseJava10() {
        JavaVersion version = new JavaVersion("10.0.2");
        assertThat(version.getMajor(), is(10));
        assertThat(version.getMinor(), is(0));
        assertThat(version.getSecurity(), is(2));
        assertThat(version.isOutdated(), is(false));
    }

    @Test
    public void parseJava10Internal() {
        JavaVersion version = new JavaVersion("10-internal");
        assertThat(version.getMajor(), is(10));
        assertThat(version.getMinor(), is(0));
        assertThat(version.getSecurity(), is(0));
        assertThat(version.isPreRelease(), is(true));
        assertThat(version.isOutdated(), is(true));
    }

    @Test
    public void comparePreRelease() {
        JavaVersion lower = new JavaVersion("10-internal");
        JavaVersion higher = new JavaVersion("10");
        assertThat(lower.compareTo(higher), is(-1));

        lower = new JavaVersion("9.0.3");
        higher = new JavaVersion("9.0.4");
        assertThat(higher.compareTo(lower), is(1));
    }

    @Test
    public void compareMajor() {
        JavaVersion lower = new JavaVersion("1.8.0_161");
        JavaVersion higher = new JavaVersion("10");
        assertThat(higher.compareTo(lower), is(1));
    }

    @Test
    public void compareEqual() {
        JavaVersion lower = new JavaVersion("10-Ubuntu");
        JavaVersion higher = new JavaVersion("10");
        assertThat(lower.compareTo(higher), is(0));
    }
}

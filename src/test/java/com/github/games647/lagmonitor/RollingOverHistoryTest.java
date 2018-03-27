package com.github.games647.lagmonitor;

import com.github.games647.lagmonitor.utils.RollingOverHistory;

import java.util.Arrays;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

public class RollingOverHistoryTest {

    @Test
    public void testGetAverage() {
        RollingOverHistory history = new RollingOverHistory(4, 1);

        assertThat(history.getAverage(), is(1.0F));
        history.add(3);
        assertThat(history.getAverage(), is(2.0F));
        history.add(2);
        assertThat(history.getAverage(), is(2.0F));
        history.add(3);
        assertThat(history.getAverage(), is(2.25F));
    }

    @Test
    public void testGetCurrentPosition() {
        RollingOverHistory history = new RollingOverHistory(2, 1);

        assertThat(history.getCurrentPosition(), is(0));
        history.add(2);

        assertThat(history.getCurrentPosition(), is(1));
        history.add(2);
        //reached the max size
        assertThat(history.getCurrentPosition(), is(0));
    }

    @Test
    public void testGetLastSample() {
        RollingOverHistory history = new RollingOverHistory(3, 1);

        assertThat(history.getLastSample(), is(1.0F));
        history.add(2);
        assertThat(history.getLastSample(), is(2.0F));
        history.add(3);
        assertThat(history.getLastSample(), is(3.0F));
        history.add(2);
        assertThat(history.getLastSample(), is(2.0F));
    }

    @Test
    public void testGetSamples() {
        RollingOverHistory history = new RollingOverHistory(1, 1);

        history.add(2);
        float[] expected = {2.0F};
        assertThat(Arrays.asList(history.getSamples()), everyItem(is(expected)));
    }
}

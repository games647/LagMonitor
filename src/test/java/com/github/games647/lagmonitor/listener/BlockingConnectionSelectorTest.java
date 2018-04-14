package com.github.games647.lagmonitor.listener;

import com.github.games647.lagmonitor.threading.BlockingActionManager;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BlockingConnectionSelectorTest {

    @Mock
    private BlockingActionManager actionManager;
    private BlockingConnectionSelector selector;

    @Before
    public void setUp() throws Exception {
        this.selector = new BlockingConnectionSelector(actionManager);
    }

    @Test
    public void testHttp() throws Exception {
        selector.select(URI.create("https://spigotmc.org"));
        verify(actionManager, times(1)).checkBlockingAction(anyString());
    }

    @Test
    public void testDuplicateHttp() throws Exception {
        //http creates to proxy selector events one for http address and one for the socket one
        //the second one should be ignored
        selector.select(URI.create("socket://api.mojang.com:443"));
        verify(actionManager, times(0)).checkBlockingAction(anyString());
    }

    @Test
    public void testBlockingSocket() throws Exception {
        selector.select(URI.create("socket://api.mojang.com:50"));
        verify(actionManager, times(1)).checkBlockingAction(anyString());
    }
}

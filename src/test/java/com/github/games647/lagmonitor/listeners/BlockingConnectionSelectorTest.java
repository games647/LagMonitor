package com.github.games647.lagmonitor.listeners;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.threading.BlockingActionManager;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(PowerMockRunner.class)
public class BlockingConnectionSelectorTest {

    private BlockingConnectionSelector selector;
    private BlockingActionManager actionManager;

    @Before
    public void setUp() throws Exception {
        LagMonitor plugin = mock(LagMonitor.class);

        this.actionManager = mock(BlockingActionManager.class);
        doReturn(actionManager).when(plugin).getBlockingActionManager();

        this.selector = new BlockingConnectionSelector(plugin, ProxySelector.getDefault());
    }

    @Test
    public void testHttp() throws Exception {
        selector.select(new URL("https://spigotmc.org").toURI());
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

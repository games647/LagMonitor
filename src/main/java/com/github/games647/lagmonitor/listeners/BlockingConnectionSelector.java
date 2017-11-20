package com.github.games647.lagmonitor.listeners;

import com.github.games647.lagmonitor.threading.BlockingActionManager;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

public class BlockingConnectionSelector extends ProxySelector {

    private final BlockingActionManager actionManager;
    private final ProxySelector oldProxySelector;

    public BlockingConnectionSelector(BlockingActionManager actionManager, ProxySelector oldProxySelector) {
        this.actionManager = actionManager;
        this.oldProxySelector = oldProxySelector;
    }

    @Override
    public List<Proxy> select(URI uri) {
        String url = uri.toString().replace("www", "");
        if (uri.getScheme().startsWith("http") || (uri.getPort() != 80 && uri.getPort() != 443)) {
            actionManager.checkBlockingAction("Socket: " + url);
        }

        return oldProxySelector == null ? Lists.newArrayList(Proxy.NO_PROXY) : oldProxySelector.select(uri);
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        if (oldProxySelector != null) {
            oldProxySelector.connectFailed(uri, sa, ioe);
        }
    }

    public ProxySelector getOldProxySelector() {
        return oldProxySelector;
    }
}

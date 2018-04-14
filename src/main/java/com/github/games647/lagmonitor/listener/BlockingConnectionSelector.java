package com.github.games647.lagmonitor.listener;

import com.github.games647.lagmonitor.threading.BlockingActionManager;
import com.github.games647.lagmonitor.threading.Injectable;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class BlockingConnectionSelector extends ProxySelector implements Injectable {

    private static final Pattern WWW_PATERN = Pattern.compile("www", Pattern.LITERAL);

    private final BlockingActionManager actionManager;
    private ProxySelector oldProxySelector;

    public BlockingConnectionSelector(BlockingActionManager actionManager) {
        this.actionManager = actionManager;
    }

    @Override
    public List<Proxy> select(URI uri) {
        String url = WWW_PATERN.matcher(uri.toString()).replaceAll("");
        if (uri.getScheme().startsWith("http") || (uri.getPort() != 80 && uri.getPort() != 443)) {
            actionManager.checkBlockingAction("Socket: " + url);
        }

        return oldProxySelector == null ? Collections.singletonList(Proxy.NO_PROXY) : oldProxySelector.select(uri);
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        if (oldProxySelector != null) {
            oldProxySelector.connectFailed(uri, sa, ioe);
        }
    }

    @Override
    public void inject() {
        ProxySelector proxySelector = ProxySelector.getDefault();
        if (proxySelector != this) {
            oldProxySelector = proxySelector;
            ProxySelector.setDefault(this);
        }
    }

    @Override
    public void restore() {
        if (ProxySelector.getDefault() == this) {
            ProxySelector.setDefault(oldProxySelector);
        }
    }
}

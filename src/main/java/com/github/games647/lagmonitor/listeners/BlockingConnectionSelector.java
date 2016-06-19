package com.github.games647.lagmonitor.listeners;

import com.github.games647.lagmonitor.LagMonitor;
import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;

public class BlockingConnectionSelector extends ProxySelector {

    private final LagMonitor plugin;
    private final ProxySelector oldProxySelector;

    public BlockingConnectionSelector(LagMonitor plugin, ProxySelector oldProxySelector) {
        this.plugin = plugin;
        this.oldProxySelector = oldProxySelector;
    }

    @Override
    public List<Proxy> select(URI uri) {
        if (Bukkit.isPrimaryThread()) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            plugin.getLogger().log(Level.WARNING, "Server is performing a blocking socket connection on the main thread"
                    , stackTrace);
        }

        return oldProxySelector.select(uri);
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        oldProxySelector.connectFailed(uri, sa, ioe);
    }

    public ProxySelector getOldProxySelector() {
        return oldProxySelector;
    }
}

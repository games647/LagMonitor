package com.github.games647.lagmonitor.listeners;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.PluginUtil;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class BlockingConnectionSelector extends ProxySelector {

    private final LagMonitor plugin;
    private final ProxySelector oldProxySelector;

    private final Set<String> violatedPlugins = Sets.newHashSet();

    public BlockingConnectionSelector(LagMonitor plugin, ProxySelector oldProxySelector) {
        this.plugin = plugin;
        this.oldProxySelector = oldProxySelector;
    }

    @Override
    public List<Proxy> select(URI uri) {
        if (!uri.getScheme().startsWith("http") && Bukkit.isPrimaryThread()) {
            Exception stackTraceCreator = new Exception();
            StackTraceElement[] stackTrace = stackTraceCreator.getStackTrace();

            //remove the parts from LagMonitor
            StackTraceElement[] copyOfRange = Arrays.copyOfRange(stackTrace, 1, stackTrace.length);
            Entry<Plugin, StackTraceElement> foundPlugin = PluginUtil.findPlugin(copyOfRange);
            String pluginName = "unknown";
            if (foundPlugin != null) {
                pluginName = foundPlugin.getKey().getName();
                if (!violatedPlugins.add(pluginName) && plugin.getConfig().getBoolean("oncePerPlugin")) {
                    return oldProxySelector.select(uri);
                }
            }

            plugin.getLogger().log(Level.WARNING, "Plugin {0} is performing a blocking action to {1} on the main thread"
                    + " This could be a performance hit."
                    + " Report it to the plugin author", new Object[]{pluginName, uri});

            if (plugin.getConfig().getBoolean("hideStacktrace")) {
                if (foundPlugin != null) {
                    StackTraceElement source = foundPlugin.getValue();
                    plugin.getLogger().log(Level.WARNING, "Source: {0}, method {1}, line{2}"
                            , new Object[]{source.getClassName(), source.getMethodName(), source.getLineNumber()});
                }
            } else {
                plugin.getLogger().log(Level.WARNING, "", stackTraceCreator);
            }
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

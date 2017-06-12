package com.github.games647.lagmonitor.threading;

import com.github.games647.lagmonitor.LagMonitor;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class BlockingActionManager implements Listener {

    private final LagMonitor plugin;

    private final Set<PluginViolation> violations = Sets.newConcurrentHashSet();
    private final Set<String> violatedPlugins = Sets.newConcurrentHashSet();

    private final Map<ClassLoader, Plugin> cachedLoaders = Maps.newHashMap();

    public BlockingActionManager(LagMonitor plugin) {
        this.plugin = plugin;

        synchronized (Bukkit.getPluginManager()) {
            for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
                cachedLoaders.put(pl.getClass().getClassLoader(), pl);
            }
        }
    }

    public void checkBlockingAction(String event) {
        if (Bukkit.isPrimaryThread()) {
            String message = "Plugin {0} is performing a blocking action on the main thread. "
                    + "This could be a performance hit {1}. " +
                    "Such actions should be handled async from the main thread. ";
            logCurrentStack(message, event);
        }
    }

    public void checkThreadSafety(String eventName) {
        if (!Bukkit.isPrimaryThread()) {
            String message = "Plugin {0} is performed a async operation for an sync Event. "
                    + "This could be a very dangerous {1}. ";
            logCurrentStack(message, eventName);
        }
    }

    public void logCurrentStack(String format, String eventName) {
        IllegalAccessException stackTraceCreator = new IllegalAccessException();
        StackTraceElement[] stackTrace = stackTraceCreator.getStackTrace();

        Map.Entry<String, StackTraceElement> foundPlugin = findPlugin(stackTrace);

        PluginViolation violation = new PluginViolation(eventName);
        if (foundPlugin != null) {
            String pluginName = foundPlugin.getKey();
            violation = new PluginViolation(pluginName, foundPlugin.getValue(), eventName);
            if (pluginName.equals("Vanilla")) {
                return;
            }

            if (!violatedPlugins.add(violation.getPluginName()) && plugin.getConfig().getBoolean("oncePerPlugin")) {
                return;
            }
        }

        if (!violations.add(violation)) {
            return;
        }

        plugin.getLogger().log(Level.WARNING, format + "Report it to the plugin author"
                , new Object[]{violation.getPluginName(), eventName});

        if (plugin.getConfig().getBoolean("hideStacktrace")) {
            plugin.getLogger().log(Level.WARNING, "Source: {0}, method {1}, line {2}"
                    , new Object[]{violation.getSourceFile(), violation.getMethodName(), violation.getLineNumber()});
        } else {
            plugin.getLogger().log(Level.WARNING, "The following exception is not an error. " +
                    "It's a hint for the plugin developer to find the source of the threading action. " +
                    plugin.getName() + " doesn't prevent this action. It just warns you", stackTraceCreator);
        }
    }

    public Map.Entry<String, StackTraceElement> findPlugin(StackTraceElement[] stacktrace) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        boolean skipping = true;
        for (StackTraceElement elem : stacktrace) {
            try {
                Class<?> clazz = Class.forName(elem.getClassName());
                ClassLoader searchingClazzLoader = clazz.getClassLoader();
                if (skipping) {

                    if (searchingClazzLoader == classLoader) {
                        continue;
                    }

                    skipping = false;
                }

                Plugin plugin = cachedLoaders.get(searchingClazzLoader);
                if (plugin != null) {
                    Map<String, StackTraceElement> map = Maps.newHashMapWithExpectedSize(1);
                    map.put(plugin.getName(), elem);
                    return map.entrySet().iterator().next();
                } else if (clazz.getSimpleName().equals("VanillaCommandWrapper")) {
                    Map<String, StackTraceElement> map = Maps.newHashMapWithExpectedSize(1);
                    map.put("Vanilla", elem);
                    return map.entrySet().iterator().next();
                }
            } catch (ClassNotFoundException ex) {
                //if this class cannot be loaded then it could be something native so we ignore it
            }
        }

        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPluginEnable(PluginEnableEvent event) {
        cachedLoaders.remove(event.getPlugin().getClass().getClassLoader());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPluginDisable(PluginDisableEvent event) {
        Plugin plugin = event.getPlugin();
        cachedLoaders.put(plugin.getClass().getClassLoader(), plugin);
    }
}

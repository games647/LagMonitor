package com.github.games647.lagmonitor.threading;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockingActionManager implements Listener {

    private final Plugin plugin;

    private final Set<PluginViolation> violations = Sets.newConcurrentHashSet();
    private final Set<String> violatedPlugins = Sets.newConcurrentHashSet();

    public BlockingActionManager(Plugin plugin) {
        this.plugin = plugin;
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
            String message = "Plugin {0} did a async operation for an sync Event {1}. "
                    + "This could cause server stability issues. ";
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
                    "It's a hint for the plugin developer to find the source. " +
                    plugin.getName() + " doesn't prevent this action. It just warns you about it", stackTraceCreator);
        }
    }

    public Map.Entry<String, StackTraceElement> findPlugin(StackTraceElement[] stacktrace) {
        boolean skipping = true;
        for (StackTraceElement elem : stacktrace) {
            try {
                Class<?> clazz = Class.forName(elem.getClassName());
                if (clazz.getName().endsWith("VanillaCommandWrapper")) {
                    //explicit use getName instead of SimpleName because getSimpleBinaryName causes a
                    //StringIndexOutOfBoundsException for obfuscated plugins
                    return Maps.immutableEntry("Vanilla", elem);
                }

                Plugin plugin;
                try {
                    plugin = JavaPlugin.getProvidingPlugin(clazz);
                    if (plugin == this.plugin) {
                        continue;
                    }

                    return Maps.immutableEntry(plugin.getName(), elem);
                } catch (IllegalArgumentException illegalArgumentEx) {
                    //ignore
                }
            } catch (ClassNotFoundException ex) {
                //if this class cannot be loaded then it could be something native so we ignore it
            }
        }

        return null;
    }
}

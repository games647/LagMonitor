package com.github.games647.lagmonitor.threading;

import com.github.games647.lagmonitor.LagMonitor;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class BlockingActionManager {

    private final LagMonitor plugin;

    private final Set<PluginViolation> violations = Sets.newConcurrentHashSet();
    private final Set<String> violatedPlugins = Sets.newConcurrentHashSet();

    public BlockingActionManager(LagMonitor plugin) {
        this.plugin = plugin;
    }

    public void checkBlockingAction(String event) {
        if (Bukkit.isPrimaryThread()) {
            String message = "Plugin {0} is performing a threading action on the main thread "
                    + "This could be a performance hit {1}. Such actions should be handled async from the main thread";
            logCurrentStack(message, event);
        }
    }

    public void checkThreadSafety(String eventName) {
        if (!Bukkit.isPrimaryThread()) {
            String message = "Plugin {0} is performed a async operation for an sync Event "
                    + "This could be a very dangerous {1}.";
            logCurrentStack(message, eventName);
        }
    }

    private void logCurrentStack(String format, String eventName) {
        IllegalAccessException stackTraceCreator = new IllegalAccessException();
        StackTraceElement[] stackTrace = stackTraceCreator.getStackTrace();

        //remove the parts from LagMonitor
        StackTraceElement[] copyOfRange = Arrays.copyOfRange(stackTrace, 2, stackTrace.length);
        Map.Entry<Plugin, StackTraceElement> foundPlugin = PluginUtil.findPlugin(copyOfRange);

        PluginViolation violation = new PluginViolation(eventName);
        if (foundPlugin != null) {
            String pluginName = foundPlugin.getKey().getName();
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
                    "It's a hint for the plugin developer to find the source of the threading action. " +
                    plugin.getName() + " doesn't prevent this action. It just warns you", stackTraceCreator);
        }
    }
}

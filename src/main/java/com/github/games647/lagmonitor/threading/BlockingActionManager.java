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

    //feel free to improve the wording of this:
    private static final String THREAD_SAFETY_NOTICE = "As threads **can** run concurrently or in parallel " +
            "shared data access has to be synchronized (thread-safety) in order to prevent " +
            "unexpected behavior or crashes. ";

    private static final String SAFETY_METHODS = "You can guarantee thread-safety by " +
            "running the data access always on the same thread, using atomic operations, " +
            "locks (ex: a synchronized block), immutable objects, thread local data " +
            "or something similar. ";

    private static final String COMMON_SAFE = "Common things that are thread-safe: Logging, Bukkit Scheduler, " +
            "Concurrent collections (ex: ConcurrentHashMap or Collections.synchronized*), ... ";

    private static final String BLOCKING_ACTION_MESSAGE = "Plugin {0} is performing a blocking I/O operation ({1}) " +
            "on the main thread. " +
            "This could affect the server performance, because the thread pauses until it gets the response. " +
            "Such operations should be performed asynchronous from the main thread. " +
            "Besides gameplay performance it could also improve startup time. " +
            "Keep in mind to keep the code thread-safe. ";

    private final Plugin plugin;

    private final Set<PluginViolation> violations = Sets.newConcurrentHashSet();
    private final Set<String> violatedPlugins = Sets.newConcurrentHashSet();

    public BlockingActionManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void checkBlockingAction(String event) {
        if (!Bukkit.isPrimaryThread()) {
            return;
        }

        logCurrentStack(BLOCKING_ACTION_MESSAGE, event);
    }

    public void checkThreadSafety(String eventName) {
        if (Bukkit.isPrimaryThread()) {
            return;
        }

        logCurrentStack("Plugin {0} triggered an synchronous event {1} from an asynchronous Thread. "
            + THREAD_SAFETY_NOTICE
            + "Use runTask* (no Async*), scheduleSync* or callSyncMethod to run on the main thread.", eventName);
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
                    "It's a hint for the plugin developers to find the source. " +
                    plugin.getName() + " doesn't prevent this action. It just warns you about it. ", stackTraceCreator);
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

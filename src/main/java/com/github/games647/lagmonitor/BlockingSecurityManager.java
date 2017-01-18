package com.github.games647.lagmonitor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.io.FilePermission;
import java.net.SocketPermission;
import java.security.Permission;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class BlockingSecurityManager extends SecurityManager {

    private final LagMonitor plugin;
    private final SecurityManager delegate;

    private final Set<PluginViolation> violations = Sets.newConcurrentHashSet();
    private final Set<String> violatedPlugins = Sets.newConcurrentHashSet();
    private final Set<String> fileWhitelist = ImmutableSet.of(".jar", "session.lock");

    public BlockingSecurityManager(LagMonitor plugin, SecurityManager delegate) {
        this.plugin = plugin;

        this.delegate = delegate;
    }

    public BlockingSecurityManager(LagMonitor plugin) {
        this(plugin, null);
    }

    public SecurityManager getOldSecurityManager() {
        return delegate;
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        if (delegate != null) {
            delegate.checkPermission(perm, context);
        }

        checkMainThreadOperation(perm);
    }

    @Override
    public void checkPermission(Permission perm) {
        if (delegate != null) {
            delegate.checkPermission(perm);
        }

        checkMainThreadOperation(perm);
    }

    private void checkMainThreadOperation(Permission perm) {
        if (Bukkit.isPrimaryThread() && isBlockingAction(perm)) {
            Exception stackTraceCreator = new Exception();
            StackTraceElement[] stackTrace = stackTraceCreator.getStackTrace();

            //remove the parts from LagMonitor
            StackTraceElement[] copyOfRange = Arrays.copyOfRange(stackTrace, 2, stackTrace.length);
            Entry<Plugin, StackTraceElement> foundPlugin = PluginUtil.findPlugin(copyOfRange);

            PluginViolation violation = new PluginViolation(perm.getName());
            if (foundPlugin != null) {
                String pluginName = foundPlugin.getKey().getName();
                violation = new PluginViolation(pluginName, foundPlugin.getValue(), perm.getName());

                if (!violatedPlugins.add(violation.getPluginName()) && plugin.getConfig().getBoolean("oncePerPlugin")) {
                    return;
                }
            }

            if (!violations.add(violation)) {
                return;
            }

            plugin.getLogger().log(Level.WARNING, "Plugin {0} is performing a blocking action on the main thread "
                    + "This could be a performance hit {1}. Such actions should be handled async from the main thread"
                    + "Report it to the plugin author", new Object[]{violation.getPluginName(), perm});

            if (plugin.getConfig().getBoolean("hideStacktrace")) {
                if (foundPlugin != null) {
                    StackTraceElement source = foundPlugin.getValue();
                    plugin.getLogger().log(Level.WARNING, "Source: {0}, method {1}(), line {2}"
                            , new Object[]{source.getClassName(), source.getMethodName(), source.getLineNumber()});
                }
            } else {
                plugin.getLogger().log(Level.WARNING, "", stackTraceCreator);
            }
        }
    }

    private boolean isBlockingAction(Permission permission) {
        String actions = permission.getActions();

        if (permission instanceof FilePermission) {
            //commented out, because also operations like .createNewFile() is also a write permission
            //which could executed by the main thread, doesn't it`?
//            ignore jar files because the java runtime load and unload classes at runtime
            return actions.contains("read")
                    && fileWhitelist.stream().noneMatch(ignored -> permission.getName().contains(ignored));
            //read write
        } else if (permission instanceof SocketPermission) {
            //already handled with connection selector
//            return actions.contains("connect");
        }

        return false;
    }
}

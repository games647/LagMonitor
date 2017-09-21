package com.github.games647.lagmonitor.threading;

import com.github.games647.lagmonitor.LagMonitor;
import com.google.common.collect.ImmutableSet;

import java.io.FilePermission;
import java.security.Permission;
import java.util.Set;

public class BlockingSecurityManager extends SecurityManager {

    private final LagMonitor plugin;
    private final SecurityManager delegate;

    private final Set<String> fileWhitelist = ImmutableSet.of(".jar", "session.lock");

    public BlockingSecurityManager(LagMonitor plugin, SecurityManager delegate) {
        this.plugin = plugin;

        this.delegate = delegate;
    }

    public BlockingSecurityManager(LagMonitor plugin) {
        this(plugin, System.getSecurityManager());
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
        if (isBlockingAction(perm)) {
            plugin.getBlockingActionManager().checkBlockingAction("Permission: " + perm.getName());
        }
    }

    private boolean isBlockingAction(Permission permission) {
        String actions = permission.getActions();

        if (permission instanceof FilePermission) {
//            ignore jar files because the java runtime load and unload classes at runtime
            return actions.contains("read")
                    && fileWhitelist.stream().noneMatch(ignored -> permission.getName().contains(ignored));
        }

        return false;
    }
}

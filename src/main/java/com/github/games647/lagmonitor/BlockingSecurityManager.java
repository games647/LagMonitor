package com.github.games647.lagmonitor;

import java.io.FilePermission;
import java.net.SocketPermission;
import java.security.Permission;
import java.util.logging.Level;

public class BlockingSecurityManager extends SecurityManager {

    private final LagMonitor plugin;
    private final Thread mainThread;
    private final SecurityManager delegate;

    public BlockingSecurityManager(LagMonitor plugin, Thread mainThread, SecurityManager delegate) {
        this.plugin = plugin;

        this.mainThread = mainThread;
        this.delegate = delegate;
    }

    public BlockingSecurityManager(LagMonitor plugin, Thread mainThread) {
        this(plugin, mainThread, null);
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
        if (Thread.currentThread() == mainThread && isBlockingAction(perm)) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            plugin.getLogger().log(Level.WARNING, "Blocking action on the main thread", new Object[]{perm, stackTrace});
        }
    }

    private boolean isBlockingAction(Permission permission) {
        String actions = permission.getActions();
        if (permission instanceof FilePermission) {
            //commented out, because also operations like .createNewFile() is also a write permission
            //which could executed by the main thread, doesn't it`?
//            return actions.contains("read") || actions.contains("write");
            //read write
        } else if (permission instanceof SocketPermission) {
            return actions.contains("connect");
        }

        return false;
    }
}

package com.github.games647.lagmonitor.threading;

import com.google.common.collect.ImmutableSet;

import java.io.FilePermission;
import java.security.Permission;
import java.util.Set;

public class BlockingSecurityManager extends SecurityManager implements Injectable {

    private final BlockingActionManager actionManager;
    private final Set<String> fileWhitelist = ImmutableSet.of(".jar", "session.lock");

    private SecurityManager delegate;

    public BlockingSecurityManager(BlockingActionManager actionManager) {
        this.actionManager = actionManager;
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
            actionManager.checkBlockingAction("Permission: " + perm.getName());
        }
    }

    private boolean isBlockingAction(Permission permission) {
        String actions = permission.getActions();
        return permission instanceof FilePermission
                && actions.contains("read")
                && fileWhitelist.stream().noneMatch(ignored -> permission.getName().contains(ignored));
    }

    @Override
    public void inject() {
        SecurityManager oldSecurityManager = System.getSecurityManager();
        if (oldSecurityManager != this) {
            this.delegate = oldSecurityManager;
            System.setSecurityManager(this);
        }
    }

    @Override
    public void restore() {
        if (System.getSecurityManager() == this) {
            System.setSecurityManager(delegate);
        }
    }
}

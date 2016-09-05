package com.github.games647.lagmonitor.tasks;

import com.github.games647.lagmonitor.LagMonitor;

import java.lang.Thread.State;
import java.util.TimerTask;
import java.util.logging.Level;

public class BlockingIODetectorTask extends TimerTask {

    private final Thread mainThread;
    private final LagMonitor plugin;

    public BlockingIODetectorTask(LagMonitor plugin, Thread mainThread) {
        this.plugin = plugin;
        this.mainThread = mainThread;
    }

    @Override
    public void run() {
        //According to this post the thread is still in Runnable although it's waiting for
        //file/http ressources
        //https://stackoverflow.com/questions/20795295/why-jstack-out-says-thread-state-is-runnable-while-socketread
        if (mainThread.getState() == State.RUNNABLE) {
            //Based on this post we have to check the top element of the stack
            //https://stackoverflow.com/questions/20891386/how-to-detect-thread-being-blocked-by-io
            StackTraceElement[] stackTrace = mainThread.getStackTrace();
            StackTraceElement topElement = stackTrace[stackTrace.length - 1];
            if (topElement.isNativeMethod()) {
                //Socket/SQL (connect) - java.net.DualStackPlainSocketImpl.connect0
                //Socket/SQL (read) - java.net.SocketInputStream.socketRead0
                //Socket/SQL (write) - java.net.SocketOutputStream.socketWrite0
                if (isElementEqual(topElement, "java.net.DualStackPlainSocketImpl", "connect0")
                        || isElementEqual(topElement, "java.net.SocketInputStream", "socketRead0")
                        || isElementEqual(topElement, "java.net.SocketOutputStream", "socketWrite0")) {
                    plugin.getLogger().log(Level.WARNING
                            , "Server is performing socket operations on the main thread", new Throwable());
                } //File (in) - java.io.FileInputStream.readBytes
                //File (out) - java.io.FileOutputStream.writeBytes
                else if (isElementEqual(topElement, "java.io.FileInputStream", "readBytes")
                        || isElementEqual(topElement, "java.io.FileOutputStream", "writeBytes")) {
                    plugin.getLogger().log(Level.WARNING
                            , "Server is performing file operations on the main thread", new Throwable());
                }
            }
        }
    }

    private boolean isElementEqual(StackTraceElement traceElement, String className, String methodName) {
        return traceElement.getClassName().equals(className) && traceElement.getMethodName().equals(methodName);
    }
}

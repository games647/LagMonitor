package com.github.games647.lagmonitor.tasks;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.MethodMeasurement;
import com.github.games647.lagmonitor.commands.MonitorCommand;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.TimerTask;

/**
 * Based on the project https://github.com/sk89q/WarmRoast by sk89q
 */
public class MonitorTask extends TimerTask {

    private static final int MAX_DEPTH = 25;

    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final LagMonitor plugin;
    private final long threadId;

    private MethodMeasurement rootNode;
    private int samples;

    public MonitorTask(LagMonitor plugin, long threadId) {
        this.plugin = plugin;
        this.threadId = threadId;
    }

    public synchronized MethodMeasurement getRootSample() {
        return rootNode;
    }

    public synchronized int getSamples() {
        return samples;
    }

    @Override
    public void run() {
        ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId, MAX_DEPTH);
        StackTraceElement[] stackTrace = threadInfo.getStackTrace();
        if (stackTrace.length > 0) {
            StackTraceElement rootElement = stackTrace[stackTrace.length - 1];
            synchronized (this) {
                samples++;

                if (rootNode == null) {
                    String rootClass = rootElement.getClassName();
                    String rootMethod = rootElement.getMethodName();

                    String id = rootClass + '.' + rootMethod;
                    rootNode = new MethodMeasurement(id, rootClass, rootMethod);
                }

                rootNode.onMeasurement(stackTrace, 0, MonitorCommand.SAMPLE_INTERVALL);
            }
        }
    }

//    public void resume() {
//
//    }
//
//    public void pause() {
//
//    }
}

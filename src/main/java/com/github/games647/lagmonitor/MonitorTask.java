package com.github.games647.lagmonitor;

import com.github.games647.lagmonitor.commands.MonitorCommand;

import java.util.Map;
import java.util.TimerTask;

public class MonitorTask extends TimerTask {

    private final LagMonitor plugin;
    private final long threadId;

    private MethodMeasurement rootNode;
    private int samples;

    public MonitorTask(LagMonitor plugin, long threadId) {
        this.plugin = plugin;
        this.threadId = threadId;
    }

    public MethodMeasurement getRootSample() {
        return rootNode;
    }

    public int getSamples() {
        return samples;
    }

    @Override
    public void run() {
        synchronized (this) {
            samples++;

            Map<Thread, StackTraceElement[]> stacktraces = Thread.getAllStackTraces();
            for (Map.Entry<Thread, StackTraceElement[]> entry : stacktraces.entrySet()) {
                Thread thread = entry.getKey();

                if (thread.getId() != threadId) {
                    //don't analyze our own thread and ignore empty threads
                    continue;
                }

                StackTraceElement[] stackTrace = entry.getValue();
                if (stackTrace.length > 0) {
                    StackTraceElement rootElement = stackTrace[stackTrace.length - 1];
                    if (rootNode == null) {
                        String rootClass = rootElement.getClassName();
                        String rootMethod = rootElement.getMethodName();

                        String id = rootClass + '.' + rootMethod;
                        rootNode = new MethodMeasurement(rootClass, rootMethod, id);
                    }

                    rootNode.onMeasurement(stackTrace, 0, MonitorCommand.SAMPLE_INTERVALL);
                }
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

package com.github.games647.lagmonitor;

import com.github.games647.lagmonitor.commands.MonitorCommand;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.TimerTask;

public class MonitorTask extends TimerTask {

    private final LagMonitor plugin;

    private Map<String, MethodMeasurement> sampleResults = Maps.newConcurrentMap();

    public MonitorTask(LagMonitor plugin) {
        this.plugin = plugin;
    }

    public Map<String, MethodMeasurement> getSampleResults() {
        return ImmutableMap.copyOf(sampleResults);
    }

    @Override
    public void run() {
        Map<Thread, StackTraceElement[]> stacktraces = Thread.getAllStackTraces();
        for (Map.Entry<Thread, StackTraceElement[]> entry : stacktraces.entrySet()) {
            Thread thread = entry.getKey();
            StackTraceElement[] stacktrace = entry.getValue();

            if (Thread.currentThread().equals(thread) || stacktrace.length == 0) {
                //don't analyze our own thread and ignore empty threads
                continue;
            }

            StackTraceElement rootElement = stacktrace[stacktrace.length - 1];
            MethodMeasurement methodMeasurement = sampleResults.get(thread.getName());
            if (methodMeasurement == null) {
                methodMeasurement = new MethodMeasurement(rootElement.getClassName(), rootElement.getMethodName());
                sampleResults.put(thread.getName(), methodMeasurement);
            }

            methodMeasurement.onMeasurement(stacktrace, 0, MonitorCommand.SAMPLE_INTERVALL);
        }
    }
}

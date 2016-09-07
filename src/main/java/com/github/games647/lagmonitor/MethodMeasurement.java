package com.github.games647.lagmonitor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;

public class MethodMeasurement implements Comparable<MethodMeasurement> {

    private final String id;
    private final String className;
    private final String method;

    private Map<String, MethodMeasurement> childInvokes;
    private long totalTime;

    public MethodMeasurement(String id, String className, String method) {
        this.id = id;

        this.className = className;
        this.method = method;
    }

    public String getId() {
        return id;
    }

    public String getClassName() {
        return className;
    }

    public String getMethod() {
        return method;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public Map<String, MethodMeasurement> getChildInvokes() {
        if (childInvokes == null) {
            return Collections.emptyMap();
        }

        return ImmutableMap.copyOf(childInvokes);
    }

    public float getTimePercent(long parentTime) {
        //one float conversion triggers the complete calculation to be decimal
        return ((float) totalTime / parentTime) * 100;
    }

    public void onMeasurement(StackTraceElement[] stackTrace, int skipElements, long time) {
        totalTime += time;

        if (skipElements >= stackTrace.length) {
            //we reached the end
            return;
        }

        if (childInvokes == null) {
            //lazy loading
            childInvokes = Maps.newHashMap();
        }

        StackTraceElement nextChildElement = stackTrace[stackTrace.length - skipElements - 1];
        String nextClass = nextChildElement.getClassName();
        String nextMethod = nextChildElement.getMethodName();

        String idName = nextChildElement.getClassName() + '.' + nextChildElement.getMethodName();
        MethodMeasurement child = childInvokes
                .computeIfAbsent(idName, (key) -> new MethodMeasurement(key, nextClass, nextMethod));
        child.onMeasurement(stackTrace, skipElements + 1, time);
    }

    public void writeString(StringBuilder builder, int indent) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            b.append(" ");
        }
        String padding = b.toString();

        for (MethodMeasurement child : getChildInvokes().values()) {
            builder.append(padding).append(child.getId()).append("()");
            builder.append(" ");
            builder.append(child.getTotalTime()).append("ms");
            builder.append("\n");
            child.writeString(builder, indent + 1);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, MethodMeasurement> entry : getChildInvokes().entrySet()) {
            builder.append(entry.getKey()).append("()");
            builder.append(" ");
            builder.append(entry.getValue().getTotalTime()).append("ms");
            builder.append("\n");
            entry.getValue().writeString(builder, 1);
        }

        return builder.toString();
    }

    @Override
    public int compareTo(MethodMeasurement other) {
        return Long.compare(this.totalTime, other.totalTime);
    }
}

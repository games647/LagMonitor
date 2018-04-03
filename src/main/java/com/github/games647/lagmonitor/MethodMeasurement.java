package com.github.games647.lagmonitor;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class MethodMeasurement implements Comparable<MethodMeasurement> {

    private final String id;
    private final String className;
    private final String method;

    private final Map<String, MethodMeasurement> childInvokes = new HashMap<>();
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
        IntStream.range(0, indent).forEach(i -> b.append(' '));

        String padding = b.toString();

        for (MethodMeasurement child : getChildInvokes().values()) {
            builder.append(padding).append(child.id).append("()");
            builder.append(' ');
            builder.append(child.totalTime).append("ms");
            builder.append('\n');
            child.writeString(builder, indent + 1);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodMeasurement that = (MethodMeasurement) o;

        return totalTime == that.totalTime &&
                Objects.equals(id, that.id) &&
                Objects.equals(className, that.className) &&
                Objects.equals(method, that.method) &&
                Objects.equals(childInvokes, that.childInvokes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, className, method, childInvokes, totalTime);
    }

    @Override
    public int compareTo(MethodMeasurement other) {
        return Long.compare(this.totalTime, other.totalTime);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, MethodMeasurement> entry : getChildInvokes().entrySet()) {
            builder.append(entry.getKey()).append("()");
            builder.append(' ');
            builder.append(entry.getValue().totalTime).append("ms");
            builder.append('\n');
            entry.getValue().writeString(builder, 1);
        }

        return builder.toString();
    }
}

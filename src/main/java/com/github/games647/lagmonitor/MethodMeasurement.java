package com.github.games647.lagmonitor;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;

public class MethodMeasurement implements Comparable<MethodMeasurement> {

    private final String name;

    private final Map<String, MethodMeasurement> childInvokes = Maps.newHashMap();
    private long totalTime;

    public MethodMeasurement(String className, String methodName) {
        this.name = className + '.' + methodName;
    }

    public String getName() {
        return name;
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

        StackTraceElement nextChildElement = stackTrace[stackTrace.length - 1 - skipElements];
        String idName = nextChildElement.getClassName() + '.' + nextChildElement.getMethodName();
        MethodMeasurement child = childInvokes.get(idName);
        if (child == null) {
            child = new MethodMeasurement(nextChildElement.getClassName(), nextChildElement.getMethodName());
            childInvokes.put(idName, child);
        }

        child.onMeasurement(stackTrace, skipElements + 1, time);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", name)
                .add("totalTime", totalTime)
                .add("children", childInvokes)
                .toString();
    }

    @Override
    public int compareTo(MethodMeasurement other) {
        return Long.compare(this.totalTime, other.totalTime);
    }
}

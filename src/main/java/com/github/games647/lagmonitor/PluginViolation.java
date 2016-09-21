package com.github.games647.lagmonitor;

import com.google.common.base.Objects;

public class PluginViolation {

    private final String pluginName;
    private final String sourceFile;
    private final String methodName;
    private final int lineNumber;

    private final String event;

    public PluginViolation(String pluginName, StackTraceElement stackTraceElement, String event) {
        this.pluginName = pluginName;
        this.sourceFile = stackTraceElement.getFileName();
        this.methodName = stackTraceElement.getMethodName();
        this.lineNumber = stackTraceElement.getLineNumber();

        this.event = event;
    }

    public PluginViolation(String event) {
        this.pluginName = "Unknown";
        this.sourceFile = "Unknown";
        this.methodName = "Unknown";
        this.lineNumber = -1;

        this.event = event;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getEvent() {
        return event;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pluginName, sourceFile, methodName);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PluginViolation)) {
            return false;
        }

        PluginViolation other = (PluginViolation) obj;
        return Objects.equal(pluginName, other.pluginName)
                && Objects.equal(sourceFile, other.sourceFile)
                && Objects.equal(methodName, other.methodName);
    }
}

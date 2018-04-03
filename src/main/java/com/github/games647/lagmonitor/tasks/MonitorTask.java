package com.github.games647.lagmonitor.tasks;

import com.github.games647.lagmonitor.MethodMeasurement;
import com.github.games647.lagmonitor.commands.MonitorCommand;
import com.google.common.net.UrlEscapers;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Based on the project https://github.com/sk89q/WarmRoast by sk89q
 */
public class MonitorTask extends TimerTask {

    private static final String PASTE_URL = "https://paste.enginehub.org/paste";
    private static final int MAX_DEPTH = 25;

    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final Logger logger;
    private final long threadId;

    private MethodMeasurement rootNode;
    private int samples;

    public MonitorTask(Logger logger, long threadId) {
        this.logger = logger;
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

                rootNode.onMeasurement(stackTrace, 0, MonitorCommand.SAMPLE_INTERVAL);
            }
        }
    }

    public String paste() {
        try {
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(PASTE_URL).openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(httpConnection.getOutputStream(), StandardCharsets.UTF_8))
            ) {
                writer.write("content=" + UrlEscapers.urlPathSegmentEscaper().escape(toString()));
                writer.write("&from=" + logger.getName());
            }

            JsonObject object;
            try (Reader reader = new BufferedReader(
                    new InputStreamReader(httpConnection.getInputStream(), StandardCharsets.UTF_8))
            ) {
                object = new Gson().fromJson(reader, JsonObject.class);
            }

            if (object.has("url")) {
                return object.get("url").getAsString();
            }

            logger.log(Level.INFO, "Failed to parse url from {0}", object);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to upload monitoring data", ex);
        }

        return null;
    }

    @Override
    public String toString() {
        ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId, MAX_DEPTH);

        StringBuilder builder = new StringBuilder();
        builder.append(threadInfo.getThreadName());
        builder.append(' ');

        synchronized (this) {
            builder.append(rootNode.getTotalTime()).append("ms");
            builder.append('\n');

            rootNode.writeString(builder, 1);
        }

        return builder.toString();
    }
}

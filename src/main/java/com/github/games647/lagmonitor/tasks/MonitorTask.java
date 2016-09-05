package com.github.games647.lagmonitor.tasks;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.MethodMeasurement;
import com.github.games647.lagmonitor.commands.MonitorCommand;
import com.google.common.base.Charsets;
import com.google.common.io.Closer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TimerTask;
import java.util.logging.Level;

import org.json.simple.JSONValue;

/**
 * Based on the project https://github.com/sk89q/WarmRoast by sk89q
 */
public class MonitorTask extends TimerTask {

    private static final String PASTE_URL = "https://paste.enginehub.org/paste";

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
    public String paste() {
        Closer closer = Closer.create();
        try {
            HttpURLConnection httpConnection = (HttpURLConnection) new URL(PASTE_URL).openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);

            OutputStream outputStream = closer.register(httpConnection.getOutputStream());
            OutputStreamWriter streamWriter = closer.register(new OutputStreamWriter(outputStream, Charsets.UTF_8));
            BufferedWriter writer = closer.register(new BufferedWriter(streamWriter));

            writer.write("content=" + URLEncoder.encode(toString(), Charsets.UTF_8.name()));
            writer.write("&from=" + URLEncoder.encode(plugin.getName(), Charsets.UTF_8.name()));
            writer.flush();

            BufferedReader reader = closer
                    .register(new BufferedReader(closer
                            .register(new InputStreamReader(closer
                                    .register(httpConnection.getInputStream())))));

            StringBuilder inputBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                inputBuilder.append(line);
            }

            String input = inputBuilder.toString();
            plugin.getLogger().log(Level.FINE, input);

            Object object = JSONValue.parse(input);
            if (object instanceof Map) {
                @SuppressWarnings("unchecked")
                String urlString = String.valueOf(((Map<Object, Object>) object).get("url"));
                return urlString;
            }

            plugin.getLogger().info("Failed to parse url");
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        } finally {
            try {
                closer.close();
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, null, ex);
            }
        }

        return null;
    }

    @Override
    public String toString() {
        ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId, MAX_DEPTH);

        StringBuilder builder = new StringBuilder();
        builder.append(threadInfo.getThreadName());
        builder.append(" ");
        builder.append(rootNode.getTotalTime()).append("ms");
        builder.append("\n");

        rootNode.writeString(builder, 1);

        return builder.toString();
    }
}

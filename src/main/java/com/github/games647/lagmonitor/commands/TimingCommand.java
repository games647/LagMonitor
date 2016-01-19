package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.Timing;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.TimingsCommand;
import org.spigotmc.CustomTimingsHandler;

public class TimingCommand implements CommandExecutor {

    private static final String EXCLUDE_INDENTIFIER = "** ";

    private final LagMonitor plugin;

    public TimingCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayStream);

        //place sampleTime here to be very accurat
        long sampleTime = System.nanoTime() - TimingsCommand.timingStart;
        CustomTimingsHandler.printTimings(printStream);

        String output = null;
        try {
            output = byteArrayStream.toString(Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            //ignore utf-8 is always available
        }

        sendParsedOutput(output, sender, sampleTime);
        return true;
    }

    private void sendParsedOutput(String output, CommandSender sender, long sampleTime) {
        Map<String, Timing> timings = Maps.newHashMap();
        Timing breakdownTiming = new Timing("Breakdown", 0, 0);
        Timing minecraftTiming = new Timing("Minecraft", 0, 0);
        timings.put("Minecraft", minecraftTiming);
        timings.put("Breakdown", breakdownTiming);

        parseTimings(output, timings, minecraftTiming, breakdownTiming);

        breakdownTiming.setTotalTime(minecraftTiming.getTotalTime() - 1);

        long total = minecraftTiming.getTotalTime();
        long numTicks = 0;
        long entityTicks = 0;
        long playerTicks = 0;
        long activatedEntityTicks = 0;

        if (breakdownTiming.getSubcategories() != null) {
            for (Map.Entry<String, Timing> entry : breakdownTiming.getSubcategories().entrySet()) {
                String key = entry.getKey();
                Timing value = entry.getValue();
                if ("** tickEntity - EntityPlayer".equalsIgnoreCase(key)) {
                    playerTicks = value.getTotalCount();
                } else if ("** activatedTickEntity".equalsIgnoreCase(key)) {
                    activatedEntityTicks = value.getTotalCount();
                } else if ("** tickEntity".equalsIgnoreCase(key)) {
                    entityTicks = value.getTotalCount();
                } else if (key.contains(" - entityTick")) {
                    numTicks = Math.max(numTicks, value.getTotalCount());
                }
            }
        }

        numTicks = Math.max(1, numTicks);
        float serverLoad = 0;

        for (Map.Entry<String, Timing> entry : timings.entrySet()) {
            String categoryName = entry.getKey();
            Timing value = entry.getValue();
            float pct = (float) value.getTotalTime() / sampleTime;

            //nanoseconds -> seconds
            float totalSeconds = (float) value.getTotalTime() / 1000 / 1000 / 1000;
            sender.sendMessage("=" + categoryName + " Total: " + round(totalSeconds) + "sec pct: " + round(pct) + "=");
            if (value.getSubcategories() != null) {
                for (Map.Entry<String, Timing> subEntry : value.getSubcategories().entrySet()) {
                    String event = subEntry.getKey();
                    Timing subValue = subEntry.getValue();

                    float avg = (float) subValue.getTotalTime() / subValue.getTotalCount();
                    float timesPerTick = (float) subValue.getTotalCount() / numTicks;
                    if (timesPerTick > 1) {
                        avg *= timesPerTick;
                    }

                    float pctTick = avg / 1000 / 1000 / 50;
                    float count = (float) subValue.getTotalCount() / 1000;
                    //->ms
                    avg = avg / 1000 / 1000;
                    float pctTotal = (float) subValue.getTotalTime() / sampleTime;
                    if (event.equalsIgnoreCase("** Full Server Tick")) {
                        serverLoad = pctTick;
                    }

                    sender.sendMessage(event + " Pct: " + round(pctTotal)
                            + " PctTick: " + round(pctTick)
                            + " AVG: " + round(avg)
                            + " TimeTick: " + round(timesPerTick)
                            + " Count: " + round(count));
                }
            }
        }

        float totalSeconds = (float) total / 1000 / 1000 / 1000;

        float activatedAvgEntities = (float) activatedEntityTicks / numTicks;
        float totalAvgEntities = (float) entityTicks / numTicks;
        float activatedPercent = activatedAvgEntities / totalAvgEntities;

        float averagePlayers = (float) playerTicks / numTicks;

        float desiredTicks = (float) sampleTime / 1000 / 1000 / 1000 * 20;
        float averageTicks = numTicks / desiredTicks * 20;

        //head data
        sender.sendMessage("Avg ticks: " + round(averageTicks));
        sender.sendMessage("Server Load: " + round(serverLoad));
        sender.sendMessage("AVG Players: " + round(averagePlayers));
        sender.sendMessage("Activated Percent: " + round(activatedPercent));
        sender.sendMessage("Activated Entities: " + round(activatedAvgEntities) + " / " + round(totalAvgEntities));
        sender.sendMessage("Total: " + round(totalSeconds));
        sender.sendMessage("Ticks: " + round(numTicks));
        //convert from nanoseconds to seconds
        sender.sendMessage("Sample Time: " + round((float) sampleTime / 1000 / 1000 / 1000));
    }

    private void parseTimings(String output, Map<String, Timing> timings
            , Timing minecraftTiming, Timing breakdownTiming) {
        String[] lines = output.split(System.lineSeparator());
        for (String line : lines) {
            if (line.startsWith("    ")) {
                String category = line.substring("    ".length(), line.lastIndexOf("Time: ") - 1);

                Timing active = minecraftTiming;
                String subCategory = category;
                if (category.contains("Event: ")) {
                    String pluginName = getProperty(category, "Plugin");
                    String listener = getProperty(category, "Event");

                    Timing pluginReport = timings.get(pluginName);
                    if (pluginReport == null) {
                        pluginReport = new Timing(pluginName);
                        timings.put(pluginName, pluginReport);
                    }

                    active = pluginReport;
                    subCategory = listener;
                } else if (category.contains("Task: ")) {
                    String pluginName = getProperty(category, "Task");
                    String runnable = getProperty(category, "Runnable");

                    Timing pluginReport = timings.get(pluginName);
                    if (pluginReport == null) {
                        pluginReport = new Timing(pluginName);
                        timings.put(pluginName, pluginReport);
                    }

                    active = pluginReport;
                    subCategory = runnable;
                }

                long totalTime = getPropertyValue(line, "Time");
                long count = getPropertyValue(line, "Count");
                if (subCategory.startsWith(EXCLUDE_INDENTIFIER)) {
                    breakdownTiming.addSubcategory(subCategory, totalTime, count);
                } else {
                    active.addSubcategory(subCategory, totalTime, count);
                    if (subCategory.startsWith("Task:")) {
                        breakdownTiming.addSubcategory(EXCLUDE_INDENTIFIER + "Tasks", totalTime, count);
                    }

                    if (active.getTotalTime() >= 0) {
                        active.addTotal(totalTime);
                    }
                }
            }
        }
    }

    private float round(float number) {
        return (float) (Math.round(number * 100.0) / 100.0);
    }

    private String getProperty(String line, String propertyName) {
        String categoryName = propertyName + ": ";

        int startIndex = line.indexOf(categoryName) + categoryName.length();
        int endIndex = line.indexOf(' ', startIndex);
        if (endIndex == -1) {
            //line reached the end
            endIndex = line.length();
        }

        return line.substring(startIndex, endIndex);
    }

    private long getPropertyValue(String line, String propertyName) {
        String unparsedProperty = getProperty(line, propertyName);
        return Long.parseLong(unparsedProperty);
    }
}

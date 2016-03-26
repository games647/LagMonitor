package com.github.games647.lagmonitor.commands;

import com.avaje.ebeaninternal.api.ClassUtil;
import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.Timing;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.TimingsCommand;
import org.spigotmc.CustomTimingsHandler;

/**
 * Parsed from the PHP by aikar
 * https://github.com/aikar/timings
 */
public class TimingCommand implements CommandExecutor {

    private static final String EXCLUDE_INDENTIFIER = "** ";

    private final LagMonitor plugin;

    public TimingCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!Bukkit.getServer().getPluginManager().useTimings()) {
            sender.sendMessage(ChatColor.DARK_RED + "The server deactivated timing reports");
            sender.sendMessage(ChatColor.DARK_RED + "Go to bukkit.yml and activate plugin-profiling");
            return true;
        }

        if (!ClassUtil.isPresent("org.bukkit.command.defaults.TimingsCommand")) {
            sender.sendMessage(ChatColor.DARK_RED + "You're using a new Timings version on your server system");
            sender.sendMessage(ChatColor.DARK_RED + "This is currently unsupported");
            sender.sendMessage(ChatColor.DARK_RED + "For more details: Visit: ");
            sender.sendMessage(ChatColor.DARK_RED + "https://github.com/games647/LagMonitor/issues/5");
            return true;
        }

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
        Timing breakdownTiming = new Timing("Breakdown", -1, -1);
        Timing minecraftTiming = new Timing("Minecraft");
        timings.put("Minecraft", minecraftTiming);
        timings.put("Breakdown", breakdownTiming);

        parseTimings(output, timings, minecraftTiming, breakdownTiming);

        long playerTicks = 0;
        long activatedEntityTicks = 0;
        long entityTicks = 0;
        long numTicks = 0;

        if (breakdownTiming.getSubCategories() != null) {
            for (Map.Entry<String, Timing> entry : breakdownTiming.getSubCategories().entrySet()) {
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
            String category = entry.getKey();
            Timing value = entry.getValue();
//            float pct = (float) value.getTotalTime() / sampleTime;
            float pct = (float) value.getTotalTime() / sampleTime * 100;

            String highlightedPercent;
            if ("Minecraft".equals(category)) {
                highlightedPercent = highlightPct(round(pct), 20, 40, 70);
            } else {
                highlightedPercent = highlightPct(round(pct), 1, 3, 6);
            }

            //nanoseconds -> seconds
            float totalSeconds = (float) value.getTotalTime() / 1000 / 1000 / 1000;
            sender.sendMessage(ChatColor.YELLOW + "=== " + category + " Total: " + round(totalSeconds)
                    + "sec: " + highlightedPercent + "% " + ChatColor.YELLOW + "===");
            if (value.getSubCategories() != null) {
                for (Map.Entry<String, Timing> subEntry : value.getSubCategories().entrySet()) {
                    String event = subEntry.getKey().replace("** ", "").replace("-", "");
                    int lastPackage = event.lastIndexOf('.');
                    if (lastPackage != -1) {
                        event = event.substring(lastPackage + 1);
                    }

                    Timing subValue = subEntry.getValue();

                    float avg = (float) subValue.getTotalTime() / subValue.getTotalCount();
                    float timesPerTick = (float) subValue.getTotalCount() / numTicks;
                    if (timesPerTick > 1) {
                        avg *= timesPerTick;
                    }

                    float pctTick = avg / 1000 / 1000 / 50 * 100;
//                    float count = (float) subValue.getTotalCount() / 1000;
                    //->ms
                    avg = avg / 1000 / 1000;
                    float pctTotal = (float) subValue.getTotalTime() / sampleTime * 100;
                    if (event.equalsIgnoreCase("Full Server Tick")) {
                        serverLoad = pctTick;
                    }

                    sender.sendMessage(ChatColor.DARK_AQUA + event + ' ' + highlightPct(round(pctTotal), 10, 20, 50)
                            + " Tick: " + highlightPct(round(pctTick), 3, 15, 40)
                            + " AVG: " + round(avg) + "ms");
                }
            }
        }

        sender.sendMessage(ChatColor.YELLOW + "==========================================");

        float activatedAvgEntities = (float) activatedEntityTicks / numTicks;
        float totalAvgEntities = (float) entityTicks / numTicks;
//        float activatedPercent = activatedAvgEntities / totalAvgEntities;

        float averagePlayers = (float) playerTicks / numTicks;

        float desiredTicks = (float) sampleTime / 1000 / 1000 / 1000 * 20;
        float averageTicks = numTicks / desiredTicks * 20;

        String format = ChatColor.DARK_AQUA + "%s" + " " + ChatColor.GRAY + "%s";

        //head data
        sender.sendMessage(String.format(format, "Avg ticks:", round(averageTicks)));
        sender.sendMessage(String.format(format, "Server Load:", round(serverLoad)));
        sender.sendMessage(String.format(format, "AVG Players:", round(averagePlayers)));
        sender.sendMessage(String.format(format, "Activated Entities:", round(activatedAvgEntities))
                + " / " + round(totalAvgEntities));

        sender.sendMessage(String.format(format, "Ticks:", round(numTicks)));

        //convert from nanoseconds to seconds
        sender.sendMessage(String.format(format, "Sample Time (sec):", round((float) sampleTime / 1000 / 1000 / 1000)));
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

    private String highlightPct(float percent, int low, int med, int high) {
        ChatColor prefix = ChatColor.GRAY;
        if (percent > high) {
            prefix = ChatColor.DARK_RED;
        } else if (percent > med) {
            prefix = ChatColor.GOLD;
        } else if (percent > low) {
            prefix = ChatColor.YELLOW;
        }

        return prefix + "" + percent + '%' + ChatColor.GRAY;
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

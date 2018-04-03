package com.github.games647.lagmonitor.commands.timings;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.Pagination;
import com.github.games647.lagmonitor.commands.LagCommand;
import com.github.games647.lagmonitor.traffic.Reflection;
import com.github.games647.lagmonitor.traffic.Reflection.FieldAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.TimingsCommand;
import org.spigotmc.CustomTimingsHandler;

import static com.github.games647.lagmonitor.utils.LagUtils.round;

/**
 * Parsed from the PHP project by aikar
 * https://github.com/aikar/timings
 */
public class SpigotTimingsCommand extends LagCommand {

    //these timings will be in the breakdown report
    private static final String EXCLUDE_IDENTIFIER = "** ";

    public SpigotTimingsCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canExecute(sender, command)) {
            return true;
        }

        if (!Bukkit.getPluginManager().useTimings()) {
            sendError(sender, "The server deactivated timing reports");
            sendError(sender, "Go to bukkit.yml and activate plugin-profiling");
            return true;
        }

        //place sampleTime here to be very accurate
        long sampleTime = System.nanoTime() - TimingsCommand.timingStart;
        if (sampleTime <= 1_000 * 1_000 * 1_000) {
            sendError(sender, "Sampling time is too low");
            return true;
        }

        Queue<CustomTimingsHandler> handlers = Reflection.getField(CustomTimingsHandler.class, "HANDLERS", Queue.class)
                .get(null);

        List<BaseComponent[]> lines = new ArrayList<>();
        sendParsedOutput(handlers, lines, sampleTime);

        Pagination pagination = new Pagination("Paper Timings", lines);
        pagination.send(sender);

        this.plugin.getPageManager().setPagination(sender.getName(), pagination);
        return true;
    }

    private void sendParsedOutput(Iterable<CustomTimingsHandler> handlers, Collection<BaseComponent[]> lines, long sampleTime) {
        Map<String, Timing> timings = new HashMap<>();
        Timing breakdownTiming = new Timing("Breakdown", -1, -1);
        Timing minecraftTiming = new Timing("Minecraft");
        timings.put("Minecraft", minecraftTiming);
        timings.put("Breakdown", breakdownTiming);

        parseTimings(handlers, timings, minecraftTiming, breakdownTiming);

        long playerTicks = 0;
        long activatedEntityTicks = 0;
        long entityTicks = 0;
        long numTicks = 0;
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

        double serverLoad = 0;

        for (Map.Entry<String, Timing> entry : timings.entrySet()) {
            String category = entry.getKey();
            Timing timing = entry.getValue();
            float pct = (float) timing.getTotalTime() / sampleTime * 100;

            String highlightedPercent = highlightPct(round(pct), 1, 3, 6);
            if (timing == minecraftTiming) {
                highlightedPercent = highlightPct(round(pct), 20, 40, 70);
            }

            //nanoseconds -> seconds
            float totalSeconds = (float) timing.getTotalTime() / 1000 / 1000 / 1000;
            lines.add(TextComponent.fromLegacyText(ChatColor.YELLOW + "=== " + category
                    + " Total: " + round(totalSeconds) + "sec: "
                    + highlightedPercent + "% " + ChatColor.YELLOW + "==="));
            if (timing.getSubCategories() != null) {
                for (Map.Entry<String, Timing> subEntry : timing.getSubCategories().entrySet()) {
                    String event = subEntry.getKey().replace("** ", "").replace("-", "");
                    int lastPackage = event.lastIndexOf('.');
                    if (lastPackage != -1) {
                        event = event.substring(lastPackage + 1);
                    }

                    Timing subValue = subEntry.getValue();

                    double avg = subValue.calculateAverage();
                    double timesPerTick = (double) subValue.getTotalCount() / numTicks;
                    if (timesPerTick > 1) {
                        avg *= timesPerTick;
                    }

                    double pctTick = avg / 1000 / 1000 / 50 * 100;
//                    float count = (float) subValue.getTotalCount() / 1000;
                    //->ms
                    avg = avg / 1000 / 1000;
                    double pctTotal = (double) subValue.getTotalTime() / sampleTime * 100;
                    if ("Full Server Tick".equalsIgnoreCase(event)) {
                        serverLoad = pctTick;
                    }

                    lines.add(TextComponent.fromLegacyText(ChatColor.DARK_AQUA + event + ' '
                            + highlightPct(round(pctTotal), 10, 20, 50)
                            + " Tick: " + highlightPct(round(pctTick), 3, 15, 40)
                            + " AVG: " + round(avg) + "ms"));
                }
            }
        }

        lines.add(new ComponentBuilder("==========================================").color(ChatColor.GOLD).create());

        long total = minecraftTiming.getTotalTime();
        printHeadData(total, activatedEntityTicks, numTicks, entityTicks, playerTicks, sampleTime, lines, serverLoad);
    }

    private void printHeadData(long total, long activatedEntityTicks, long numTicks, long entityTicks, long playerTicks
            , long sampleTime, Collection<BaseComponent[]> lines, double serverLoad) {
        float totalSeconds = (float) total / 1000 / 1000 / 1000;

        float activatedAvgEntities = (float) activatedEntityTicks / numTicks;
        float totalAvgEntities = (float) entityTicks / numTicks;

        float averagePlayers = (float) playerTicks / numTicks;

        float desiredTicks = (float) sampleTime / 1000 / 1000 / 1000 * 20;
        float averageTicks = numTicks / desiredTicks * 20;

        String format = ChatColor.DARK_AQUA + "%s" + ' ' + ChatColor.GRAY + "%s";

        //head data
        lines.add(TextComponent.fromLegacyText(String.format(format, "Total (sec):", round(totalSeconds))));
        lines.add(TextComponent.fromLegacyText(String.format(format, "Ticks:", round(numTicks))));
        lines.add(TextComponent.fromLegacyText(String.format(format, "Avg ticks:", round(averageTicks))));
        lines.add(TextComponent.fromLegacyText(String.format(format, "Server Load:", round(serverLoad))));
        lines.add(TextComponent.fromLegacyText(String.format(format, "AVG Players:", round(averagePlayers))));

        lines.add(TextComponent.fromLegacyText(String.format(format, "Activated Entities:", round(activatedAvgEntities))
                + " / " + round(totalAvgEntities)));

        //convert from nanoseconds to seconds
        String formatted = String.format(format, "Sample Time (sec):", round((float) sampleTime / 1000 / 1000 / 1000));
        lines.add(TextComponent.fromLegacyText(formatted));
    }

    private void parseTimings(Iterable<CustomTimingsHandler> handlers, Map<String, Timing> timings
            , Timing minecraftTiming, Timing breakdownTiming) {
//        FieldAccessor<CustomTimingsHandler> getParent = Reflection
//                .getField(CustomTimingsHandler.class, "parent", CustomTimingsHandler.class);
        FieldAccessor<String> getName = Reflection.getField(CustomTimingsHandler.class, "name", String.class);

        FieldAccessor<Long> getTotalTime = Reflection.getField(CustomTimingsHandler.class, "totalTime", Long.TYPE);
        FieldAccessor<Long> getCount = Reflection.getField(CustomTimingsHandler.class, "count", Long.TYPE);
//        FieldAccessor<Long> getViolations = Reflection.getField(CustomTimingsHandler.class, "violations", Long.TYPE);
        for (CustomTimingsHandler handler : handlers) {
            String subCategory = getName.get(handler);
            long totalTime = getTotalTime.get(handler);
            long count = getCount.get(handler);

            Timing active = minecraftTiming;
            if (subCategory.contains("Event: ")) {
                String pluginName = getProperty(subCategory, "Plugin");
                subCategory = getProperty(subCategory, "Event");

                active = timings.computeIfAbsent(pluginName, Timing::new);
            } else if (subCategory.contains("Task: ")) {
                String pluginName = getProperty(subCategory, "Task");
                subCategory = getProperty(subCategory, "Runnable");

                active = timings.computeIfAbsent(pluginName, Timing::new);
            }

            if (subCategory.startsWith(EXCLUDE_IDENTIFIER)) {
                breakdownTiming.addSubcategory(subCategory, totalTime, count);
            } else {
                active.addSubcategory(subCategory, totalTime, count);
                if (subCategory.startsWith("Task:")) {
                    breakdownTiming.addSubcategory(EXCLUDE_IDENTIFIER + "Tasks", totalTime, count);
                }

                if (active.getTotalTime() >= 0) {
                    active.addTotal(totalTime);
                }
            }
        }
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

        return prefix + String.valueOf(percent) + '%' + ChatColor.GRAY;
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
}

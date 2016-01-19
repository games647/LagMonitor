package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.MethodMeasurement;
import com.github.games647.lagmonitor.MonitorTask;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import java.util.Timer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.ChatPaginator;

public class MonitorCommand implements CommandExecutor {

    public static final long SAMPLE_INTERVALL = 100L;
    public static final long SAMPLE_DELAY = 3 * 1_000L;

    private static final int MIN_PERCENT = 1;

    private final LagMonitor plugin;

    private Timer timer;
    private MonitorTask monitorTask;

    public MonitorCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            String monitorCommand = args[0];
            if ("start".equalsIgnoreCase(monitorCommand)) {
                startMonitor(sender);
            } else if ("stop".equalsIgnoreCase(monitorCommand)) {
                stopMonitor(sender);
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "Invalid command parameter");
            }
        } else {
            if (monitorTask == null) {
                sender.sendMessage(ChatColor.DARK_RED + "Monitor is not running");
            } else {
                Map<String, MethodMeasurement> sampleResults = monitorTask.getSampleResults();
                //minecraft main thread
                MethodMeasurement mainThreadResults = sampleResults.get(Thread.currentThread().getName());

                MethodMeasurement parentMeasurement = mainThreadResults;
                long parentTime;
                //max depth with three chat pages
                for (int entries = 0; entries < ChatPaginator.OPEN_CHAT_PAGE_HEIGHT * 3; entries++) {
                    parentTime = parentMeasurement.getTotalTime();
                    Collection<MethodMeasurement> childInvokes = parentMeasurement.getChildInvokes().values();

                    if (childInvokes.isEmpty()) {
                        break;
                    }

                    List<MethodMeasurement> sortedList = Lists.newArrayList(childInvokes);
                    Collections.sort(sortedList);

                    //list the top element
                    MethodMeasurement topElement = sortedList.get(0);
                    float timePercent = topElement.getTimePercent(parentTime);
                    if (timePercent <= MIN_PERCENT) {
                        //ignore it
                        break;
                    }

                    sender.sendMessage(topElement.getName() + ' ' + timePercent);
                    parentMeasurement = topElement;
                }
            }
        }

        return true;
    }

    private void startMonitor(CommandSender sender) {
        if (monitorTask == null && timer == null) {
            timer = new Timer(plugin.getName() + "-Monitor");
            monitorTask = new MonitorTask(plugin);
            timer.scheduleAtFixedRate(monitorTask, SAMPLE_DELAY, SAMPLE_INTERVALL);

            sender.sendMessage(ChatColor.DARK_GREEN + "Monitor started");
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Monitor is already running");
        }
    }

    private void stopMonitor(CommandSender sender) {
        if (monitorTask == null && timer == null) {
            sender.sendMessage(ChatColor.DARK_RED + "Monitor is not running");
        } else {
            timer.cancel();
            monitorTask = null;
            timer = null;
            sender.sendMessage(ChatColor.DARK_GREEN + "Monitor stopped");
        }
    }
}

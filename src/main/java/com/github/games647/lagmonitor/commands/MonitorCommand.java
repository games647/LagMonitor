package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.MethodMeasurement;
import com.github.games647.lagmonitor.Pagination;
import com.github.games647.lagmonitor.tasks.MonitorTask;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import org.apache.commons.lang.StringUtils;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MonitorCommand implements CommandExecutor {

    public static final long SAMPLE_INTERVALL = 100L;
    public static final long SAMPLE_DELAY = 3 * 1_000L;

    private final LagMonitor plugin;

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
        } else if (monitorTask == null) {
            sender.sendMessage(ChatColor.DARK_RED + "Monitor is not running");
        } else {
            synchronized (monitorTask) {
                MethodMeasurement rootSample = monitorTask.getRootSample();

                printTrace(sender, 0, rootSample, 0);
            }
        }

        return true;
    }

    private void printTrace(CommandSender sender, long parentTime, MethodMeasurement current, int depth) {
        String space = StringUtils.repeat(" ", depth);

        long currentTime = current.getTotalTime();
        float timePercent = current.getTimePercent(parentTime);

        String clazz = ChatColor.DARK_AQUA + Pagination.filterPackageNames(current.getClassName());
        String method = ChatColor.DARK_GREEN + current.getMethod();
        sender.sendMessage(space + "[-] " + clazz + '.' + method + ' ' + ChatColor.GRAY + timePercent + '%');

        Collection<MethodMeasurement> childInvokes = current.getChildInvokes().values();
        List<MethodMeasurement> sortedList = Lists.newArrayList(childInvokes);
        Collections.sort(sortedList);

        for (MethodMeasurement child : sortedList) {
            printTrace(sender, currentTime, child, depth + 1);
        }
    }

    private void startMonitor(CommandSender sender) {
        Timer timer = plugin.getMonitorTimer();
        if (monitorTask == null && timer == null) {
            timer = new Timer(plugin.getName() + "-Monitor");
            plugin.setMonitorTimer(timer);

            monitorTask = new MonitorTask(plugin, Thread.currentThread().getId());
            timer.scheduleAtFixedRate(monitorTask, SAMPLE_DELAY, SAMPLE_INTERVALL);

            sender.sendMessage(ChatColor.DARK_GREEN + "Monitor started");
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Monitor is already running");
        }
    }

    private void stopMonitor(CommandSender sender) {
        Timer timer = plugin.getMonitorTimer();
        if (monitorTask == null && timer == null) {
            sender.sendMessage(ChatColor.DARK_RED + "Monitor is not running");
        } else {
            timer.cancel();
            timer.purge();
            monitorTask = null;
            plugin.setMonitorTimer(null);

            sender.sendMessage(ChatColor.DARK_GREEN + "Monitor stopped");
        }
    }
}

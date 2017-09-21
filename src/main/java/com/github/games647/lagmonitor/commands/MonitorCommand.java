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

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class MonitorCommand extends LagCommand {

    public static final long SAMPLE_INTERVAL = 100L;
    public static final long SAMPLE_DELAY = 3 * 1_000L;

    private MonitorTask monitorTask;

    public MonitorCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, command)) {
            sender.sendMessage(org.bukkit.ChatColor.DARK_RED + "Not whitelisted");
            return true;
        }

        if (args.length > 0) {
            String monitorCommand = args[0];
            if ("start".equalsIgnoreCase(monitorCommand)) {
                startMonitor(sender);
            } else if ("stop".equalsIgnoreCase(monitorCommand)) {
                stopMonitor(sender);
            } else if ("paste".equalsIgnoreCase(monitorCommand)) {
                pasteMonitor(sender);
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "Invalid command parameter");
            }
        } else if (monitorTask == null) {
            sender.sendMessage(ChatColor.DARK_RED + "Monitor is not running");
        } else {
            List<BaseComponent[]> lines = Lists.newArrayList();
            synchronized (monitorTask) {
                MethodMeasurement rootSample = monitorTask.getRootSample();
                printTrace(lines, 0, rootSample, 0);
            }

            Pagination pagination = new Pagination("Monitor", lines);
            pagination.send(sender);
            this.plugin.getPaginations().put(sender, pagination);
        }

        return true;
    }

    private void printTrace(List<BaseComponent[]> lines, long parentTime, MethodMeasurement current, int depth) {
        String space = StringUtils.repeat(" ", depth);

        long currentTime = current.getTotalTime();
        float timePercent = current.getTimePercent(parentTime);

        String clazz = Pagination.filterPackageNames(current.getClassName());
        String method = current.getMethod();
        lines.add(new ComponentBuilder(space + "[-] ")
                .append(clazz + '.')
                .color(ChatColor.DARK_AQUA)
                .append(method)
                .color(ChatColor.DARK_GREEN)
                .append(' ' + timePercent + "%")
                .color(ChatColor.GRAY)
                .create());

        Collection<MethodMeasurement> childInvokes = current.getChildInvokes().values();
        List<MethodMeasurement> sortedList = Lists.newArrayList(childInvokes);
        Collections.sort(sortedList);

        sortedList.forEach((child) -> printTrace(lines, currentTime, child, depth + 1));
    }

    private void startMonitor(CommandSender sender) {
        Timer timer = plugin.getMonitorTimer();
        if (monitorTask == null && timer == null) {
            timer = new Timer(plugin.getName() + "-Monitor");
            plugin.setMonitorTimer(timer);

            monitorTask = new MonitorTask(plugin, Thread.currentThread().getId());
            timer.scheduleAtFixedRate(monitorTask, SAMPLE_DELAY, SAMPLE_INTERVAL);

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

    private void pasteMonitor(final CommandSender sender) {
        Timer timer = plugin.getMonitorTimer();
        if (monitorTask == null && timer == null) {
            sender.sendMessage(ChatColor.DARK_RED + "Monitor is not running");
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String reportUrl = monitorTask.paste();
            if (reportUrl == null) {
                sender.sendMessage(ChatColor.DARK_RED + "Error occurred. Please check the console");
            } else {
                sender.sendMessage(ChatColor.DARK_GREEN + "Report url: " + reportUrl + ".profile");
            }
        });
    }
}

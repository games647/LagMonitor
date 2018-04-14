package com.github.games647.lagmonitor.command;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.MethodMeasurement;
import com.github.games647.lagmonitor.Pages;
import com.github.games647.lagmonitor.task.MonitorTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class MonitorCommand extends LagCommand {

    public static final long SAMPLE_INTERVAL = 100L;
    public static final long SAMPLE_DELAY = TimeUnit.SECONDS.toMillis(1)  / 2;

    private MonitorTask monitorTask;

    public MonitorCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canExecute(sender, command)) {
            return true;
        }

        if (args.length > 0) {
            String monitorCommand = args[0].toLowerCase();
            switch (monitorCommand) {
                case "start":
                    startMonitor(sender);
                    break;
                case "stop":
                    stopMonitor(sender);
                    break;
                case "paste":
                    pasteMonitor(sender);
                    break;
                default:
                    sendError(sender, "Invalid command parameter");
            }
        } else if (monitorTask == null) {
            sendError(sender, "Monitor is not running");
        } else {
            List<BaseComponent[]> lines = new ArrayList<>();
            synchronized (monitorTask) {
                MethodMeasurement rootSample = monitorTask.getRootSample();
                printTrace(lines, 0, rootSample, 0);
            }

            Pages pagination = new Pages("Monitor", lines);
            pagination.send(sender);
            this.plugin.getPageManager().setPagination(sender.getName(), pagination);
        }

        return true;
    }

    private void printTrace(List<BaseComponent[]> lines, long parentTime, MethodMeasurement current, int depth) {
        String space = StringUtils.repeat(" ", depth);

        long currentTime = current.getTotalTime();
        float timePercent = current.getTimePercent(parentTime);

        String clazz = Pages.filterPackageNames(current.getClassName());
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
        List<MethodMeasurement> sortedList = new ArrayList<>(childInvokes);
        Collections.sort(sortedList);

        sortedList.forEach((child) -> printTrace(lines, currentTime, child, depth + 1));
    }

    private void startMonitor(CommandSender sender) {
        Timer timer = plugin.getMonitorTimer();
        if (monitorTask == null && timer == null) {
            timer = new Timer(plugin.getName() + "-Monitor");
            plugin.setMonitorTimer(timer);

            monitorTask = new MonitorTask(plugin.getLogger(), Thread.currentThread().getId());
            timer.scheduleAtFixedRate(monitorTask, SAMPLE_DELAY, SAMPLE_INTERVAL);

            sender.sendMessage(ChatColor.DARK_GREEN + "Monitor started");
        } else {
            sendError(sender, "Monitor task is already running");
        }
    }

    private void stopMonitor(CommandSender sender) {
        Timer timer = plugin.getMonitorTimer();
        if (monitorTask == null && timer == null) {
            sendError(sender, "Monitor is not running");
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
            sendError(sender, "Monitor is not running");
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String reportUrl = monitorTask.paste();
            if (reportUrl == null) {
                sendError(sender, "Error occurred. Please check the console");
            } else {
                String profileUrl = reportUrl + ".profile";
                sender.spigot().sendMessage(new ComponentBuilder("Report url: " + profileUrl)
                        .color(ChatColor.GREEN)
                        .event(new ClickEvent(Action.OPEN_URL, profileUrl))
                        .create());
            }
        });
    }
}

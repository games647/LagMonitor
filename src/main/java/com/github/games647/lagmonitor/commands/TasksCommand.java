package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.Pagination;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class TasksCommand extends LagCommand {

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;
    private static final ChatColor SECONDARY_COLOR = ChatColor.GRAY;

    public TasksCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, command)) {
            sender.sendMessage(org.bukkit.ChatColor.DARK_RED + "Not whitelisted");
            return true;
        }

        List<BaseComponent[]> lines = new ArrayList<>();

        List<BukkitTask> pendingTasks = Bukkit.getScheduler().getPendingTasks();
        for (BukkitTask pendingTask : pendingTasks) {
            Plugin owner = pendingTask.getOwner();
            int taskId = pendingTask.getTaskId();
            boolean sync = pendingTask.isSync();

            String id = Integer.toString(taskId);
            if (sync) {
                id += "-Sync";
            } else if (Bukkit.getScheduler().isCurrentlyRunning(taskId)) {
                id += "-Running";
            }

            lines.add(new ComponentBuilder(owner.getName())
                    .color(PRIMARY_COLOR)
                    .append('-' + id)
                    .color(SECONDARY_COLOR)
                    .create());
            Class<?> runnableClass = getRunnableClass(pendingTask);
            if (runnableClass != null) {
                lines.add(new ComponentBuilder("    Task: ")
                        .color(PRIMARY_COLOR)
                        .append(runnableClass.getSimpleName())
                        .color(SECONDARY_COLOR)
                        .create());
            }
        }

        Pagination pagination = new Pagination("Stacktrace", lines);
        pagination.send(sender);
        plugin.getPaginations().put(sender.getName(), pagination);
        return true;
    }

    private Class<?> getRunnableClass(BukkitTask task) {
        try {
            Field runnableField = task.getClass().getDeclaredField("task");
            runnableField.setAccessible(true);
            Object runnable = runnableField.get(task);
            return runnable.getClass();
        } catch (NoSuchFieldException | IllegalAccessException ex) {
//            plugin.getLogger().log(Level.SEVERE, null, ex);
        }

        return null;
    }
}

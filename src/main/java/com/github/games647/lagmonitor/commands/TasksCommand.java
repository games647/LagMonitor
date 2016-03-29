package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import java.lang.reflect.Field;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;

public class TasksCommand implements CommandExecutor {

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;
    private static final ChatColor SECONDARY_COLOR = ChatColor.GRAY;

    private final LagMonitor plugin;

    public TasksCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<BukkitWorker> activeWorkers = Bukkit.getScheduler().getActiveWorkers();
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

            sender.sendMessage(PRIMARY_COLOR + owner.getName() + SECONDARY_COLOR + '-' + id);
            sender.sendMessage(PRIMARY_COLOR + "Task: " + SECONDARY_COLOR + getRunnableClass(pendingTask));
        }

        return true;
    }

    private Class<?> getRunnableClass(BukkitTask task) {
        try {
            Field runnableField = task.getClass().getDeclaredField("task");
            runnableField.setAccessible(true);
            Object runnable = runnableField.get(task);
            return runnable.getClass();
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        }

        return null;
    }
}

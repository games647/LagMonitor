package com.github.games647.lagmonitor.command.minecraft;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.Pages;
import com.github.games647.lagmonitor.command.LagCommand;
import com.github.games647.lagmonitor.traffic.Reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class TasksCommand extends LagCommand {

    private static final MethodHandle taskHandle;

    static {
        Class<?> taskClass = Reflection.getCraftBukkitClass("scheduler.CraftTask");

        MethodHandle localHandle = null;
        try {
            localHandle = MethodHandles.publicLookup().findGetter(taskClass, "task", Runnable.class);
        } catch (NoSuchFieldException | IllegalAccessException noSuchFieldEx) {
            //ignore
        }

        taskHandle = localHandle;
    }

    public TasksCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canExecute(sender, command)) {
            return true;
        }

        List<BaseComponent[]> lines = new ArrayList<>();

        List<BukkitTask> pendingTasks = Bukkit.getScheduler().getPendingTasks();
        for (BukkitTask pendingTask : pendingTasks) {
            lines.add(formatTask(pendingTask));

            Class<?> runnableClass = getRunnableClass(pendingTask);
            if (runnableClass != null) {
                lines.add(new ComponentBuilder("    Task: ")
                        .color(PRIMARY_COLOR.asBungee())
                        .append(runnableClass.getSimpleName())
                        .color(SECONDARY_COLOR.asBungee())
                        .create());
            }
        }

        Pages pagination = new Pages("Stacktrace", lines);
        pagination.send(sender);
        plugin.getPageManager().setPagination(sender.getName(), pagination);
        return true;
    }

    private BaseComponent[] formatTask(BukkitTask pendingTask) {
        Plugin owner = pendingTask.getOwner();
        int taskId = pendingTask.getTaskId();
        boolean sync = pendingTask.isSync();

        String id = Integer.toString(taskId);
        if (sync) {
            id += "-Sync";
        } else if (Bukkit.getScheduler().isCurrentlyRunning(taskId)) {
            id += "-Running";
        }

        return new ComponentBuilder(owner.getName())
                .color(PRIMARY_COLOR.asBungee())
                .append('-' + id)
                .color(SECONDARY_COLOR.asBungee())
                .create();
    }

    private Class<?> getRunnableClass(BukkitTask task) {
        try {
            return taskHandle.invoke(task).getClass();
        } catch (Exception ex) {
            //ignore
        } catch (Throwable throwable) {
            throw (Error) throwable;
        }

        return null;
    }
}

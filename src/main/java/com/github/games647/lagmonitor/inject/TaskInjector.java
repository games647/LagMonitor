package com.github.games647.lagmonitor.inject;

import com.github.games647.lagmonitor.traffic.Reflection;
import com.github.games647.lagmonitor.traffic.Reflection.FieldAccessor;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class TaskInjector implements Runnable {

    private final Runnable originalTask;

    private long totalTime;
    private long count;

    public TaskInjector(Runnable originalTask) {
        this.originalTask = originalTask;
    }

    @Override
    public void run() {
        long start = System.nanoTime();
        //todo add a more aggressive 10 ms cpu sample
        originalTask.run();
        long end = System.nanoTime();

        totalTime += end - start;
        count++;
    }

    public Runnable getOriginalTask() {
        return originalTask;
    }

    //sadly it works only with interval tasks
    //for single runs we would have to register a dynamic proxy
    public static void inject(Plugin plugin) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        List<BukkitTask> pendingTasks = scheduler.getPendingTasks();
        for (BukkitTask pendingTask : pendingTasks) {
            //we could ignore async tasks for now
            if (pendingTask.isSync() && pendingTask.getOwner().equals(plugin)) {
                FieldAccessor<Runnable> taskField = Reflection.getField(pendingTask.getClass(), "task", Runnable.class);

                Runnable originalTask = taskField.get(pendingTask);
                TaskInjector taskInjector = new TaskInjector(originalTask);
                taskField.set(pendingTask, taskInjector);
            }
        }
    }

    public static void restore(Plugin plugin) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        List<BukkitTask> pendingTasks = scheduler.getPendingTasks();
        for (BukkitTask pendingTask : pendingTasks) {
            //we could ignore async tasks for now
            if (pendingTask.isSync() && pendingTask.getOwner().equals(plugin)) {
                FieldAccessor<Runnable> taskField = Reflection.getField(pendingTask.getClass(), "task", Runnable.class);

                Runnable task = taskField.get(pendingTask);
                if (task instanceof TaskInjector) {
                    taskField.set(pendingTask, ((TaskInjector) task).originalTask);
                }
            }
        }
    }
}

package com.github.games647.lagmonitor.listeners;

import com.github.games647.lagmonitor.LagMonitor;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 * We can listen to events which are intended to run sync to the main thread.
 * If those events are fired on a async task the operation was likely not thread-safe.
 */
public class ThreadSafetyListener implements Listener {

    private final Thread mainThread = Thread.currentThread();
    private final LagMonitor plugin;

    public ThreadSafetyListener(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        checkSafety(quitEvent);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent moveEvent) {
        checkSafety(moveEvent);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent chunkLoadEvent) {
        checkSafety(chunkLoadEvent);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent chunkUnloadEvent) {
        checkSafety(chunkUnloadEvent);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent worldLoadEvent) {
        checkSafety(worldLoadEvent);
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent worldUnloadEvent) {
        checkSafety(worldUnloadEvent);
    }

    private void checkSafety(Event eventType) {
        if (Thread.currentThread() != mainThread && !eventType.isAsynchronous()) {
            String eventName = eventType.getEventName();
            throw new IllegalAccessError("Async operation for an sync Event:" + eventName);
        }
    }
}

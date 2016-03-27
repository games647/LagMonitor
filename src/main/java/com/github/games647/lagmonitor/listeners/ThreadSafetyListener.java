package com.github.games647.lagmonitor.listeners;

import com.github.games647.lagmonitor.LagMonitor;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
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
    public void onInventoryOpen(InventoryOpenEvent inventoryOpenEvent) {
        checkSafety(inventoryOpenEvent);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent moveEvent) {
        checkSafety(moveEvent);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent teleportEvent) {
        checkSafety(teleportEvent);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        checkSafety(quitEvent);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent creatureSpawnEvent) {
        checkSafety(creatureSpawnEvent);
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent itemSpawnEvent) {
        checkSafety(itemSpawnEvent);
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
    public void onWorldSave(WorldSaveEvent worldSaveEvent) {
        checkSafety(worldSaveEvent);
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent worldUnloadEvent) {
        checkSafety(worldUnloadEvent);
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent pluginEnableEvent) {
        checkSafety(pluginEnableEvent);
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent pluginDisableEvent) {
        checkSafety(pluginDisableEvent);
    }

    private void checkSafety(Event eventType) {
        if (Thread.currentThread() != mainThread && !eventType.isAsynchronous()) {
            String eventName = eventType.getEventName();
            throw new IllegalAccessError("Async operation for an sync Event:" + eventName);
        }
    }
}

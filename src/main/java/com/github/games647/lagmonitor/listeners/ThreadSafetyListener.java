package com.github.games647.lagmonitor.listeners;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.PluginUtil;
import com.github.games647.lagmonitor.PluginViolation;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Map.Entry;

import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.SpawnChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;

/**
 * We can listen to events which are intended to run sync to the main thread.
 * If those events are fired on a async task the operation was likely not thread-safe.
 */
public class ThreadSafetyListener implements Listener {

    private final LagMonitor plugin;

    private final Set<PluginViolation> violations = Sets.newHashSet();
    private final Set<String> violatedPlugins = Sets.newHashSet();

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
    public void onPlayerJoin(PlayerQuitEvent quitEvent) {
        checkSafety(quitEvent);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        checkSafety(quitEvent);
    }

    @EventHandler
    public void onItemHeldChange(PlayerItemHeldEvent itemHeldEvent) {
        checkSafety(itemHeldEvent);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent blockPhysicsEvent) {
        checkSafety(blockPhysicsEvent);
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent blockFromToEvent) {
        checkSafety(blockFromToEvent);
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

    @EventHandler
    public void onSpawnChange(SpawnChangeEvent spawnChangeEvent) {
        checkSafety(spawnChangeEvent);
    }

    private void checkSafety(Event eventType) {
        //async executing of sync event
        String eventName = eventType.getEventName();
        if (!Bukkit.isPrimaryThread() && !eventType.isAsynchronous()) {
            IllegalAccessException stackTraceCreator = new IllegalAccessException();
            StackTraceElement[] stackTrace = stackTraceCreator.getStackTrace();

            //remove the parts from LagMonitor
            StackTraceElement[] copyOfRange = Arrays.copyOfRange(stackTrace, 2, stackTrace.length);
            Entry<Plugin, StackTraceElement> foundPlugin = PluginUtil.findPlugin(copyOfRange);

            PluginViolation violation = new PluginViolation(eventName);
            if (foundPlugin != null) {
                String pluginName = foundPlugin.getKey().getName();
                violation = new PluginViolation(pluginName, foundPlugin.getValue(), eventName);

                if (!violatedPlugins.add(violation.getPluginName()) && plugin.getConfig().getBoolean("oncePerPlugin")) {
                    return;
                }
            }

            if (!violations.add(violation)) {
                return;
            }

            plugin.getLogger().log(Level.WARNING, "Plugin {0} is performed a async operation for an sync Event "
                    + "This could be a very dangerous {1}."
                    + "Report it to the plugin author", new Object[]{violation.getPluginName(), eventName});

            if (plugin.getConfig().getBoolean("hideStacktrace")) {
                plugin.getLogger().log(Level.WARNING, "Source: {0}, method {1}, line{2}"
                        , new Object[]{violation.getSourceFile(), violation.getMethodName(), violation.getLineNumber()});
            } else {
                plugin.getLogger().log(Level.WARNING, "", stackTraceCreator);
            }
        }
    }
}

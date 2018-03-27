package com.github.games647.lagmonitor.storage;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.utils.LagUtils;
import com.github.games647.lagmonitor.NativeData;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import static com.github.games647.lagmonitor.utils.LagUtils.round;

public class MonitorSaveTask implements Runnable {

    protected final LagMonitor plugin;
    protected final Storage storage;

    public MonitorSaveTask(LagMonitor plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    @Override
    public void run() {
        try {
            int monitorId = onMonitorSave(storage);
            if (monitorId == -1) {
                //error occurred
                return;
            }

            Map<UUID, WorldData> worldsData = getWorldData();
            if (!storage.saveWorlds(monitorId, worldsData.values())) {
                //error occurred
                return;
            }

            List<PlayerData> playerData = getPlayerData(worldsData);
            storage.savePlayers(playerData);
        } catch (ExecutionException | InterruptedException ex) {
            plugin.getLogger().log(Level.SEVERE, "Error saving monitoring data", ex);
        }
    }

    private List<PlayerData> getPlayerData(final Map<UUID, WorldData> worldsData)
            throws InterruptedException, ExecutionException {
        Future<List<PlayerData>> playerFuture = Bukkit.getScheduler()
                .callSyncMethod(plugin, () -> {
                    Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                    List<PlayerData> playerData = Lists.newArrayListWithCapacity(onlinePlayers.size());
                    for (Player player : onlinePlayers) {
                        UUID worldId = player.getWorld().getUID();

                        int worldRowId = 0;
                        WorldData worldData = worldsData.get(worldId);
                        if (worldData != null) {
                            worldRowId = worldData.getRowId();
                        }

                        int lastPing = (int) plugin.getPingManager().getHistory(player.getName()).getLastSample();
                        String playerName = player.getName();
                        UUID playerId = player.getUniqueId();
                        playerData.add(new PlayerData(worldRowId, playerId, playerName, lastPing));
                    }

                    return playerData;
                });
        
        return playerFuture.get();
    }

    private Map<UUID, WorldData> getWorldData()
            throws ExecutionException, InterruptedException {
        //this is not thread-safe and have to run sync
        Future<Map<UUID, WorldData>> worldFuture = Bukkit.getScheduler()
                .callSyncMethod(plugin, () -> {
                    List<World> worlds = Bukkit.getWorlds();
                    Map<UUID, WorldData> worldsData = Maps.newHashMapWithExpectedSize(worlds.size());
                    for (World world : worlds) {
                        UUID worldId = world.getUID();
                        String worldName = world.getName();
                        int tileEntities = 0;
                        for (Chunk loadedChunk : world.getLoadedChunks()) {
                            tileEntities += loadedChunk.getTileEntities().length;
                        }

                        int entities = world.getEntities().size();
                        int chunks = world.getLoadedChunks().length;

                        WorldData worldData = new WorldData(worldName, chunks, tileEntities, entities);
                        worldsData.put(worldId, worldData);
                    }

                    return worldsData;
                });

        Map<UUID, WorldData> worldsData = worldFuture.get();
        //this can run async because it's thread-safe
        for (Entry<UUID, WorldData> entry : worldsData.entrySet()) {
            UUID worldId = entry.getKey();
            WorldData worldData = entry.getValue();
            Path worldFolder = Bukkit.getWorld(worldId).getWorldFolder().toPath();
            worldData.setWorldSize(LagUtils.byteToMega(LagUtils.getFolderSize(plugin.getLogger(), worldFolder)));
        }
        
        return worldsData;
    }

    private int onMonitorSave(Storage storage) {
        Runtime runtime = Runtime.getRuntime();
        int maxMemory = LagUtils.byteToMega(runtime.maxMemory());
        //we need the free ram not the free heap
        int usedRam = LagUtils.byteToMega(runtime.totalMemory() - runtime.freeMemory());
        int freeRam = maxMemory - usedRam;

        float freeRamPct = round((freeRam * 100) / maxMemory, 4);

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        float loadAvg = round(osBean.getSystemLoadAverage(), 4);
        if (loadAvg == -1.0F) {
            //windows doesn't support this
            loadAvg = 0;
        }

        NativeData nativeData = plugin.getNativeData();
        float systemUsage = round(nativeData.getCPULoad() * 100, 4);
        float procUsage = round(nativeData.getProcessCPULoad() * 100, 4);

        int totalOsMemory = LagUtils.byteToMega(nativeData.getTotalMemory());
        int freeOsRam = LagUtils.byteToMega(nativeData.getFreeMemory());

        float freeOsRamPct = round((freeOsRam * 100) / totalOsMemory, 4);
        return storage.saveMonitor(procUsage, systemUsage, freeRam, freeRamPct, freeOsRam, freeOsRamPct, loadAvg);
    }
}

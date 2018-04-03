package com.github.games647.lagmonitor.storage;

import org.bukkit.Chunk;
import org.bukkit.World;

public class WorldData {

    private final String worldName;
    private final int loadedChunks;
    private final int tileEntities;
    private final int entities;

    private int worldSize;
    private int rowId;

    public static WorldData fromWorld(World world) {
        String worldName = world.getName();
        int tileEntities = 0;
        for (Chunk loadedChunk : world.getLoadedChunks()) {
            tileEntities += loadedChunk.getTileEntities().length;
        }

        int entities = world.getEntities().size();
        int chunks = world.getLoadedChunks().length;

        return new WorldData(worldName, chunks, tileEntities, entities);
    }

    public WorldData(String worldName, int loadedChunks, int tileEntities, int entities) {
        this.worldName = worldName;
        this.loadedChunks = loadedChunks;
        this.tileEntities = tileEntities;
        this.entities = entities;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getLoadedChunks() {
        return loadedChunks;
    }

    public int getTileEntities() {
        return tileEntities;
    }

    public int getEntities() {
        return entities;
    }

    public int getWorldSize() {
        return worldSize;
    }

    public void setWorldSize(int worldSize) {
        this.worldSize = worldSize;
    }

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "worldName=" + worldName
                + ", loadedChunks=" + loadedChunks
                + ", tileEntities=" + tileEntities
                + ", entities=" + entities
                + ", worldSize=" + worldSize
                + ", rowId=" + rowId
                + '}';
    }
}

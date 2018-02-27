package com.github.games647.lagmonitor.storage;

public class WorldData {

    private final String worldName;
    private final int loadedChunks;
    private final int tileEntities;
    private final int entities;

    private int worldSize;
    private int rowId;

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

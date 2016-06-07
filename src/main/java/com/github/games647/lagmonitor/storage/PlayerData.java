package com.github.games647.lagmonitor.storage;

import java.util.UUID;

public class PlayerData {

    private final int worldId;
    private final UUID uuid;
    private final String playerName;
    private final int ping;

    public PlayerData(int worldId, UUID uuid, String playerName, int ping) {
        this.worldId = worldId;
        this.uuid = uuid;
        this.playerName = playerName;
        this.ping = ping;
    }

    public int getWorldId() {
        return worldId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPing() {
        return ping;
    }
}

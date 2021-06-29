package com.github.games647.lagmonitor.ping;

import org.bukkit.entity.Player;

public class SpigotPing implements PingFetcher {

    @Override
    public boolean isAvailable() {
        try {
            //Only available in Paper
            Player.class.getDeclaredMethod("getPing");
            return true;
        } catch (NoSuchMethodException noSuchMethodEx) {
            return false;
        }
    }

    @Override
    public int getPing(Player player) {
        return player.getPing();
    }
}

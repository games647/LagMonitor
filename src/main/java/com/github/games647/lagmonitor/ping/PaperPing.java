package com.github.games647.lagmonitor.ping;

import org.bukkit.entity.Player;

public class PaperPing implements PingFetcher {

    @Override
    public boolean isAvailable() {
        try {
            //Only available in Paper
            Player.Spigot.class.getDeclaredMethod("getPing");
            return true;
        } catch (NoSuchMethodException noSuchMethodEx) {
            return false;
        }
    }

    @Override
    public int getPing(Player player) {
        return player.spigot().getPing();
    }
}

package com.github.games647.lagmonitor.ping;

import org.bukkit.entity.Player;

public interface PingFetcher {

    boolean isAvailable();

    int getPing(Player player);
}

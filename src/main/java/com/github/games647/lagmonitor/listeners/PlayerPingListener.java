package com.github.games647.lagmonitor.listeners;

import com.github.games647.lagmonitor.LagMonitor;

import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerPingListener implements Listener {

    private final LagMonitor plugin;

    public PlayerPingListener(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        Player player = joinEvent.getPlayer();
        
        plugin.getLogger().log(Level.INFO, "Player joined - starting player tracking {0}", player.getName());
        plugin.getPingHistoryTask().addPlayer(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        Player player = quitEvent.getPlayer();
        plugin.getPingHistoryTask().removePlayer(player);
    }
}

package com.github.games647.lagmonitor.listeners;

import com.github.games647.lagmonitor.LagMonitor;

import org.bukkit.Bukkit;
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

        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getPingHistoryTask().addPlayer(player), 2 * 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        Player player = quitEvent.getPlayer();
        plugin.getPingHistoryTask().removePlayer(player);
    }
}

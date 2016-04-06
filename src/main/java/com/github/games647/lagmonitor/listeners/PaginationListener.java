package com.github.games647.lagmonitor.listeners;

import com.github.games647.lagmonitor.LagMonitor;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PaginationListener implements Listener {

    private final LagMonitor plugin;

    public PaginationListener(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        Player player = quitEvent.getPlayer();
        plugin.getPaginations().remove(player);
    }
}

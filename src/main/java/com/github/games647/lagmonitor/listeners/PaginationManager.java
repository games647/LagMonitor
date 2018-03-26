package com.github.games647.lagmonitor.listeners;

import com.github.games647.lagmonitor.Pagination;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PaginationManager implements Listener {

    private final Map<String, Pagination> paginations = new HashMap<>();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        paginations.remove(quitEvent.getPlayer().getName());
    }

    public Pagination getPagination(String username) {
        return paginations.get(username);
    }

    public void setPagination(String username, Pagination pagination) {
        paginations.put(username, pagination);
    }
}

package com.github.games647.lagmonitor.listeners;

import com.github.games647.lagmonitor.Pagination;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PaginationManager implements Listener {

    private final Map<String, Pagination> pages = new HashMap<>();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        pages.remove(quitEvent.getPlayer().getName());
    }

    public Pagination getPagination(String username) {
        return pages.get(username);
    }

    public void setPagination(String username, Pagination pagination) {
        pages.put(username, pagination);
    }
}

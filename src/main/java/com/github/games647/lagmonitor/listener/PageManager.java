package com.github.games647.lagmonitor.listener;

import com.github.games647.lagmonitor.Pages;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PageManager implements Listener {

    private final Map<String, Pages> pages = new HashMap<>();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        pages.remove(quitEvent.getPlayer().getName());
    }

    public Pages getPagination(String username) {
        return pages.get(username);
    }

    public void setPagination(String username, Pages pagination) {
        pages.put(username, pagination);
    }
}

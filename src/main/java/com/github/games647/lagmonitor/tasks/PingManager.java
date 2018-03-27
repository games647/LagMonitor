package com.github.games647.lagmonitor.tasks;

import com.github.games647.lagmonitor.utils.RollingOverHistory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class PingManager implements Runnable, Listener {

    private static final int SAMPLE_SIZE = 5;

    private final Map<String, RollingOverHistory> playerHistory = new HashMap<>();
    private final Plugin plugin;

    private Method getHandleMethod;
    private Field pingField;

    public PingManager(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        playerHistory.forEach((playerName, history) -> {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null) {
                int ping = getPing(player);

                history.add(ping);
            }
        });
    }

    public RollingOverHistory getHistory(String playerName) {
        return playerHistory.get(playerName);
    }

    public void addPlayer(Player player) {
        int reflectionPing = getPing(player);
        playerHistory.put(player.getName(), new RollingOverHistory(SAMPLE_SIZE, reflectionPing));
    }

    public void removePlayer(Player player) {
        playerHistory.remove(player.getName());
    }

    private int getPing(Player player) {
        return getReflectionPing(player);
    }

    private int getReflectionPing(Player player) {
        try {
            if (getHandleMethod == null) {
                getHandleMethod = player.getClass().getDeclaredMethod("getHandle");
                //disable java security check. This will speed it a little
                getHandleMethod.setAccessible(true);
            }

            Object entityPlayer = getHandleMethod.invoke(player);
            if (pingField == null) {
                pingField = entityPlayer.getClass().getDeclaredField("ping");
                //disable java security check. This will speed it a little
                pingField.setAccessible(true);
            }

            //returns the found int value
            return pingField.getInt(entityPlayer);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        Player player = joinEvent.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                addPlayer(player);
            }
        }, 2 * 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        removePlayer(quitEvent.getPlayer());
    }

    public void clear() {
        playerHistory.clear();
    }
}

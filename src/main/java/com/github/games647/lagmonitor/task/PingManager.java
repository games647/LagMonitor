package com.github.games647.lagmonitor.task;

import com.github.games647.lagmonitor.ping.PingFetcher;
import com.github.games647.lagmonitor.ping.ReflectionPing;
import com.github.games647.lagmonitor.util.RollingOverHistory;

import java.lang.reflect.InvocationTargetException;
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

    //the server is pinging the client every 40 Ticks (2 sec) - so check it then
    //https://github.com/bergerkiller/CraftSource/blob/master/net.minecraft.server/PlayerConnection.java#L178
    public static final int PING_INTERVAL = 2 * 20;
    private static final int SAMPLE_SIZE = 5;

    private final Map<String, RollingOverHistory> playerHistory = new HashMap<>();
    private final Plugin plugin;
    private final PingFetcher pingFetcher;

    public PingManager(Plugin plugin) throws ReflectiveOperationException {
        this.pingFetcher = initializePingFetchur();
        this.plugin = plugin;
    }

    private PingFetcher initializePingFetchur()
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // List<Class<? extends PingFetcher>> fetchurs = Lists.newArrayList(
        //         SpigotPing.class, PaperPing.class, ReflectionPing.class
        // );
        // for (Class<? extends PingFetcher> fetchurClass : fetchurs) {
        //     PingFetcher fetchur = fetchurClass.getDeclaredConstructor().newInstance();
        //     if (fetchur.isAvailable())
        //         return fetchur;
        // }
        return new ReflectionPing();

        // throw new NoSuchMethodException("No valid ping fetcher found");
    }

    @Override
    public void run() {
        playerHistory.forEach((playerName, history) -> {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null) {
                int ping = pingFetcher.getPing(player);
                history.add(ping);
            }
        });
    }

    public RollingOverHistory getHistory(String playerName) {
        return playerHistory.get(playerName);
    }

    public void addPlayer(Player player) {
        int ping = pingFetcher.getPing(player);
        playerHistory.put(player.getName(), new RollingOverHistory(SAMPLE_SIZE, ping));
    }

    public void removePlayer(Player player) {
        playerHistory.remove(player.getName());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        Player player = joinEvent.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                addPlayer(player);
            }
        }, PING_INTERVAL);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        removePlayer(quitEvent.getPlayer());
    }

    public void clear() {
        playerHistory.clear();
    }
}

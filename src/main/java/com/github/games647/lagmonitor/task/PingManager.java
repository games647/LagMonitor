package com.github.games647.lagmonitor.task;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.traffic.Reflection;
import com.github.games647.lagmonitor.util.RollingOverHistory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class PingManager implements Runnable, Listener {

    //the server is pinging the client every 40 Ticks (2 sec) - so check it then
    //https://github.com/bergerkiller/CraftSource/blob/master/net.minecraft.server/PlayerConnection.java#L178
    public static final int PING_INTERVAL = 2 * 20;
    private static final int SAMPLE_SIZE = 5;

    private static final boolean pingMethodAvailable;

    private static final MethodHandle pingFromPlayerHandle;

    static {
        pingMethodAvailable = isPingMethodAvailable();

        MethodHandle localPing = null;
        if (!pingMethodAvailable) {
            Class<?> craftPlayerClass = Reflection.getCraftBukkitClass("entity.CraftPlayer");
            Class<?> entityPlayer = Reflection.getMinecraftClass("EntityPlayer");

            Lookup lookup = MethodHandles.publicLookup();
            try {
                MethodType type = MethodType.methodType(entityPlayer);
                MethodHandle getHandle = lookup.findVirtual(craftPlayerClass, "getHandle", type)
                        // allow interface with invokeExact
                        .asType(MethodType.methodType(Player.class));

                MethodHandle pingField = lookup.findGetter(entityPlayer, "ping", Integer.TYPE);

                // combine the handles to invoke it only once
                // *getPing(getHandle*) -> add the result of getHandle to the next getPing call
                // a call to this handle will get the ping from a player instance
                localPing = MethodHandles.collectArguments(pingField, 0, getHandle);
            } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException reflectiveEx) {
                Logger logger = JavaPlugin.getPlugin(LagMonitor.class).getLogger();
                logger.log(Level.WARNING, "Cannot find ping field/method", reflectiveEx);
            }
        }

        pingFromPlayerHandle = localPing;
    }

    private final Map<String, RollingOverHistory> playerHistory = new HashMap<>();
    private final Plugin plugin;

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
        if (pingMethodAvailable) {
            return player.spigot().getPing();
        }

        return getReflectionPing(player);
    }

    private int getReflectionPing(Player player) {
        try {
            return (int) pingFromPlayerHandle.invokeExact(player);
        } catch (Exception ex) {
            return -1;
        } catch (Throwable throwable) {
            throw (Error) throwable;
        }
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

    private static boolean isPingMethodAvailable() {
        try {
            //Only available in Paper
            Player.Spigot.class.getDeclaredMethod("getPing");
            return true;
        } catch (NoSuchMethodException noSuchMethodEx) {
            return false;
        }
    }
}

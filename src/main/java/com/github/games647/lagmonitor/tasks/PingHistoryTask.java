package com.github.games647.lagmonitor.tasks;

import com.github.games647.lagmonitor.RollingOverHistory;
import com.github.games647.lagmonitor.traffic.Reflection;
import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PingHistoryTask implements Runnable {

    private static final int SAMPLE_SIZE = 5;

    private final Map<Player, RollingOverHistory> playerHistory = Maps.newHashMap();

    private final boolean pingMethodAvailable;
    private Method getHandleMethod;
    private Field pingField;

    public PingHistoryTask() {
        boolean methodFound;
        try {
            methodFound = Reflection.getMethod(Player.Spigot.class, "getPing") != null;
        } catch (Exception ex) {
            methodFound = false;
        }

        pingMethodAvailable = methodFound;
    }

    @Override
    public void run() {
        for (Entry<Player, RollingOverHistory> entry : playerHistory.entrySet()) {
            Player player = entry.getKey();
            int ping = getPing(player);

            RollingOverHistory history = entry.getValue();
            history.add(ping);
        }
    }

    public RollingOverHistory getHistory(Player player) {
        return playerHistory.get(player);
    }

    public void addPlayer(Player player) {
        int reflectionPing = getPing(player);
        playerHistory.put(player, new RollingOverHistory(SAMPLE_SIZE, reflectionPing));
    }

    public void removePlayer(Player player) {
        playerHistory.remove(player);
    }

    private int getPing(Player player) {
        //PaperSpigot method - commented out because the compiler conflicts with the paper and spigot api
//        if (pingMethodAvailable) {
//            return player.spigot().getPing();
//        }

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
                if (isModdedServer()) {
                    //MCPC has a remapper, but it doesn't work if we get the class dynamic
                    setMCPCPing(entityPlayer);
                } else {
                    pingField = entityPlayer.getClass().getDeclaredField("ping");
                    //disable java security check. This will speed it a little
                    pingField.setAccessible(true);
                }
            }

            //returns the found int value
            return pingField.getInt(entityPlayer);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean isModdedServer() {
        //aggressive checking for modded servers
        List<String> versionNames = Arrays.asList(Bukkit.getVersion(), Bukkit.getName(), Bukkit.getServer().toString());
        for (String version : versionNames) {
            if (version.contains("MCPC") || version.contains("Cauldron")) {
                return true;
            }
        }

        return false;
    }

    private void setMCPCPing(Object entityPlayer) {
        //this isn't secure, because it detects the ping variable by the order of the fields
        Class<?> lastType = null;
        Field lastIntField = null;
        for (Field field : entityPlayer.getClass().getDeclaredFields()) {
            if (field.getType() == Integer.TYPE
                    && Modifier.isPublic(field.getModifiers())
                    && lastType == Boolean.TYPE) {
                lastIntField = field;
                continue;
            }

            if (field.getType() == Boolean.TYPE && lastIntField != null) {
                pingField = lastIntField;
                //disable java security check. This will speed it a little
                pingField.setAccessible(true);
                break;
            }

            lastIntField = null;
            lastType = field.getType();
        }
    }
}

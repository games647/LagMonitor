package com.github.games647.lagmonitor.threading;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PluginUtil {

    public static Entry<Plugin, StackTraceElement> findPlugin(StackTraceElement[] stacktrace) {
        for (StackTraceElement elem : stacktrace) {
            try {
                Plugin plugin = getPluginByClass(Class.forName(elem.getClassName()));
                if (plugin != null) {
                    HashMap<Plugin, StackTraceElement> map = Maps.newHashMapWithExpectedSize(1);
                    map.put(plugin, elem);
                    return map.entrySet().iterator().next();
                }
            } catch (ClassNotFoundException ex) {
                //if this class cannot be loaded then it could be something native so ignore it
            }
        }

        return null;
    }

    public static Plugin getPluginByClass(Class<?> clazz) {
        ClassLoader loader = clazz.getClassLoader();
        synchronized (Bukkit.getPluginManager()) {
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (plugin.getClass().getClassLoader() == loader) {
                    return plugin;
                }
            }
        }

        return null;
    }

    private PluginUtil() {
        //utility class
    }
}

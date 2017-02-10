package com.github.games647.lagmonitor.threading;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PluginUtil {

    public static Entry<String, StackTraceElement> findPlugin(StackTraceElement[] stacktrace) {
        for (StackTraceElement elem : stacktrace) {
            try {
                Class<?> clazz = Class.forName(elem.getClassName());
                Plugin plugin = getPluginByClass(clazz);
                if (plugin != null) {
                    Map<String, StackTraceElement> map = Maps.newHashMapWithExpectedSize(1);
                    map.put(plugin.getName(), elem);
                    return map.entrySet().iterator().next();
                } else if (clazz.getSimpleName().equals("VanillaCommandWrapper")) {
                    Map<String, StackTraceElement> map = Maps.newHashMapWithExpectedSize(1);
                    map.put("Vanilla", elem);
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

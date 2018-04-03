package com.github.games647.lagmonitor;

import com.github.games647.lagmonitor.commands.EnvironmentCommand;
import com.github.games647.lagmonitor.commands.GraphCommand;
import com.github.games647.lagmonitor.commands.HelpCommand;
import com.github.games647.lagmonitor.commands.MbeanCommand;
import com.github.games647.lagmonitor.commands.MonitorCommand;
import com.github.games647.lagmonitor.commands.NativeCommand;
import com.github.games647.lagmonitor.commands.NetworkCommand;
import com.github.games647.lagmonitor.commands.PaginationCommand;
import com.github.games647.lagmonitor.commands.StackTraceCommand;
import com.github.games647.lagmonitor.commands.VmCommand;
import com.github.games647.lagmonitor.commands.dump.FlightCommand;
import com.github.games647.lagmonitor.commands.dump.HeapCommand;
import com.github.games647.lagmonitor.commands.dump.ThreadCommand;
import com.github.games647.lagmonitor.commands.minecraft.PingCommand;
import com.github.games647.lagmonitor.commands.minecraft.SystemCommand;
import com.github.games647.lagmonitor.commands.minecraft.TPSCommand;
import com.github.games647.lagmonitor.commands.minecraft.TasksCommand;
import com.github.games647.lagmonitor.commands.timings.PaperTimingsCommand;
import com.github.games647.lagmonitor.commands.timings.SpigotTimingsCommand;
import com.github.games647.lagmonitor.inject.CommandInjector;
import com.github.games647.lagmonitor.inject.ListenerInjector;
import com.github.games647.lagmonitor.inject.TaskInjector;
import com.github.games647.lagmonitor.listeners.BlockingConnectionSelector;
import com.github.games647.lagmonitor.listeners.GraphListener;
import com.github.games647.lagmonitor.listeners.PageManager;
import com.github.games647.lagmonitor.listeners.ThreadSafetyListener;
import com.github.games647.lagmonitor.storage.MonitorSaveTask;
import com.github.games647.lagmonitor.storage.NativeSaveTask;
import com.github.games647.lagmonitor.storage.Storage;
import com.github.games647.lagmonitor.storage.TPSSaveTask;
import com.github.games647.lagmonitor.tasks.IODetectorTask;
import com.github.games647.lagmonitor.tasks.PingManager;
import com.github.games647.lagmonitor.tasks.TPSHistoryTask;
import com.github.games647.lagmonitor.threading.BlockingActionManager;
import com.github.games647.lagmonitor.threading.BlockingSecurityManager;
import com.github.games647.lagmonitor.threading.Injectable;
import com.github.games647.lagmonitor.traffic.TrafficReader;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Timer;
import java.util.logging.Level;

import oshi.SystemInfo;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class LagMonitor extends JavaPlugin {

    private static final String JNA_FILE = "jna-4.4.0.jar";

    private static final int DETECTION_THRESHOLD = 10;

    private final PingManager pingManager = new PingManager(this);
    private final BlockingActionManager actionManager = new BlockingActionManager(this);
    private final PageManager pageManager = new PageManager();
    private final TPSHistoryTask tpsHistoryTask = new TPSHistoryTask();

    private NativeData nativeData;
    private TrafficReader trafficReader;
    private Timer blockDetectionTimer;
    private Timer monitorTimer;

    public LagMonitor() {
        //always debug jna loading
        System.setProperty("jna.debug_load", String.valueOf(true));
        System.setProperty("jna.debug_load.jna", String.valueOf(true));
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupNativeAdapter();

        if (Files.notExists(getDataFolder().toPath().resolve("default.jfc"))) {
            saveResource("default.jfc", false);
        }

        //register schedule tasks
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.runTaskTimer(this, tpsHistoryTask, 20L, TPSHistoryTask.RUN_INTERVAL);
        scheduler.runTaskTimer(this, pingManager, 20L, PingManager.PING_INTERVAL);

        //register listeners
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new GraphListener(), this);
        pluginManager.registerEvents(pageManager, this);
        pluginManager.registerEvents(pingManager, this);

        //add the player to the list in the case the plugin is loaded at runtime
        Bukkit.getOnlinePlayers().forEach(pingManager::addPlayer);

        if (getConfig().getBoolean("traffic-counter")) {
            try {
                trafficReader = new TrafficReader(this);
            } catch (Exception ex) {
                getLogger().log(Level.SEVERE, "Failed to initialize packet reader", ex);
            }
        }

        if (getConfig().getBoolean("thread-safety-check")) {
            pluginManager.registerEvents(new ThreadSafetyListener(actionManager), this);
        }

        if (getConfig().getBoolean("thread-block-detection")) {
            scheduler.runTask(this, () -> {
                blockDetectionTimer = new Timer(getName() + "-Thread-Blocking-Detection");
                IODetectorTask detectorTask = new IODetectorTask(actionManager, Thread.currentThread());
                blockDetectionTimer.scheduleAtFixedRate(detectorTask, DETECTION_THRESHOLD, DETECTION_THRESHOLD);
            });
        }

        if (getConfig().getBoolean("monitor-database")) {
            setupMonitoringDatabase();
        }

        if (getConfig().getBoolean("socket-block-detection")) {
            scheduler.runTask(this, () -> new BlockingConnectionSelector(actionManager).inject());
        }

        if (getConfig().getBoolean("securityMangerBlockingCheck")) {
            scheduler.runTask(this, () -> new BlockingSecurityManager(actionManager).inject());
        }

        registerCommands();
    }

    private void setupNativeAdapter() {
        SystemInfo info = null;
        try {
            Class.forName("com.sun.jna.Platform");
            info = new SystemInfo();

            getLogger().info("Found JNA native library. Enabling extended native data support to display more data");
        } catch (ClassNotFoundException classNotFoundEx) {
            Path jnaPath = getDataFolder().toPath().resolve(JNA_FILE);
            if (Files.exists(jnaPath)) {
                if (getClassLoader() instanceof URLClassLoader) {
                    try {
                        Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                        addUrl.setAccessible(true);
                        addUrl.invoke(getClassLoader(), jnaPath.toUri().toURL());

                        getLogger().info("Added JNA to the classpath");
                    } catch (ReflectiveOperationException | MalformedURLException reflectiveEx) {
                        getLogger().log(Level.INFO, "Cannot add JNA to the classpath", reflectiveEx);
                    }
                }
            } else {
                getLogger().info("JNA not found. " +
                        "Please download the this to the folder of this plugin to display more data about your setup");
                getLogger().info("https://repo1.maven.org/maven2/net/java/dev/jna/jna/4.4.0/jna-4.4.0.jar");
            }
        }

        nativeData = new NativeData(getLogger(), info);
    }

    private void setupMonitoringDatabase() {
        try {
            String host = getConfig().getString("host");
            int port = getConfig().getInt("port");
            String database = getConfig().getString("database");

            String username = getConfig().getString("username");
            String password = getConfig().getString("password");
            String tablePrefix = getConfig().getString("tablePrefix");
            Storage storage = new Storage(getLogger(), host, port, database, username, password, tablePrefix);
            storage.createTables();

            BukkitScheduler scheduler = getServer().getScheduler();
            scheduler.runTaskTimerAsynchronously(this, new TPSSaveTask(tpsHistoryTask, storage), 20L,
                     getConfig().getInt("tps-save-interval") * 20L);
            //this can run async because it runs independently from the main thread
            scheduler.runTaskTimerAsynchronously(this, new MonitorSaveTask(this, storage),
                    20L,getConfig().getInt("monitor-save-interval") * 20L);
            scheduler.runTaskTimerAsynchronously(this, new NativeSaveTask(this, storage),
                    20L,getConfig().getInt("native-save-interval") * 20L);
        } catch (SQLException sqlEx) {
            getLogger().log(Level.SEVERE, "Failed to setup monitoring database", sqlEx);
        }
    }

    @Override
    public void onDisable() {
        if (trafficReader != null) {
            trafficReader.close();
            trafficReader = null;
        }

        close(blockDetectionTimer);
        blockDetectionTimer = null;

        close(monitorTimer);
        monitorTimer = null;

        //restore the security manager
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager instanceof BlockingSecurityManager) {
            ((Injectable) securityManager).restore();
        }

        ProxySelector proxySelector = ProxySelector.getDefault();
        if (proxySelector instanceof BlockingConnectionSelector) {
            ((Injectable) proxySelector).restore();
        }

        for (Plugin plugin : getServer().getPluginManager().getPlugins()) {
            ListenerInjector.restore(plugin);
            CommandInjector.restore(plugin);
            TaskInjector.restore(plugin);
        }
    }

    private void close(Timer timer) {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    public PageManager getPageManager() {
        return pageManager;
    }

    public Timer getMonitorTimer() {
        return monitorTimer;
    }

    public void setMonitorTimer(Timer monitorTimer) {
        this.monitorTimer = monitorTimer;
    }

    public TrafficReader getTrafficReader() {
        return trafficReader;
    }

    public TPSHistoryTask getTpsHistoryTask() {
        return tpsHistoryTask;
    }

    public PingManager getPingManager() {
        return pingManager;
    }

    public NativeData getNativeData() {
        return nativeData;
    }

    private void registerCommands() {
        getCommand(getName()).setExecutor(new HelpCommand(this));

        getCommand("ping").setExecutor(new PingCommand(this));
        getCommand("stacktrace").setExecutor(new StackTraceCommand(this));
        getCommand("thread").setExecutor(new ThreadCommand(this));
        getCommand("tpshistory").setExecutor(new TPSCommand(this));
        getCommand("mbean").setExecutor(new MbeanCommand(this));
        getCommand("system").setExecutor(new SystemCommand(this));
        getCommand("env").setExecutor(new EnvironmentCommand(this));
        getCommand("monitor").setExecutor(new MonitorCommand(this));
        getCommand("graph").setExecutor(new GraphCommand(this));
        getCommand("native").setExecutor(new NativeCommand(this));
        getCommand("vm").setExecutor(new VmCommand(this));
        getCommand("tasks").setExecutor(new TasksCommand(this));
        getCommand("heap").setExecutor(new HeapCommand(this));
        getCommand("lagpage").setExecutor(new PaginationCommand(this));
        getCommand("jfr").setExecutor(new FlightCommand(this));
        getCommand("network").setExecutor(new NetworkCommand(this));

        PluginCommand timing = getCommand("timing");
        try {
            //paper moved to class to package co.aikar.timings
            Class.forName("org.bukkit.command.defaults.TimingsCommand");
            timing.setExecutor(new SpigotTimingsCommand(this));
        } catch (ClassNotFoundException e) {
            timing.setExecutor(new PaperTimingsCommand(this));
        }
    }
}

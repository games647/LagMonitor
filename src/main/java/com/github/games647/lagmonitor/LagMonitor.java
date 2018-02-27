package com.github.games647.lagmonitor;

import com.github.games647.lagmonitor.commands.*;
import com.github.games647.lagmonitor.inject.CommandInjector;
import com.github.games647.lagmonitor.inject.ListenerInjector;
import com.github.games647.lagmonitor.inject.TaskInjector;
import com.github.games647.lagmonitor.listeners.BlockingConnectionSelector;
import com.github.games647.lagmonitor.listeners.GraphListener;
import com.github.games647.lagmonitor.listeners.PaginationListener;
import com.github.games647.lagmonitor.listeners.PlayerPingListener;
import com.github.games647.lagmonitor.listeners.ThreadSafetyListener;
import com.github.games647.lagmonitor.storage.MonitorSaveTask;
import com.github.games647.lagmonitor.storage.NativeSaveTask;
import com.github.games647.lagmonitor.storage.Storage;
import com.github.games647.lagmonitor.storage.TpsSaveTask;
import com.github.games647.lagmonitor.tasks.BlockingIODetectorTask;
import com.github.games647.lagmonitor.tasks.PingHistoryTask;
import com.github.games647.lagmonitor.tasks.TpsHistoryTask;
import com.github.games647.lagmonitor.threading.BlockingActionManager;
import com.github.games647.lagmonitor.threading.BlockingSecurityManager;
import com.github.games647.lagmonitor.traffic.TrafficReader;
import com.google.common.base.StandardSystemProperty;

import java.io.File;
import java.net.ProxySelector;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.hyperic.sigar.Sigar;

public class LagMonitor extends JavaPlugin {

    //the server is pinging the client every 40 Ticks (2 sec) - so check it then
    //https://github.com/bergerkiller/CraftSource/blob/master/net.minecraft.server/PlayerConnection.java#L178
    private static final int PING_INTERVAL = 2 * 20;
    private static final int DETECTION_THRESHOLD = 10;

    private final Map<String, Pagination> paginations = new HashMap<>();

    private BlockingActionManager blockActionManager;
    private TpsHistoryTask tpsHistoryTask;
    private PingHistoryTask pingHistoryTask;
    private TrafficReader trafficReader;
    private Timer blockDetectionTimer;
    private Timer monitorTimer;
    private Storage storage;
    private NativeData nativeData;

    public LagMonitor() {
        // so you can place the library into the plugins folder
        String oldPath = StandardSystemProperty.JAVA_LIBRARY_PATH.value();
        String newPath = oldPath + File.pathSeparator + getDataFolder().getAbsolutePath();
        System.setProperty("java.library.path", newPath);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        blockActionManager = new BlockingActionManager(this);

        if (Files.notExists(getDataFolder().toPath().resolve("default.jfc"))) {
            saveResource("default.jfc", false);
        }

        //register schedule tasks
        tpsHistoryTask = new TpsHistoryTask();
        pingHistoryTask = new PingHistoryTask();
        getServer().getScheduler().runTaskTimer(this, tpsHistoryTask, 20L, 20L);
        getServer().getScheduler().runTaskTimer(this, pingHistoryTask, 20L, PING_INTERVAL);

        if (getConfig().getBoolean("traffic-counter")) {
            try {
                trafficReader = new TrafficReader(this);
            } catch (Exception ex) {
                getLogger().log(Level.SEVERE, "Failed to initialize packet reader", ex);
            }
        }

        //register listeners
        getLogger().info("Register ping listener");

        getServer().getPluginManager().registerEvents(new PlayerPingListener(this), this);
        getServer().getPluginManager().registerEvents(new PaginationListener(this), this);
        //add the player to the list in the case the plugin is loaded at runtime
        Bukkit.getOnlinePlayers().forEach(pingHistoryTask::addPlayer);

        if (getConfig().getBoolean("thread-safety-check")) {
            getServer().getPluginManager().registerEvents(new ThreadSafetyListener(this), this);
        }

        if (getConfig().getBoolean("thread-block-detection")) {
            Bukkit.getScheduler().runTask(this, () -> {
                blockDetectionTimer = new Timer(getName() + "-Thread-Blocking-Detection");
                BlockingIODetectorTask detectorTask = new BlockingIODetectorTask(this, Thread.currentThread());
                blockDetectionTimer.scheduleAtFixedRate(detectorTask, DETECTION_THRESHOLD, DETECTION_THRESHOLD);
            });
        }

        if (getConfig().getBoolean("socket-block-detection")) {
            ProxySelector defaultSelector = ProxySelector.getDefault();
            BlockingConnectionSelector selector = new BlockingConnectionSelector(blockActionManager, defaultSelector);
            Bukkit.getScheduler().runTask(this, () -> ProxySelector.setDefault(selector));
        }

        if (getConfig().getBoolean("native-library")) {
            Sigar sigar = new Sigar();
            nativeData = new NativeData(getLogger(), sigar);
        } else {
            nativeData = new NativeData(getLogger(), null);
        }

        if (getConfig().getBoolean("monitor-database")) {
            try {
                String host = getConfig().getString("host");
                int port = getConfig().getInt("port");
                String database = getConfig().getString("database");

                String username = getConfig().getString("username");
                String password = getConfig().getString("password");
                String tablePrefix = getConfig().getString("tablePrefix");
                Storage localStorage = new Storage(this, host, port, database, username, password, tablePrefix);
                localStorage.createTables();
                this.storage = localStorage;

                getServer().getScheduler().runTaskTimer(this, new TpsSaveTask(this), 20L,
                         getConfig().getInt("tps-save-interval") * 20L);
                //this can run async because it runs independently from the main thread
                getServer().getScheduler().runTaskTimerAsynchronously(this, new MonitorSaveTask(this), 20L,
                         getConfig().getInt("monitor-save-interval") * 20L);
                getServer().getScheduler().runTaskTimerAsynchronously(this, new NativeSaveTask(this), 20L,
                         getConfig().getInt("native-save-interval") * 20L);
            } catch (SQLException sqlEx) {
                getLogger().log(Level.SEVERE, "Failed to setup monitoring database", sqlEx);
            }
        }

        if (getConfig().getBoolean("securityMangerBlockingCheck")) {
            Bukkit.getScheduler().runTask(this, () -> System.setSecurityManager(new BlockingSecurityManager(this)));
        }

        registerCommands();
        getServer().getPluginManager().registerEvents(new GraphListener(), this);
    }

    @Override
    public void onDisable() {
        if (trafficReader != null) {
            trafficReader.close();
            trafficReader = null;
        }

        if (blockDetectionTimer != null) {
            blockDetectionTimer.cancel();
            blockDetectionTimer.purge();
            blockDetectionTimer = null;
        }

        if (monitorTimer != null) {
            monitorTimer.cancel();
            monitorTimer.purge();
            monitorTimer = null;
        }

        //restore the security manager
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager instanceof BlockingSecurityManager) {
            SecurityManager oldSecurityManager = ((BlockingSecurityManager) securityManager).getOldSecurityManager();
            System.setSecurityManager(oldSecurityManager);
        }

        if (nativeData != null) {
            nativeData.close();
        }

        ProxySelector proxySelector = ProxySelector.getDefault();
        if (proxySelector instanceof BlockingConnectionSelector) {
            ProxySelector oldProxySelector = ((BlockingConnectionSelector) proxySelector).getOldProxySelector();
            ProxySelector.setDefault(oldProxySelector);
        }

        for (Plugin plugin : getServer().getPluginManager().getPlugins()) {
            ListenerInjector.uninject(plugin);
            CommandInjector.uninject(plugin);
            TaskInjector.uninject(plugin);
        }
    }

    public Map<String, Pagination> getPaginations() {
        return paginations;
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

    public TpsHistoryTask getTpsHistoryTask() {
        return tpsHistoryTask;
    }

    public PingHistoryTask getPingHistoryTask() {
        return pingHistoryTask;
    }

    public Storage getStorage() {
        return storage;
    }

    public NativeData getNativeData() {
        return nativeData;
    }

    public BlockingActionManager getBlockActionManager() {
        return blockActionManager;
    }

    private void registerCommands() {
        getCommand(getName()).setExecutor(new HelpCommand(this));

        getCommand("ping").setExecutor(new PingCommand(this));
        getCommand("stacktrace").setExecutor(new StackTraceCommand(this));
        getCommand("thread").setExecutor(new ThreadCommand(this));
        getCommand("tpshistory").setExecutor(new TpsHistoryCommand(this));
        getCommand("mbean").setExecutor(new MbeanCommand(this));
        getCommand("system").setExecutor(new SystemCommand(this));
        getCommand("env").setExecutor(new EnvironmentCommand(this));
        getCommand("monitor").setExecutor(new MonitorCommand(this));
        getCommand("timing").setExecutor(new TimingCommand(this));
        getCommand("graph").setExecutor(new GraphCommand(this));
        getCommand("native").setExecutor(new NativeCommand(this));
        getCommand("vm").setExecutor(new VmCommand(this));
        getCommand("tasks").setExecutor(new TasksCommand(this));
        getCommand("paper-timing").setExecutor(new PaperTimingsCommand(this));
        getCommand("heap").setExecutor(new HeapCommand(this));
        getCommand("lagpage").setExecutor(new PaginationCommand(this));
        getCommand("jfr").setExecutor(new FlightRecorderCommand(this));
    }
}

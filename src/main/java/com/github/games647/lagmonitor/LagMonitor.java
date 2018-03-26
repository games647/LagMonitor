package com.github.games647.lagmonitor;

import com.github.games647.lagmonitor.commands.EnvironmentCommand;
import com.github.games647.lagmonitor.commands.GraphCommand;
import com.github.games647.lagmonitor.commands.HelpCommand;
import com.github.games647.lagmonitor.commands.MbeanCommand;
import com.github.games647.lagmonitor.commands.MonitorCommand;
import com.github.games647.lagmonitor.commands.NativeCommand;
import com.github.games647.lagmonitor.commands.PaginationCommand;
import com.github.games647.lagmonitor.commands.StackTraceCommand;
import com.github.games647.lagmonitor.commands.VmCommand;
import com.github.games647.lagmonitor.commands.dump.FlightRecorderCommand;
import com.github.games647.lagmonitor.commands.dump.HeapCommand;
import com.github.games647.lagmonitor.commands.dump.ThreadCommand;
import com.github.games647.lagmonitor.commands.minecraft.PingCommand;
import com.github.games647.lagmonitor.commands.minecraft.SystemCommand;
import com.github.games647.lagmonitor.commands.minecraft.TasksCommand;
import com.github.games647.lagmonitor.commands.minecraft.TpsHistoryCommand;
import com.github.games647.lagmonitor.commands.timings.PaperTimingsCommand;
import com.github.games647.lagmonitor.commands.timings.SpigotTimingsCommand;
import com.github.games647.lagmonitor.inject.CommandInjector;
import com.github.games647.lagmonitor.inject.ListenerInjector;
import com.github.games647.lagmonitor.inject.TaskInjector;
import com.github.games647.lagmonitor.listeners.BlockingConnectionSelector;
import com.github.games647.lagmonitor.listeners.GraphListener;
import com.github.games647.lagmonitor.listeners.PaginationManager;
import com.github.games647.lagmonitor.listeners.ThreadSafetyListener;
import com.github.games647.lagmonitor.storage.MonitorSaveTask;
import com.github.games647.lagmonitor.storage.NativeSaveTask;
import com.github.games647.lagmonitor.storage.Storage;
import com.github.games647.lagmonitor.storage.TpsSaveTask;
import com.github.games647.lagmonitor.tasks.BlockingIODetectorTask;
import com.github.games647.lagmonitor.tasks.PingManager;
import com.github.games647.lagmonitor.tasks.TpsHistoryTask;
import com.github.games647.lagmonitor.threading.BlockingActionManager;
import com.github.games647.lagmonitor.threading.BlockingSecurityManager;
import com.github.games647.lagmonitor.threading.Injectable;
import com.github.games647.lagmonitor.traffic.TrafficReader;

import java.net.ProxySelector;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Timer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import oshi.SystemInfo;

public class LagMonitor extends JavaPlugin {

    //the server is pinging the client every 40 Ticks (2 sec) - so check it then
    //https://github.com/bergerkiller/CraftSource/blob/master/net.minecraft.server/PlayerConnection.java#L178
    private static final int PING_INTERVAL = 2 * 20;
    private static final int DETECTION_THRESHOLD = 10;

    private final NativeData nativeData = new NativeData(getLogger(), new SystemInfo());
    private final PingManager pingManager = new PingManager(this);
    private final BlockingActionManager blockActionManager = new BlockingActionManager(this);
    private final PaginationManager paginationManager = new PaginationManager();
    private final TpsHistoryTask tpsHistoryTask = new TpsHistoryTask();

    private TrafficReader trafficReader;
    private Timer blockDetectionTimer;
    private Timer monitorTimer;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (Files.notExists(getDataFolder().toPath().resolve("default.jfc"))) {
            saveResource("default.jfc", false);
        }

        //register schedule tasks
        getServer().getScheduler().runTaskTimer(this, tpsHistoryTask, 20L, 20L);
        getServer().getScheduler().runTaskTimer(this, pingManager, 20L, PING_INTERVAL);

        if (getConfig().getBoolean("traffic-counter")) {
            try {
                trafficReader = new TrafficReader(this);
            } catch (Exception ex) {
                getLogger().log(Level.SEVERE, "Failed to initialize packet reader", ex);
            }
        }

        //register listeners
        getServer().getPluginManager().registerEvents(pingManager, this);
        getServer().getPluginManager().registerEvents(paginationManager, this);

        //add the player to the list in the case the plugin is loaded at runtime
        Bukkit.getOnlinePlayers().forEach(pingManager::addPlayer);

        if (getConfig().getBoolean("thread-safety-check")) {
            getServer().getPluginManager().registerEvents(new ThreadSafetyListener(blockActionManager), this);
        }

        if (getConfig().getBoolean("thread-block-detection")) {
            Bukkit.getScheduler().runTask(this, () -> {
                blockDetectionTimer = new Timer(getName() + "-Thread-Blocking-Detection");
                BlockingIODetectorTask detectorTask = new BlockingIODetectorTask(this, Thread.currentThread());
                blockDetectionTimer.scheduleAtFixedRate(detectorTask, DETECTION_THRESHOLD, DETECTION_THRESHOLD);
            });
        }

        if (getConfig().getBoolean("socket-block-detection")) {
            Bukkit.getScheduler().runTask(this, () -> new BlockingConnectionSelector(blockActionManager).inject());
        }

        if (getConfig().getBoolean("monitor-database")) {
            setupNativeDatabase();
        }

        if (getConfig().getBoolean("securityMangerBlockingCheck")) {
            Bukkit.getScheduler().runTask(this, () -> new BlockingSecurityManager(blockActionManager).inject());
        }

        registerCommands();
        getServer().getPluginManager().registerEvents(new GraphListener(), this);
    }

    private void setupNativeDatabase() {
        try {
            String host = getConfig().getString("host");
            int port = getConfig().getInt("port");
            String database = getConfig().getString("database");

            String username = getConfig().getString("username");
            String password = getConfig().getString("password");
            String tablePrefix = getConfig().getString("tablePrefix");
            Storage storage = new Storage(this, host, port, database, username, password, tablePrefix);
            storage.createTables();

            getServer().getScheduler().runTaskTimer(this, new TpsSaveTask(this, storage), 20L,
                     getConfig().getInt("tps-save-interval") * 20L);
            //this can run async because it runs independently from the main thread
            getServer().getScheduler().runTaskTimerAsynchronously(this, new MonitorSaveTask(this, storage),
                    20L,getConfig().getInt("monitor-save-interval") * 20L);
            getServer().getScheduler().runTaskTimerAsynchronously(this, new NativeSaveTask(this, storage),
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

        pingManager.clear();

        //restore the security manager
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager instanceof BlockingSecurityManager) {
            ((Injectable) securityManager).restore();
        }

        ProxySelector proxySelector = ProxySelector.getDefault();
        if (proxySelector instanceof BlockingConnectionSelector) {
            ((Injectable) securityManager).restore();
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

    public PaginationManager getPaginationManager() {
        return paginationManager;
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

    public PingManager getPingManager() {
        return pingManager;
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
        getCommand("timing").setExecutor(new SpigotTimingsCommand(this));
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

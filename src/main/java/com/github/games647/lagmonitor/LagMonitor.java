package com.github.games647.lagmonitor;

import com.github.games647.lagmonitor.tasks.TpsHistoryTask;
import com.github.games647.lagmonitor.commands.EnvironmentCommand;
import com.github.games647.lagmonitor.commands.GraphCommand;
import com.github.games647.lagmonitor.commands.MbeanCommand;
import com.github.games647.lagmonitor.commands.MonitorCommand;
import com.github.games647.lagmonitor.commands.PingCommand;
import com.github.games647.lagmonitor.commands.StackTraceCommand;
import com.github.games647.lagmonitor.commands.SystemCommand;
import com.github.games647.lagmonitor.commands.ThreadCommand;
import com.github.games647.lagmonitor.commands.TimingCommand;
import com.github.games647.lagmonitor.commands.TpsHistoryCommand;
import com.github.games647.lagmonitor.listeners.PlayerListener;
import com.github.games647.lagmonitor.listeners.ThreadSafetyListener;
import com.github.games647.lagmonitor.tasks.PingHistoryTask;
import com.github.games647.lagmonitor.traffic.TrafficReader;

import org.bukkit.plugin.java.JavaPlugin;

public class LagMonitor extends JavaPlugin {

    //the server is pinging the client every 40 Ticks - so check it then
    //https://github.com/bergerkiller/CraftSource/blob/master/net.minecraft.server/PlayerConnection.java#L178
    private static final int PING_INTERVAL = 40;

    private TpsHistoryTask tpsHistoryTask;
    private PingHistoryTask pingHistoryTask;
    private TrafficReader trafficReader;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        //register commands
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

        //register schedule tasks
        tpsHistoryTask = new TpsHistoryTask();
        pingHistoryTask = new PingHistoryTask();
        getServer().getScheduler().runTaskTimer(this, tpsHistoryTask, 20L, 20L);
        getServer().getScheduler().runTaskTimer(this, pingHistoryTask, 20L, PING_INTERVAL);

        if (getConfig().getBoolean("traffic-counter")) {
            trafficReader = new TrafficReader(this);
        }

        //register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        if (getConfig().getBoolean("thread-safety-check")) {
            getServer().getPluginManager().registerEvents(new ThreadSafetyListener(this), this);
        }
    }

    @Override
    public void onDisable() {
        if (trafficReader != null) {
            trafficReader.close();
            trafficReader = null;
        }
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
}

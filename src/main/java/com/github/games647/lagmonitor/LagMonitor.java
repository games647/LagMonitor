package com.github.games647.lagmonitor;

import com.github.games647.lagmonitor.commands.MbeanCommand;
import com.github.games647.lagmonitor.commands.PingCommand;
import com.github.games647.lagmonitor.commands.StackCommand;
import com.github.games647.lagmonitor.commands.SystemCommand;
import com.github.games647.lagmonitor.commands.ThreadCommand;
import com.github.games647.lagmonitor.commands.TpsHistoryCommand;

import org.bukkit.plugin.java.JavaPlugin;

public class LagMonitor extends JavaPlugin {

    private TpsHistoryTask tpsHistoryTask;

    @Override
    public void onEnable() {
        //register commands
        getCommand("ping").setExecutor(new PingCommand(this));
        getCommand("stacktrace").setExecutor(new StackCommand(this));
        getCommand("thread").setExecutor(new ThreadCommand(this));
        getCommand("tpshistory").setExecutor(new TpsHistoryCommand(this));
        getCommand("mbean").setExecutor(new MbeanCommand(this));
        getCommand("system").setExecutor(new SystemCommand(this));

        //register schedule tasks
        tpsHistoryTask = new TpsHistoryTask();
        getServer().getScheduler().runTaskTimer(this, tpsHistoryTask, 10 * 20L, 3 * 20L);
    }

    public TpsHistoryTask getTpsHistoryTask() {
        return tpsHistoryTask;
    }
}

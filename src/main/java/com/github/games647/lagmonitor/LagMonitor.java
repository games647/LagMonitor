package com.github.games647.lagmonitor;

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
import org.bukkit.ChatColor;

import org.bukkit.plugin.java.JavaPlugin;

public class LagMonitor extends JavaPlugin {

    private TpsHistoryTask tpsHistoryTask;

    @Override
    public void onEnable() {
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
        getServer().getScheduler().runTaskTimer(this, tpsHistoryTask, 1 * 20L, 1 * 20L);
    }

    public ChatColor getHighlightColor(int percent) {
        ChatColor highlightColor;
        switch (percent) {
            case 0:
            case 1:
                highlightColor = ChatColor.DARK_RED;
                break;
            case 2:
            case 3:
            case 4:
                highlightColor = ChatColor.RED;
                break;
            case 5:
            case 6:
                highlightColor = ChatColor.GOLD;
                break;
            case 7:
                highlightColor = ChatColor.YELLOW;
                break;
            case 8:
            case 9:
                highlightColor = ChatColor.GREEN;
                break;
            case 10:
            default:
                highlightColor = ChatColor.DARK_GREEN;
                break;
        }

        return highlightColor;
    }

    public TpsHistoryTask getTpsHistoryTask() {
        return tpsHistoryTask;
    }
}

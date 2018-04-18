package com.github.games647.lagmonitor.command.timing;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.command.LagCommand;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public abstract class TimingCommand extends LagCommand {

    public TimingCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canExecute(sender, command)) {
            return true;
        }

        if (isTimingsEnabled()) {
            sendError(sender,"The server deactivated timing reports");
            sendError(sender,"Go to paper.yml or spigot.yml and activate timings");
            return true;
        }

        return true;
    }

    protected abstract void sendTimings(CommandSender sender);

    protected abstract boolean isTimingsEnabled();

    protected String highlightPct(float percent, int low, int med, int high) {
        ChatColor prefix = ChatColor.GRAY;
        if (percent > high) {
            prefix = ChatColor.DARK_RED;
        } else if (percent > med) {
            prefix = ChatColor.GOLD;
        } else if (percent > low) {
            prefix = ChatColor.YELLOW;
        }

        return prefix + String.valueOf(percent) + '%' + ChatColor.GRAY;
    }
}

package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.RollingOverHistory;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PingCommand implements CommandExecutor {

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;

    private final LagMonitor plugin;

    public PingCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.isAllowed(sender, command)) {
            sender.sendMessage(org.bukkit.ChatColor.DARK_RED + "Not whitelisted");
            return true;
        }

        if (args.length > 0) {
            displayPingOther(sender, command, args);
        } else if (sender instanceof Player) {
            RollingOverHistory sampleHistory = plugin.getPingHistoryTask().getHistory(sender.getName());
            if (sampleHistory == null) {
                sender.sendMessage(ChatColor.DARK_RED + "Sorry there is currently no data available");
                return true;
            }

            int lastPing = (int) sampleHistory.getLastSample();
            sender.sendMessage(PRIMARY_COLOR + "Your ping is: " + ChatColor.DARK_GREEN + lastPing + "ms");

            float pingAverage = (float) (Math.round(sampleHistory.getAverage() * 100.0) / 100.0);
            sender.sendMessage(PRIMARY_COLOR + "Average: " + ChatColor.DARK_GREEN + pingAverage + "ms");
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "You have to be ingame in order to see your own ping");
        }

        return true;
    }

    private void displayPingOther(CommandSender sender, Command command, String[] args) {
        if (sender.hasPermission(command.getPermission() + ".other")) {
            String playerName = args[0];
            RollingOverHistory sampleHistory = plugin.getPingHistoryTask().getHistory(playerName);
            if (sampleHistory == null) {
                sender.sendMessage(ChatColor.DARK_RED + "No data for that player " + playerName);
                return;
            }

            int lastPing = (int) sampleHistory.getLastSample();

            sender.sendMessage(ChatColor.WHITE + playerName + PRIMARY_COLOR + "'s ping is: "
                    + ChatColor.DARK_GREEN + lastPing + "ms");

            float pingAverage = (float) (Math.round(sampleHistory.getAverage() * 100.0) / 100.0);
            sender.sendMessage(PRIMARY_COLOR + "Average: " + ChatColor.DARK_GREEN + pingAverage + "ms");
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "You don't have enough permission");
        }
    }
}

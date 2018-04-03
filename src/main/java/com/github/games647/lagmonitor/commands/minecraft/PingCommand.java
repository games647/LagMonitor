package com.github.games647.lagmonitor.commands.minecraft;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.commands.LagCommand;
import com.github.games647.lagmonitor.utils.LagUtils;
import com.github.games647.lagmonitor.utils.RollingOverHistory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PingCommand extends LagCommand {

    public PingCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canExecute(sender, command)) {
            return true;
        }

        if (args.length > 0) {
            displayPingOther(sender, command, args);
        } else if (sender instanceof Player) {
            displayPingSelf(sender);
        } else {
            sendError(sender, "You have to be in game in order to see your own ping");
        }

        return true;
    }

    private void displayPingSelf(CommandSender sender) {
        RollingOverHistory sampleHistory = plugin.getPingManager().getHistory(sender.getName());
        if (sampleHistory == null) {
            sendError(sender, "Sorry there is currently no data available");
            return;
        }

        int lastPing = (int) sampleHistory.getLastSample();
        sender.sendMessage(PRIMARY_COLOR + "Your ping is: " + ChatColor.DARK_GREEN + lastPing + "ms");

        float pingAverage = (float) (Math.round(sampleHistory.getAverage() * 100.0) / 100.0);
        sender.sendMessage(PRIMARY_COLOR + "Average: " + ChatColor.DARK_GREEN + pingAverage + "ms");
    }

    private void displayPingOther(CommandSender sender, Command command, String[] args) {
        if (sender.hasPermission(command.getPermission() + ".other")) {
            String playerName = args[0];
            RollingOverHistory sampleHistory = plugin.getPingManager().getHistory(playerName);
            if (sampleHistory == null || !canSee(sender, playerName)) {
                sendError(sender, "No data for that player " + playerName);
                return;
            }

            int lastPing = (int) sampleHistory.getLastSample();

            sender.sendMessage(ChatColor.WHITE + playerName + PRIMARY_COLOR + "'s ping is: "
                    + ChatColor.DARK_GREEN + lastPing + "ms");

            float pingAverage = LagUtils.round(sampleHistory.getAverage());
            sender.sendMessage(PRIMARY_COLOR + "Average: " + ChatColor.DARK_GREEN + pingAverage + "ms");
        } else {
            sendError(sender, "You don't have enough permission");
        }
    }

    private boolean canSee(CommandSender sender, String playerName) {
        if (sender instanceof Player) {
            return ((Player) sender).canSee(Bukkit.getPlayerExact(playerName));
        }

        return true;
    }
}

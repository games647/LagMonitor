package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public abstract class LagCommand implements CommandExecutor {

    protected final LagMonitor plugin;

    public LagCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    public boolean isAllowed(CommandSender sender, Command cmd) {
        if (!(sender instanceof Player)) {
            return true;
        }

        FileConfiguration config = plugin.getConfig();
        List<String> commandWhitelist = config.getStringList("whitelist-" + cmd.getName());
        if (commandWhitelist != null && !commandWhitelist.isEmpty()) {
            return commandWhitelist.contains(sender.getName());
        }

        for (String alias : cmd.getAliases()) {
            List<String> aliasWhitelist = config.getStringList("whitelist-" + alias);
            if (aliasWhitelist != null && !aliasWhitelist.isEmpty()) {
                return aliasWhitelist.contains(sender.getName());
            }
        }

        //whitelist doesn't exist
        return true;
    }
}

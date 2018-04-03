package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public abstract class LagCommand implements CommandExecutor {

    protected static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;
    protected static final ChatColor SECONDARY_COLOR = ChatColor.GRAY;

    protected static final String NATIVE_NOT_FOUND = "Native library not found. Please download it to see this data";

    protected final LagMonitor plugin;

    public LagCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    private boolean isCommandWhitelisted(Command cmd, CommandSender sender) {
        if (!(sender instanceof Player)) {
            return true;
        }

        FileConfiguration config = plugin.getConfig();

        Collection<String> aliases = new ArrayList<>(cmd.getAliases());
        aliases.add(cmd.getName());
        for (String alias : aliases) {
            List<String> aliasWhitelist = config.getStringList("whitelist-" + alias);
            if (aliasWhitelist != null && !aliasWhitelist.isEmpty()) {
                return aliasWhitelist.contains(sender.getName());
            }
        }

        //whitelist doesn't exist
        return true;
    }

    public boolean canExecute(CommandSender sender, Command cmd) {
        if (!isCommandWhitelisted(cmd, sender)) {
            sendError(sender, "Command not whitelisted for you!");
            return false;
        }

        return true;
    }

    protected void sendMessage(CommandSender sender, String title, String value) {
        sender.sendMessage(PRIMARY_COLOR + title + ": " + SECONDARY_COLOR + value);
    }

    protected void sendError(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.DARK_RED + msg);
    }

    public static void send(CommandSender sender, BaseComponent... components) {
        //CommandSender#sendMessage(BaseComponent[]) was introduced after 1.8. This is a backwards compatible solution
        if (sender instanceof Player) {
            sender.spigot().sendMessage(components);
        } else {
            sender.sendMessage(TextComponent.toLegacyText(components));
        }
    }
}

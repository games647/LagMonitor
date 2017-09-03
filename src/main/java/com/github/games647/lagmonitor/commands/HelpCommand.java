package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.util.Map;
import java.util.Map.Entry;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

import org.apache.commons.lang.WordUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

public class HelpCommand implements CommandExecutor {

    private static final int HOVER_MAX_LENGTH = 40;

    private final LagMonitor plugin;

    public HelpCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Map<String, Map<String, Object>> commands = plugin.getDescription().getCommands();

        sender.sendMessage(ChatColor.AQUA + plugin.getName() + "-Help");

        int maxWidth = ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH;
        if (!(sender instanceof Player)) {
            maxWidth = Integer.MAX_VALUE;
        }

        for (Entry<String, Map<String, Object>> entry : commands.entrySet()) {
            String commandKey = entry.getKey();
            Map<String, Object> value = entry.getValue();

            String description = ' ' + value.getOrDefault("description", "No description").toString();
            String usage = ((String) value.getOrDefault("usage", '/' + commandKey)).replace("<command>", commandKey);

            TextComponent usageComponent = new TextComponent(usage);
            usageComponent.setColor(ChatColor.DARK_AQUA);
            
            TextComponent descriptionComponent = new TextComponent(description);
            descriptionComponent.setColor(ChatColor.GOLD);
            int totalLen = usage.length() + description.length();
            if (totalLen > maxWidth) {
                int newDescLeng = maxWidth - usage.length() - 3 - 1;
                if (newDescLeng < 0) {
                    newDescLeng = 0;
                }

                String shortDesc = description.substring(0, newDescLeng) + "...";
                descriptionComponent.setText(shortDesc);

                ComponentBuilder hoverBuilder = new ComponentBuilder("");

                String separated = WordUtils.wrap(description, HOVER_MAX_LENGTH, "\n", false);
                for (String line : separated.split("\n")) {
                    hoverBuilder.append(line + '\n');
                    hoverBuilder.color(ChatColor.GOLD);
                }

                descriptionComponent
                        .setHoverEvent(new HoverEvent(Action.SHOW_TEXT, hoverBuilder.create()));
            } else {
                descriptionComponent.setText(description);
            }

            usageComponent.addExtra(descriptionComponent);
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.spigot().sendMessage(usageComponent);
            } else {
                sender.sendMessage(usageComponent.toLegacyText());
            }
        }

        return true;
    }
}

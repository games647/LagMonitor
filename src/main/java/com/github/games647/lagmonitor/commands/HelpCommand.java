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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

public class HelpCommand extends LagCommand {

    private static final int HOVER_MAX_LENGTH = 40;

    public HelpCommand(LagMonitor plugin) {
        super(plugin);
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

            TextComponent component = createCommandHelp(usage, description, maxWidth);
            LagCommand.send(sender, component);
        }

        return true;
    }

    private TextComponent createCommandHelp(String usage, String description, int maxWidth) {
        TextComponent usageComponent = new TextComponent(usage);
        usageComponent.setColor(ChatColor.DARK_AQUA);

        TextComponent descriptionComponent = new TextComponent(description);
        descriptionComponent.setColor(ChatColor.GOLD);
        int totalLen = usage.length() + description.length();
        if (totalLen > maxWidth) {
            int newDescLength = maxWidth - usage.length() - 3 - 1;
            if (newDescLength < 0) {
                newDescLength = 0;
            }

            String shortDesc = description.substring(0, newDescLength) + "...";
            descriptionComponent.setText(shortDesc);

            ComponentBuilder hoverBuilder = new ComponentBuilder("");

            String separated = WordUtils.wrap(description, HOVER_MAX_LENGTH, "\n", false);
            for (String line : separated.split("\n")) {
                hoverBuilder.append(line + '\n');
                hoverBuilder.color(ChatColor.GOLD);
            }

            descriptionComponent.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, hoverBuilder.create()));
        } else {
            descriptionComponent.setText(description);
        }

        usageComponent.addExtra(descriptionComponent);
        return usageComponent;
    }
}

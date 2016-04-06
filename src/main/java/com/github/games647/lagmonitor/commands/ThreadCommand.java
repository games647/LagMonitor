package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.Pagination;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ThreadCommand implements CommandExecutor {

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;
    private static final ChatColor SECONDARY_COLOR = ChatColor.GRAY;

    private final LagMonitor plugin;

    public ThreadCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<BaseComponent[]> lines = Lists.newArrayList();

        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        for (Thread thread : allStackTraces.keySet()) {
            if (thread.getContextClassLoader() == null) {
                //ignore java system threads like reference handler
                continue;
            }

            BaseComponent[] components = new ComponentBuilder(thread.getName())
                    .color(PRIMARY_COLOR)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND
                            , "/stacktrace " + thread.getName()))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT
                            , new ComponentBuilder("Show the stacktrace").create()))
                    .append("-" + thread.getId() + " State: ")
                    .color(ChatColor.GOLD)
                    .append(thread.getState().toString())
                    .color(SECONDARY_COLOR)
                    .create();
            lines.add(components);
        }

        Pagination pagination = new Pagination("Threads", lines);
        pagination.send(sender);
        plugin.getPaginations().put(sender, pagination);
        return true;
    }
}

package com.github.games647.lagmonitor.commands.dump;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.Pages;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.management.InstanceNotFoundException;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ThreadCommand extends DumpCommand {

    private static final String DUMP_COMMAND = "threadPrint";

    public ThreadCommand(LagMonitor plugin) {
        super(plugin, "thread", "tdump");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canExecute(sender, command)) {
            return true;
        }

        if (args.length > 0) {
            String subCommand = args[0];
            if ("dump".equalsIgnoreCase(subCommand)) {
                onDump(sender);
            } else {
                sender.sendMessage(label);
            }

            return true;
        }

        List<BaseComponent[]> lines = new ArrayList<>();

        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        for (Thread thread : allStackTraces.keySet()) {
            if (thread.getContextClassLoader() == null) {
                //ignore java system threads like reference handler
                continue;
            }

            BaseComponent[] components = new ComponentBuilder("ID-" + thread.getId() + ": ")
                    .color(PRIMARY_COLOR.asBungee())
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND
                            , "/stacktrace " + thread.getName()))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT
                            , new ComponentBuilder("Show the stacktrace").create()))
                    .append(thread.getName() + ' ')
                    .color(ChatColor.GOLD)
                    .append(thread.getState().toString())
                    .color(SECONDARY_COLOR.asBungee())
                    .create();
            lines.add(components);
        }

        Pages pagination = new Pages("Threads", lines);
        pagination.send(sender);
        plugin.getPageManager().setPagination(sender.getName(), pagination);
        return true;
    }

    private void onDump(CommandSender sender) {
        try {
            String result = invokeDiagnosticCommand(DUMP_COMMAND, ArrayUtils.EMPTY_STRING_ARRAY);

            Path dumpFile = getNewDumpFile();
            Files.write(dumpFile, Collections.singletonList(result));

            sender.sendMessage(ChatColor.GRAY + "Dump created: " + dumpFile.getFileName());
            sender.sendMessage(ChatColor.GRAY + "You can analyse it using VisualVM");
        } catch (InstanceNotFoundException instanceNotFoundException) {
            sendError(sender, NOT_ORACLE_MSG);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
            sendError(sender, "An exception occurred. Please check the server log");
        }
    }
}

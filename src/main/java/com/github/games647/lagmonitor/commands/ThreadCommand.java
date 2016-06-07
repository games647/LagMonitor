package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.Pagination;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.management.InstanceNotFoundException;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang.ArrayUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ThreadCommand implements CommandExecutor {

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;
    private static final ChatColor SECONDARY_COLOR = ChatColor.GRAY;

    //https://docs.oracle.com/javase/8/docs/jre/api/management/extension/com/sun/management/DiagnosticCommandMBean.html
    private static final String DIAGNOSTIC_COMMAND = "com.sun.management:type=DiagnosticCommand";
    private static final String DUMP_COMMAND = "threadPrint";
    private static final String DUMP_FILE_NAME = "thread";
    private static final String DUMP_FILE_ENDING = ".tdump";

    private final LagMonitor plugin;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    public ThreadCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            String subCommand = args[0];
            if ("dump".equalsIgnoreCase(subCommand)) {
                onDump(sender);
            } else {
                sender.sendMessage(label);
            }

            return true;
        }

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

    private void onDump(CommandSender sender) {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName diagnosticBean = ObjectName.getInstance(DIAGNOSTIC_COMMAND);

            String timeSuffix = '-' + dateFormat.format(new Date());
            File dumpFile = new File(plugin.getDataFolder(), DUMP_FILE_NAME + timeSuffix + DUMP_FILE_ENDING);
            //it needs to be with a system dependent path seperator
            String result = (String) mBeanServer.invoke(diagnosticBean, DUMP_COMMAND
                    , new Object[]{ArrayUtils.EMPTY_STRING_ARRAY}, new String[]{String[].class.getName()});

            Files.write(result, dumpFile, Charsets.UTF_8);

            sender.sendMessage(ChatColor.GRAY + "Dump created: " + dumpFile.getCanonicalPath());
            sender.sendMessage(ChatColor.GRAY + "You can analyse it using VisualVM");
        } catch (InstanceNotFoundException instanceNotFoundException) {
            plugin.getLogger().log(Level.SEVERE, "You are not using Oracle JVM. OpenJDK hasn't implemented it yet"
                    , instanceNotFoundException);
            sender.sendMessage(ChatColor.DARK_RED + "You are not using Oracle JVM. OpenJDK hasn't implemented it yet");
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
            sender.sendMessage(ChatColor.DARK_RED + "An exception occurred. Please check the server log");
        }
    }
}

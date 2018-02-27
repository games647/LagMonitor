package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.Pagination;

import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class HeapCommand extends DumpCommand {

    //https://docs.oracle.com/javase/8/docs/jre/api/management/extension/com/sun/management/DiagnosticCommandMBean.html
    private static final String DIAGNOSTIC_COMMAND = "com.sun.management:type=DiagnosticCommand";
    private static final String HEAP_COMMAND = "gcClassHistogram";

    //can be useful for dumping heaps in binary format
    //https://docs.oracle.com/javase/8/docs/jre/api/management/extension/com/sun/management/HotSpotDiagnosticMXBean.html
    private static final String HOTSPOT_DIAGNOSTIC = "com.sun.management:type=HotSpotDiagnostic";
    private static final String DUMP_COMMAND = "dumpHeap";
    private static final boolean DUMP_DEAD_OBJECTS = false;

    public HeapCommand(LagMonitor plugin) {
        super(plugin, "heap", "hprof");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, command)) {
            sender.sendMessage(org.bukkit.ChatColor.DARK_RED + "Not whitelisted");
            return true;
        }

        if (args.length > 0) {
            String subCommand = args[0];
            if ("dump".equalsIgnoreCase(subCommand)) {
                onDump(sender);
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "Unknown subcommand");
            }

            return true;
        }

        List<BaseComponent[]> paginatedLines = new ArrayList<>();
        try {
            ObjectName diagnosticObjectName = ObjectName.getInstance(DIAGNOSTIC_COMMAND);

            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            String reply = (String) mBeanServer.invoke(diagnosticObjectName, HEAP_COMMAND
                    , new Object[]{ArrayUtils.EMPTY_STRING_ARRAY}, new String[]{String[].class.getName()});
            String[] lines = reply.split("\n");
            for (String line : lines) {
                paginatedLines.add(new ComponentBuilder(line).create());
            }

            Pagination pagination = new Pagination("Heap", paginatedLines);
            pagination.send(sender);
            plugin.getPaginations().put(sender.getName(), pagination);
        } catch (InstanceNotFoundException instanceNotFoundException) {
            plugin.getLogger().log(Level.SEVERE, "You are not using Oracle JVM. OpenJDK hasn't implemented it yet"
                    , instanceNotFoundException);
            sender.sendMessage(ChatColor.DARK_RED + "You are not using Oracle JVM. OpenJDK hasn't implemented it yet");
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
            sender.sendMessage(ChatColor.DARK_RED + "An exception occurred. Please check the server log");
        }

        return true;
    }

    private void onDump(CommandSender sender) {
        try {
            Path dumpFile = getNewDumpFile();
            invokeBeanCommand(HOTSPOT_DIAGNOSTIC, DUMP_COMMAND
                    , new Object[]{dumpFile.toAbsolutePath().toString(), DUMP_DEAD_OBJECTS}
                    , new String[]{String.class.getName(), Boolean.TYPE.getName()});

            sender.sendMessage(ChatColor.GRAY + "Dump created: " + dumpFile.getFileName());
            sender.sendMessage(ChatColor.GRAY + "You can analyse it using VisualVM");
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
            sender.sendMessage(ChatColor.DARK_RED + "An exception occurred. Please check the server log");
        }
    }
}

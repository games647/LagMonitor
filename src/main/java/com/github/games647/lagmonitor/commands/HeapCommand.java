package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.Pagination;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class HeapCommand implements CommandExecutor {

    //https://docs.oracle.com/javase/8/docs/jre/api/management/extension/com/sun/management/DiagnosticCommandMBean.html
    private static final String DIAGNOSTIC_COMMAND = "com.sun.management:type=DiagnosticCommand";
    private static final String HEAP_COMMAND = "gcClassHistogram";

    //can be useful for dumping heaps in binary format
    //https://docs.oracle.com/javase/8/docs/jre/api/management/extension/com/sun/management/HotSpotDiagnosticMXBean.html
    private static final String HOTSPOT_DIAGNOSTIC = "com.sun.management:type=HotSpotDiagnostic";
    private static final String DUMP_COMMAND = "dumpHeap";
    private static final String DUMP_FILE_NAME = "heap";
    private static final String DUMP_FILE_ENDING = ".hprof";
    private static final boolean DUMP_DEAD_OBJECTS = false;

    private final LagMonitor plugin;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    public HeapCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.isAllowed(sender, command)) {
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

        List<BaseComponent[]> paginatedLines = Lists.newArrayList();
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
            plugin.getPaginations().put(sender, pagination);
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
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName hotspotBean = ObjectName.getInstance(HOTSPOT_DIAGNOSTIC);

            String timeSuffix = '-' + dateFormat.format(new Date());
            Path dumpFile = plugin.getDataFolder().toPath().resolve(DUMP_FILE_NAME + timeSuffix + DUMP_FILE_ENDING);
            //it needs to be with a system dependent path seperator
            mBeanServer.invoke(hotspotBean, DUMP_COMMAND
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

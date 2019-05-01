package com.github.games647.lagmonitor.command.dump;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.Pages;
import com.sun.management.HotSpotDiagnosticMXBean;

import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.management.InstanceNotFoundException;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class HeapCommand extends DumpCommand {

    private static final String HEAP_COMMAND = "gcClassHistogram";
    private static final boolean DUMP_DEAD_OBJECTS = false;

    private static final String[] EMPTY_STRING = {};

    public HeapCommand(LagMonitor plugin) {
        super(plugin, "heap", "hprof");
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
                sendError(sender, "Unknown subcommand");
            }

            return true;
        }

        List<BaseComponent[]> paginatedLines = new ArrayList<>();
        try {
            String reply = invokeDiagnosticCommand(HEAP_COMMAND, EMPTY_STRING);
            for (String line : reply.split("\n")) {
                paginatedLines.add(new ComponentBuilder(line).create());
            }

            Pages pagination = new Pages("Heap", paginatedLines);
            pagination.send(sender);
            plugin.getPageManager().setPagination(sender.getName(), pagination);
        } catch (InstanceNotFoundException instanceNotFoundException) {
            sendError(sender, NOT_ORACLE_MSG);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
            sendError(sender, "An exception occurred. Please check the server log");
        }

        return true;
    }

    private void onDump(CommandSender sender) {
        try {
            //test if this class is available
            Class.forName("com.sun.management.HotSpotDiagnosticMXBean");

            //can be useful for dumping heaps in binary format
            HotSpotDiagnosticMXBean hostSpot = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);

            Path dumpFile = getNewDumpFile();
            hostSpot.dumpHeap(dumpFile.toAbsolutePath().toString(), DUMP_DEAD_OBJECTS);

            sender.sendMessage(ChatColor.GRAY + "Dump created: " + dumpFile.getFileName());
            sender.sendMessage(ChatColor.GRAY + "You can analyse it using VisualVM");
        } catch (ClassNotFoundException notFoundEx) {
            sendError(sender, NOT_ORACLE_MSG);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
            sendError(sender, "An exception occurred. Please check the server log");
        }
    }
}

package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FlightRecorderCommand implements CommandExecutor {

    private static final String DIAGNOSTIC_COMMAND = "com.sun.management:type=DiagnosticCommand";
    private static final String START_COMMAND = "jfrStart";
    private static final String STOP_COMMAND = "jfrStop";
    private static final String DUMP_COMMAND = "jfrDump";

    private static final String UNLOCK_COMMERCIAL_COMMAND = "vmCheckCommercialFeatures";

    private static final String SETTINGS_FILE = "default.jfc";

    private static final String DUMP_FILE_NAME = "flight_recorder";
    private static final String DUMP_FILE_ENDING = ".jfr";

    private final LagMonitor plugin;
    private final String settingsPath;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private final String recordingName;

    public FlightRecorderCommand(LagMonitor plugin) {
        this.plugin = plugin;
        this.recordingName = plugin.getName() + "-Record";
        this.settingsPath = new File(plugin.getDataFolder(), SETTINGS_FILE).getAbsolutePath();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            String subCommand = args[0];
            if ("start".equalsIgnoreCase(subCommand)) {
                onStartCommand(sender);
            } else if ("stop".equalsIgnoreCase(subCommand)) {
                onStopCommand(sender);
            } else if ("dump".equalsIgnoreCase(subCommand)) {
                onDumpCommand(sender);
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "Unknown subcommand");
            }
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Not enought arguments");
        }

        return true;
    }

    private void onStartCommand(CommandSender sender) {
        try {
            ObjectName diagnosticObjectName = ObjectName.getInstance(DIAGNOSTIC_COMMAND);

            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            String reply = (String) mBeanServer.invoke(diagnosticObjectName, START_COMMAND
                    , new Object[]{new String[]{"settings=" + settingsPath, "name=" + recordingName}}
                    , new String[]{String[].class.getName()});
            sender.sendMessage(reply);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
            sender.sendMessage(ChatColor.DARK_RED + "An exception occurred. Please check the server log");
        }
    }

    private void onStopCommand(CommandSender sender) {
        try {
            ObjectName diagnosticObjectName = ObjectName.getInstance(DIAGNOSTIC_COMMAND);

            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            String reply = (String) mBeanServer.invoke(diagnosticObjectName, STOP_COMMAND
                    , new Object[]{new String[]{"name=" + recordingName}}
                    , new String[]{String[].class.getName()});
            sender.sendMessage(reply);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
            sender.sendMessage(ChatColor.DARK_RED + "An exception occurred. Please check the server log");
        }
    }

    private void onDumpCommand(CommandSender sender) {
        try {
            ObjectName diagnosticObjectName = ObjectName.getInstance(DIAGNOSTIC_COMMAND);

            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

            String timeSuffix = '-' + dateFormat.format(new Date());
            File dumpFile = new File(plugin.getDataFolder(), DUMP_FILE_NAME + timeSuffix + DUMP_FILE_ENDING);
            String reply = (String) mBeanServer.invoke(diagnosticObjectName, DUMP_COMMAND
                    , new Object[]{new String[]{"filename=" + dumpFile.getAbsolutePath()
                            , "name=" + recordingName, "compress=true"}}
                    , new String[]{String[].class.getName()});
            sender.sendMessage(reply);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
            sender.sendMessage(ChatColor.DARK_RED + "An exception occurred. Please check the server log");
        }
    }
}

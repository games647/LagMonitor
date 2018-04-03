package com.github.games647.lagmonitor.commands.dump;

import com.github.games647.lagmonitor.LagMonitor;

import java.nio.file.Path;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class FlightRecorderCommand extends DumpCommand {

    private static final String BEAN_NAME = "com.sun.management:type=DiagnosticCommand";
    private static final String START_COMMAND = "jfrStart";
    private static final String STOP_COMMAND = "jfrStop";
    private static final String DUMP_COMMAND = "jfrDump";

    private static final String SETTINGS_FILE = "default.jfc";

    private final String settingsPath;
    private final String recordingName;

    public FlightRecorderCommand(LagMonitor plugin) {
        super(plugin, "flight_recorder", "jfr");

        this.recordingName = plugin.getName() + "-Record";
        this.settingsPath = plugin.getDataFolder().toPath().resolve(SETTINGS_FILE).toAbsolutePath().toString();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canExecute(sender, command)) {
            return true;
        }

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
            sender.sendMessage(ChatColor.DARK_RED + "Not enough arguments");
        }

        return true;
    }

    private void onStartCommand(CommandSender sender) {
        try {
            String reply = (String) invokeBeanCommand(BEAN_NAME, START_COMMAND
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
            String reply = (String) invokeBeanCommand(BEAN_NAME, STOP_COMMAND
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
            Path dumpFile = getNewDumpFile();
            String reply = (String) invokeBeanCommand(BEAN_NAME, DUMP_COMMAND
                    , new Object[]{new String[]{"filename=" + dumpFile.toAbsolutePath()
                            , "name=" + recordingName, "compress=true"}}
                    , new String[]{String[].class.getName()});

            sender.sendMessage(reply);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
            sender.sendMessage(ChatColor.DARK_RED + "An exception occurred. Please check the server log");
        }
    }
}

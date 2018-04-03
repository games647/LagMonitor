package com.github.games647.lagmonitor.commands.dump;

import com.github.games647.lagmonitor.LagMonitor;

import java.nio.file.Path;
import java.util.logging.Level;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class FlightRecorderCommand extends DumpCommand {

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

        try {
            if (args.length > 0) {
                String subCommand = args[0];
                if ("start".equalsIgnoreCase(subCommand)) {
                    onStartCommand(sender);
                } else if ("stop".equalsIgnoreCase(subCommand)) {
                    onStopCommand(sender);
                } else if ("dump".equalsIgnoreCase(subCommand)) {
                    onDumpCommand(sender);
                } else {
                    sendError(sender, "Unknown subcommand");
                }
            } else {
                sendError(sender, "Not enough arguments");
            }
        } catch (InstanceNotFoundException notFoundEx) {
            sendError(sender, NOT_ORACLE_MSG);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
            sendError(sender, "An exception occurred. Please check the server log");
        }

        return true;
    }

    private void onStartCommand(CommandSender sender)
            throws MalformedObjectNameException, ReflectionException, MBeanException, InstanceNotFoundException {
        String reply = invokeDiagnosticCommand(START_COMMAND, "settings=" + settingsPath, "name=" + recordingName);
        sender.sendMessage(reply);
    }

    private void onStopCommand(CommandSender sender)
            throws MalformedObjectNameException, ReflectionException, MBeanException, InstanceNotFoundException {
        String reply = invokeDiagnosticCommand(STOP_COMMAND, "name=" + recordingName);
        sender.sendMessage(reply);
    }

    private void onDumpCommand(CommandSender sender)
            throws MalformedObjectNameException, ReflectionException, MBeanException, InstanceNotFoundException {
        Path dumpFile = getNewDumpFile();
        String reply = invokeDiagnosticCommand(DUMP_COMMAND
                , "filename=" + dumpFile.toAbsolutePath(), "name=" + recordingName, "compress=true");

        sender.sendMessage(reply);
    }
}

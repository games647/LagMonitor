package com.github.games647.lagmonitor.command.dump;

import com.github.games647.lagmonitor.LagMonitor;

import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class FlightCommand extends DumpCommand {

    private static final String START_COMMAND = "jfrStart";
    private static final String STOP_COMMAND = "jfrStop";
    private static final String DUMP_COMMAND = "jfrDump";

    private static final String SETTINGS_FILE = "default.jfc";

    private final String settingsPath;
    private final String recordingName;

    private final boolean isSupported;

    public FlightCommand(LagMonitor plugin) {
        super(plugin, "flight_recorder", "jfr");

        this.recordingName = plugin.getName() + "-Record";
        this.settingsPath = plugin.getDataFolder().toPath().resolve(SETTINGS_FILE).toAbsolutePath().toString();

        isSupported = areFlightMethodsAvailable();
    }

    private boolean areFlightMethodsAvailable() {
        MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName = ObjectName.getInstance(DIAGNOSTIC_BEAN);
            MBeanInfo beanInfo = beanServer.getMBeanInfo(objectName);
            return Arrays.stream(beanInfo.getOperations())
                    .map(MBeanFeatureInfo::getName)
                    .anyMatch(op -> op.contains("jfr"));
        } catch (JMException instanceNotFoundEx) {
            return false;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canExecute(sender, command)) {
            return true;
        }

        if (!isSupported) {
            sendError(sender, NOT_ORACLE_MSG);
            return true;
        }

        try {
            if (args.length > 0) {
                String subCommand = args[0].toLowerCase();
                switch (subCommand) {
                    case "start":
                        onStartCommand(sender);
                        break;
                    case "stop":
                        onStopCommand(sender);
                        break;
                    case "dump":
                        onDumpCommand(sender);
                        break;
                    default:
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

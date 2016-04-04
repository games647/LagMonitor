package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.lang.management.ManagementFactory;
import java.util.logging.Level;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.commons.lang.ArrayUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HeapCommand implements CommandExecutor {

    //https://docs.oracle.com/javase/8/docs/jre/api/management/extension/com/sun/management/DiagnosticCommandMBean.html
    private static final String DIAGNOTISC_COMMAND = "com.sun.management:type=DiagnosticCommand";

    private static final String HEAP_COMMAND = "gcClassHistogram";

    //can be useful for dumping heaps
    //https://docs.oracle.com/javase/8/docs/jre/api/management/extension/com/sun/management/HotSpotDiagnosticMXBean.html
    private static final String HOTSPOT_DIAGNOSTIC = "com.sun.management:type=HotSpotDiagnostic";

    private final LagMonitor plugin;

    public HeapCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

        try {
            ObjectName diagnosticObjectName = ObjectName.getInstance(DIAGNOTISC_COMMAND);

            String reply = (String) mBeanServer.invoke(diagnosticObjectName, HEAP_COMMAND
                    , new Object[] {ArrayUtils.EMPTY_STRING_ARRAY}, new String[] {String[].class.getName()});
            System.out.println(reply);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        }

        return true;
    }
}

package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.google.common.base.StandardSystemProperty;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class VmCommand extends LagCommand {

    public VmCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, command)) {
            sendError(sender, "Not whitelisted");
            return true;
        }

        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        //java info
        String javaVersion = StandardSystemProperty.JAVA_VERSION.value();
        String vendorVersion = System.getProperty("java.vendor.version", "n/a");
        sendMessage(sender, "Java Version", javaVersion);
        sendMessage(sender, "Java release date", System.getProperty("java.version.date", "n/a"));
        sendMessage(sender, "Java VM", runtimeBean.getVmName() + ' ' + runtimeBean.getVmVersion());
        sendMessage(sender, "Java Vendor", runtimeBean.getVmVendor() + ' ' + vendorVersion);

        sendMessage(sender, "Class version", System.getProperty("java.class.version"));
        sendMessage(sender, "Java lib", System.getProperty("sun.boot.library.path", "Unknown"));
        sendMessage(sender, "Java home", StandardSystemProperty.JAVA_HOME.value());
        sendMessage(sender, "Temp path", System.getProperty("java.io.tmpdir", "Unknown"));

        //vm specification
        sendMessage(sender, "Spec name", runtimeBean.getSpecName());
        sendMessage(sender, "Spec vendor", runtimeBean.getSpecVendor());
        sendMessage(sender, "Spec version", runtimeBean.getSpecVersion());

        CompilationMXBean compileBean = ManagementFactory.getCompilationMXBean();
        sendMessage(sender, "Compiler name", compileBean.getName());

        //class loading
        ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
        sendMessage(sender, "Loaded classes", String.valueOf(classBean.getLoadedClassCount()));
        sendMessage(sender, "Total loaded", String.valueOf(classBean.getTotalLoadedClassCount()));
        sendMessage(sender, "Unloaded classes", String.valueOf(classBean.getUnloadedClassCount()));

        //garbage collector
        List<GarbageCollectorMXBean> gcBean = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean collector : gcBean) {
            sendMessage(sender, "Garbage Collector", collector.getName());
            sendMessage(sender, "    Time", String.valueOf(collector.getCollectionTime()));
            sendMessage(sender, "    Count", String.valueOf(collector.getCollectionCount()));
        }

        return true;
    }
}

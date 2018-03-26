package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.google.common.base.StandardSystemProperty;

import java.lang.management.ClassLoadingMXBean;
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
            sender.sendMessage(org.bukkit.ChatColor.DARK_RED + "Not whitelisted");
            return true;
        }

        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        //java info
        String javaVersion = StandardSystemProperty.JAVA_VERSION.value();
        sender.sendMessage(PRIMARY_COLOR + "Java Version: " + SECONDARY_COLOR + javaVersion);
        sender.sendMessage(PRIMARY_COLOR + "Java VM: " + SECONDARY_COLOR + runtimeBean.getVmName());
        sender.sendMessage(PRIMARY_COLOR + "Java Vendor: " + SECONDARY_COLOR
                + runtimeBean.getVmVendor() + ' ' + runtimeBean.getVmVersion());

        //vm specification
        sender.sendMessage(PRIMARY_COLOR + "Spec name: " + SECONDARY_COLOR + runtimeBean.getSpecName());
        sender.sendMessage(PRIMARY_COLOR + "Spec Vendor: " + SECONDARY_COLOR + runtimeBean.getSpecVendor());
        sender.sendMessage(PRIMARY_COLOR + "Spec Version: " + SECONDARY_COLOR + runtimeBean.getSpecVersion());

        //class loading
        ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
        sender.sendMessage(PRIMARY_COLOR + "Loaded Classes: " + SECONDARY_COLOR + classBean.getLoadedClassCount());
        sender.sendMessage(PRIMARY_COLOR + "Total Loaded: " + SECONDARY_COLOR + classBean.getTotalLoadedClassCount());
        sender.sendMessage(PRIMARY_COLOR + "Unloaded Classes: " + SECONDARY_COLOR + classBean.getUnloadedClassCount());

        //garbage collector
        List<GarbageCollectorMXBean> gcBean = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean collector : gcBean) {
            sender.sendMessage(PRIMARY_COLOR + "Garbage Collector: " + SECONDARY_COLOR + collector.getName());
            sender.sendMessage(PRIMARY_COLOR + "    Time: " + SECONDARY_COLOR + collector.getCollectionTime());
            sender.sendMessage(PRIMARY_COLOR + "    Count: " + SECONDARY_COLOR + collector.getCollectionCount());
        }

        return true;
    }
}

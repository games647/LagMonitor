package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VmCommand implements CommandExecutor {

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;
    private static final ChatColor SECONDARY_COLOR = ChatColor.GRAY;

    private final LagMonitor plugin;

    public VmCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        //java info
        sender.sendMessage(PRIMARY_COLOR + "Java VM: " + SECONDARY_COLOR + runtimeBean.getVmName());
        sender.sendMessage(PRIMARY_COLOR + "Java Version: " + SECONDARY_COLOR + System.getProperty("java.version"));
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

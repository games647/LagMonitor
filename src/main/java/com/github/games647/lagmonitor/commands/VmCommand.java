package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.utils.JavaVersion;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class VmCommand extends LagCommand {

    public VmCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canExecute(sender, command)) {
            return true;
        }

        //java version info
        displayJavaVersion(sender);

        //java paths
        sendMessage(sender, "Java lib", System.getProperty("sun.boot.library.path", "Unknown"));
        sendMessage(sender, "Java home", System.getProperty("java.home", "Unknown"));
        sendMessage(sender, "Temp path", System.getProperty("java.io.tmpdir", "Unknown"));

        displayRuntimeInfo(sender, ManagementFactory.getRuntimeMXBean());
        displayCompilationInfo(sender, ManagementFactory.getCompilationMXBean());
        displayClassLoading(sender, ManagementFactory.getClassLoadingMXBean());

        //garbage collector
        for (GarbageCollectorMXBean collector : ManagementFactory.getGarbageCollectorMXBeans()) {
            displayCollectorStats(sender, collector);
        }

        return true;
    }

    private void displayCompilationInfo(CommandSender sender, CompilationMXBean compilationBean) {
        sendMessage(sender, "Compiler name", compilationBean.getName());
        sendMessage(sender, "Compilation time (ms)", String.valueOf(compilationBean.getTotalCompilationTime()));
    }

    private void displayRuntimeInfo(CommandSender sender, RuntimeMXBean runtimeBean) {
        //vm
        sendMessage(sender, "Java VM", runtimeBean.getVmName() + ' ' + runtimeBean.getVmVersion());
        sendMessage(sender, "Java vendor", runtimeBean.getVmVendor());

        //vm specification
        sendMessage(sender, "Spec name", runtimeBean.getSpecName());
        sendMessage(sender, "Spec vendor", runtimeBean.getSpecVendor());
        sendMessage(sender, "Spec version", runtimeBean.getSpecVersion());
    }

    private void displayCollectorStats(CommandSender sender, GarbageCollectorMXBean collector) {
        sendMessage(sender, "Garbage collector", collector.getName());
        sendMessage(sender, "    Count", String.valueOf(collector.getCollectionCount()));
        sendMessage(sender, "    Time (ms)", String.valueOf(collector.getCollectionTime()));
    }

    private void displayClassLoading(CommandSender sender, ClassLoadingMXBean classBean) {
        sendMessage(sender, "Loaded classes", String.valueOf(classBean.getLoadedClassCount()));
        sendMessage(sender, "Total loaded", String.valueOf(classBean.getTotalLoadedClassCount()));
        sendMessage(sender, "Unloaded classes", String.valueOf(classBean.getUnloadedClassCount()));
    }

    private void displayJavaVersion(CommandSender sender) {
        JavaVersion currentVersion = JavaVersion.detect();
        LagCommand.send(sender, formatJavaVersion(currentVersion));

        sendMessage(sender, "Java release date", System.getProperty("java.version.date", "n/a"));
        sendMessage(sender, "Class version", System.getProperty("java.class.version"));
    }

    private BaseComponent[] formatJavaVersion(JavaVersion version) {
        ComponentBuilder builder = new ComponentBuilder("Java version: ").color(PRIMARY_COLOR.asBungee())
                .append(version.getRaw()).color(SECONDARY_COLOR.asBungee());
        if (version.isOutdated()) {
            builder = builder.append(" (").color(ChatColor.WHITE)
                    .append("Outdated").color(ChatColor.DARK_RED)
                    .event(new HoverEvent(Action.SHOW_TEXT,
                            new ComponentBuilder("You're running an outdated Java version. \n"
                                    + "Java 9 and 10 are already released. \n"
                                    + "Newer versions could improve the performance or include bug or security fixes.")
                                    .color(ChatColor.DARK_AQUA).create()))
                    .append(")").color(ChatColor.WHITE);
        }

        return builder.create();
    }
}

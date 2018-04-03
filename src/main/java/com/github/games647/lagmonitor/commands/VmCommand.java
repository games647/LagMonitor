package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.utils.JavaVersion;
import com.google.common.base.StandardSystemProperty;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VmCommand extends LagCommand {

    public VmCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canExecute(sender, command)) {
            return true;
        }

        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        //java info
        sendJavaVersion(sender, JavaVersion.detect());

        sendMessage(sender, "Java release date", System.getProperty("java.version.date", "n/a"));
        sendMessage(sender, "Java VM", runtimeBean.getVmName() + ' ' + runtimeBean.getVmVersion());
        sendMessage(sender, "Java vendor", runtimeBean.getVmVendor());

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
        sendMessage(sender, "Compilation time (ms)", String.valueOf(compileBean.getTotalCompilationTime()));

        //class loading
        ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
        sendMessage(sender, "Loaded classes", String.valueOf(classBean.getLoadedClassCount()));
        sendMessage(sender, "Total loaded", String.valueOf(classBean.getTotalLoadedClassCount()));
        sendMessage(sender, "Unloaded classes", String.valueOf(classBean.getUnloadedClassCount()));

        //garbage collector
        List<GarbageCollectorMXBean> gcBean = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean collector : gcBean) {
            sendMessage(sender, "Garbage collector", collector.getName());
            sendMessage(sender, "    Time (ms)", String.valueOf(collector.getCollectionTime()));
            sendMessage(sender, "    Count", String.valueOf(collector.getCollectionCount()));
        }

        return true;
    }

    private void sendJavaVersion(CommandSender sender, JavaVersion version) {
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

        if (sender instanceof Player) {
            sender.spigot().sendMessage(builder.create());
        } else {
            sender.sendMessage(TextComponent.toLegacyText(builder.create()));
        }
    }
}

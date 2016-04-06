package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.Pagination;
import com.google.common.collect.Lists;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public class StackTraceCommand implements TabExecutor {

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;

    private final LagMonitor plugin;

    public StackTraceCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            String threadName = args[0];
            Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
            for (Map.Entry<Thread, StackTraceElement[]> entry : allStackTraces.entrySet()) {
                Thread thread = entry.getKey();
                if (thread.getName().equalsIgnoreCase(threadName)) {
                    StackTraceElement[] stackTrace = entry.getValue();
                    printStackTrace(sender, stackTrace);
                    return true;
                }
            }

            sender.sendMessage(ChatColor.DARK_RED + "No thread with that name found");
        } else {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            printStackTrace(sender, stackTrace);
        }

        return true;
    }

    private void printStackTrace(CommandSender sender, StackTraceElement[] stackTrace) {
        List<BaseComponent[]> lines = Lists.newArrayList();

        //begin from the top
        for (int i = stackTrace.length - 1; i >= 0; i--) {
            StackTraceElement traceElement = stackTrace[i];

            String className = Pagination.filterPackageNames(traceElement.getClassName());
            String methodName = traceElement.getMethodName();

            boolean nativeMethod = traceElement.isNativeMethod();
            int lineNumber = traceElement.getLineNumber();

            String line = Integer.toString(lineNumber);
            if (nativeMethod) {
                line = "Native";
            }

            lines.add(new ComponentBuilder(className + '.')
                    .color(PRIMARY_COLOR)
                    .append(methodName + ':')
                    .color(ChatColor.DARK_GREEN)
                    .append(line)
                    .color(ChatColor.DARK_PURPLE)
                    .create());
        }

        Pagination pagination = new Pagination("Stacktrace", lines);
        pagination.send(sender);
        plugin.getPaginations().put(sender, pagination);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = Lists.newArrayList();

        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg).append(' ');
        }

        String requestName = builder.toString();

        ThreadInfo[] threads = ManagementFactory.getThreadMXBean().dumpAllThreads(false, false);
        for (ThreadInfo thread : threads) {
            if (thread.getThreadName().startsWith(requestName)) {
                result.add(thread.getThreadName());
            }
        }

        Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
        return result;
    }
}

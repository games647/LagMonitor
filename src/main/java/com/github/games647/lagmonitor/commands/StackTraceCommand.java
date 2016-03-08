package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.google.common.collect.Lists;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
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
        //begin from the top
        for (int i = stackTrace.length - 1; i >= 0; i--) {
            StackTraceElement traceElement = stackTrace[i];

            String className = traceElement.getClassName();
            String methodName = ChatColor.DARK_GREEN + traceElement.getMethodName();

            boolean nativeMethod = traceElement.isNativeMethod();
            int lineNumber = traceElement.getLineNumber();

            String line = Integer.toString(lineNumber);
            if (nativeMethod) {
                line = "Native";
            }

            sender.sendMessage(PRIMARY_COLOR + className + '.' + methodName + ':' + ChatColor.DARK_PURPLE + line);
        }
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

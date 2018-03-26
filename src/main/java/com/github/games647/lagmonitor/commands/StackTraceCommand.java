package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.Pagination;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public class StackTraceCommand extends LagCommand implements TabExecutor {

    public StackTraceCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, command)) {
            sender.sendMessage(ChatColor.DARK_RED + "Not whitelisted");
            return true;
        }

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
        List<BaseComponent[]> lines = new ArrayList<>();

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
                    .color(PRIMARY_COLOR.asBungee())
                    .append(methodName + ':')
                    .color(ChatColor.DARK_GREEN)
                    .append(line)
                    .color(ChatColor.DARK_PURPLE)
                    .create());
        }

        Pagination pagination = new Pagination("Stacktrace", lines);
        pagination.send(sender);
        plugin.getPaginations().put(sender.getName(), pagination);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();

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

        result.sort(String.CASE_INSENSITIVE_ORDER);
        return result;
    }
}

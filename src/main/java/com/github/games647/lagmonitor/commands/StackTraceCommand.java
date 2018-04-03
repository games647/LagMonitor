package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.Pages;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public class StackTraceCommand extends LagCommand implements TabExecutor {

    private static final int MAX_DEPTH = 75;

    public StackTraceCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canExecute(sender, command)) {
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

            sendError(sender, "No thread with that name found");
        } else {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            ThreadInfo threadInfo = threadBean.getThreadInfo(Thread.currentThread().getId(), MAX_DEPTH);
            printStackTrace(sender, threadInfo.getStackTrace());
        }

        return true;
    }

    private void printStackTrace(CommandSender sender, StackTraceElement[] stackTrace) {
        List<BaseComponent[]> lines = new ArrayList<>();

        //begin from the top
        for (int i = stackTrace.length - 1; i >= 0; i--) {
            lines.add(formatTraceElement(stackTrace[i]));
        }

        Pages pagination = new Pages("Stacktrace", lines);
        pagination.send(sender);
        plugin.getPageManager().setPagination(sender.getName(), pagination);
    }

    private BaseComponent[] formatTraceElement(StackTraceElement traceElement) {
        String className = Pages.filterPackageNames(traceElement.getClassName());
        String methodName = traceElement.getMethodName();

        boolean nativeMethod = traceElement.isNativeMethod();
        int lineNumber = traceElement.getLineNumber();

        String line = Integer.toString(lineNumber);
        if (nativeMethod) {
            line = "Native";
        }

        return new ComponentBuilder(className + '.')
                .color(PRIMARY_COLOR.asBungee())
                .append(methodName + ':')
                .color(ChatColor.DARK_GREEN)
                .append(line)
                .color(ChatColor.DARK_PURPLE)
                .create();
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
        return Arrays.stream(threads)
                .map(ThreadInfo::getThreadName)
                .filter(name -> name.startsWith(requestName))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }
}

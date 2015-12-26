package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.ChatPaginator;

public class StackCommand implements CommandExecutor {

    private final LagMonitor plugin;

    public StackCommand(LagMonitor plugin) {
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
        for (int i = 0; i < ChatPaginator.OPEN_CHAT_PAGE_HEIGHT * 3; i++) {
            StackTraceElement traceElement = stackTrace[i];

            String className = traceElement.getClassName();
            String methodName = traceElement.getMethodName();
            int lineNumber = traceElement.getLineNumber();
            sender.sendMessage(ChatColor.GOLD + className + methodName + lineNumber);
        }
    }
}

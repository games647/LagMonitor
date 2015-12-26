package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.lang.management.ManagementFactory;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MbeanCommand implements CommandExecutor {

    private final LagMonitor plugin;

    public MbeanCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

        Set<ObjectInstance> allBeans = mbeanServer.queryMBeans(null, null);
        for (ObjectInstance mbean : allBeans) {
            sender.sendMessage(ChatColor.GOLD + mbean.getClassName());
        }

        return true;
    }
}

package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.lang.management.ManagementFactory;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import javax.management.MBeanAttributeInfo;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

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

        if (args.length > 0) {
            try {
                ObjectName beanObject = ObjectName.getInstance(args[0]);
                if (args.length > 1) {
                    Object result = mbeanServer.getAttribute(beanObject, args[1]);
                    sender.sendMessage(ChatColor.DARK_GREEN + Objects.toString(result));
                } else {
                    MBeanAttributeInfo[] attributes = mbeanServer.getMBeanInfo(beanObject).getAttributes();
                    for (MBeanAttributeInfo attribute : attributes) {
                        if (attribute.getName().equals("ObjectName")) {
                            //ignore the object name
                            continue;
                        }

                        sender.sendMessage(ChatColor.YELLOW + attribute.getName());
                    }
                }
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, null, ex);
            }
        } else {
            Set<ObjectInstance> allBeans = mbeanServer.queryMBeans(null, null);
            for (ObjectInstance mbean : allBeans) {
                sender.sendMessage(ChatColor.GOLD + mbean.getObjectName().getCanonicalName());
            }
        }

        return true;
    }
}

package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.google.common.collect.Lists;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public class MbeanCommand implements TabExecutor {

    private final LagMonitor plugin;

    public MbeanCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

        if (args.length > 0) {
            try {
                ObjectName beanObject = ObjectName.getInstance(args[0]);
                if (args.length > 1) {
                    Object result = mBeanServer.getAttribute(beanObject, args[1]);
                    sender.sendMessage(ChatColor.DARK_GREEN + Objects.toString(result));
                } else {
                    MBeanAttributeInfo[] attributes = mBeanServer.getMBeanInfo(beanObject).getAttributes();
                    for (MBeanAttributeInfo attribute : attributes) {
                        if (attribute.getName().equals("ObjectName")) {
                            //ignore the object name - it's already known if the user invoke the command
                            continue;
                        }

                        sender.sendMessage(ChatColor.YELLOW + attribute.getName());
                    }
                }
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, null, ex);
            }
        } else {
            Set<ObjectInstance> allBeans = mBeanServer.queryMBeans(null, null);
            for (ObjectInstance mbean : allBeans) {
                sender.sendMessage(ChatColor.DARK_AQUA + mbean.getObjectName().getCanonicalName());
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = Lists.newArrayList();
        String lastArg = args[args.length - 1];

        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        if (args.length == 1) {
            Set<ObjectName> mbeans = mBeanServer.queryNames(null, null);
            for (ObjectName mbean : mbeans) {
                if (mbean.getCanonicalName().startsWith(lastArg)) {
                    result.add(mbean.getCanonicalName());
                }
            }
        } else if (args.length == 2) {
            try {
                ObjectName beanObject = ObjectName.getInstance(args[0]);
                MBeanAttributeInfo[] attributes = mBeanServer.getMBeanInfo(beanObject).getAttributes();
                for (MBeanAttributeInfo attribute : attributes) {
                    if (attribute.getName().equals("ObjectName")) {
                        //ignore the object name - it's already known if the user invoke the command
                        continue;
                    }

                    if (attribute.getName().startsWith(lastArg)) {
                        result.add(attribute.getName());
                    }
                }
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, null, ex);
            }
        }

        Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
        return result;
    }
}

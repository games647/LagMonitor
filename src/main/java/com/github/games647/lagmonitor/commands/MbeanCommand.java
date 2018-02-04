package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
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

import static java.util.stream.Collectors.toList;

public class MbeanCommand extends LagCommand implements TabExecutor {

    public MbeanCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, command)) {
            sender.sendMessage(org.bukkit.ChatColor.DARK_RED + "Not whitelisted");
            return true;
        }

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
                        if ("ObjectName".equals(attribute.getName())) {
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
            allBeans.stream()
                    .map(ObjectInstance::getObjectName)
                    .map(ObjectName::getCanonicalName)
                    .forEach(bean -> sender.sendMessage(ChatColor.DARK_AQUA + bean));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        String lastArg = args[args.length - 1];

        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        if (args.length == 1) {
            Set<ObjectName> mbeans = mBeanServer.queryNames(null, null);
            result.addAll(mbeans.stream()
                    .filter(mbean -> mbean.getCanonicalName().startsWith(lastArg))
                    .map(ObjectName::getCanonicalName)
                    .collect(toList()));
        } else if (args.length == 2) {
            try {
                ObjectName beanObject = ObjectName.getInstance(args[0]);
                MBeanAttributeInfo[] attributes = mBeanServer.getMBeanInfo(beanObject).getAttributes();
                for (MBeanAttributeInfo attribute : attributes) {
                    if ("ObjectName".equals(attribute.getName())) {
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

        result.sort(String.CASE_INSENSITIVE_ORDER);
        return result;
    }
}

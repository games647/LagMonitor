package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.bukkit.command.CommandExecutor;

public abstract class DumpCommand implements CommandExecutor {

    protected final LagMonitor plugin;

    private final String filePrefix;
    private final String fileExt;

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    public DumpCommand(LagMonitor plugin, String filePrefix, String fileExt) {
        this.plugin = plugin;

        this.filePrefix = filePrefix;
        this.fileExt = '.' + fileExt;
    }

    public Path getNewDumpFile() {
        String timeSuffix = '-' + LocalDateTime.now().format(dateFormat);
        Path folder = plugin.getDataFolder().toPath();
        return folder.resolve(filePrefix + '-' + timeSuffix + fileExt);
    }

    public Object invokeBeanCommand(String beanName, String command, Object[] args, String[] signature)
            throws MalformedObjectNameException, MBeanException, InstanceNotFoundException, ReflectionException {
        MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName beanObject = ObjectName.getInstance(beanName);

        return beanServer.invoke(beanObject, command, args, signature);
    }
}

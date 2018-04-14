package com.github.games647.lagmonitor.command.dump;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.command.LagCommand;

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

public abstract class DumpCommand extends LagCommand {

    //https://docs.oracle.com/javase/10/docs/jre/api/management/extension/com/sun/management/DiagnosticCommandMBean.html
    protected static final String DIAGNOSTIC_BEAN = "com.sun.management:type=DiagnosticCommand";
    protected static final String NOT_ORACLE_MSG = "You are not using Oracle JVM. OpenJDK hasn't implemented it yet";

    private final String filePrefix;
    private final String fileExt;

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    public DumpCommand(LagMonitor plugin, String filePrefix, String fileExt) {
        super(plugin);

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

    public String invokeDiagnosticCommand(String command, String... args)
            throws MalformedObjectNameException, ReflectionException, MBeanException, InstanceNotFoundException {
        return (String) invokeBeanCommand(DIAGNOSTIC_BEAN, command,
                new Object[]{args}, new String[]{String[].class.getName()});
    }
}

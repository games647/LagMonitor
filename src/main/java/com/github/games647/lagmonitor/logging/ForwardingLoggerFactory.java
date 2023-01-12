package com.github.games647.lagmonitor.logging;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.jul.JDK14LoggerAdapter;

public class ForwardingLoggerFactory implements ILoggerFactory {

    private final ConcurrentMap<String, Logger> loggerMap = new ConcurrentHashMap<>();

    public static java.util.logging.Logger PARENT_LOGGER;

    @Override
    public Logger getLogger(String name) {
        return loggerMap.computeIfAbsent(name, key -> {
            java.util.logging.Logger julLogger;
            if (PARENT_LOGGER == null) {
                julLogger = java.util.logging.Logger.getLogger(name);
            } else {
                julLogger = PARENT_LOGGER;
            }

            Logger newInstance = null;
            try {
                newInstance = createJDKLogger(julLogger);
            } catch (NoSuchMethodException | InvocationTargetException |
                     InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                System.out.println("Failed to created logging instance");
            }

            return newInstance;
        });
    }

    protected static Logger createJDKLogger(java.util.logging.Logger parent)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> adapterClass = JDK14LoggerAdapter.class;
        Constructor<?> cons = adapterClass.getDeclaredConstructor(java.util.logging.Logger.class);
        cons.setAccessible(true);
        return (Logger) cons.newInstance(parent);
    }
}

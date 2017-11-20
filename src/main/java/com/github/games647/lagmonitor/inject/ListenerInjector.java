package com.github.games647.lagmonitor.inject;

import com.github.games647.lagmonitor.traffic.Reflection;
import com.github.games647.lagmonitor.traffic.Reflection.FieldAccessor;

import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

public class ListenerInjector implements EventExecutor {

    private final EventExecutor originalExecutor;

    private long totalTime;
    private long count;

    public ListenerInjector(EventExecutor originalExecutor) {
        this.originalExecutor = originalExecutor;
    }

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        if (!event.isAsynchronous()) {
            long start = System.nanoTime();
            //todo add a more aggressive 10 ms cpu sample
            originalExecutor.execute(listener, event);
            long end = System.nanoTime();

            totalTime += end - start;
            count++;
        }
    }

    public EventExecutor getOriginalExecutor() {
        return originalExecutor;
    }

    public static void inject(Plugin plugin) {
        List<RegisteredListener> listeners = HandlerList.getRegisteredListeners(plugin);
        for (RegisteredListener listener : listeners) {
            HandlerList.unregisterAll(listener.getListener());
            FieldAccessor<EventExecutor> executorField = Reflection
                    .getField(RegisteredListener.class, "executor", EventExecutor.class);

            EventExecutor originalExecutor = executorField.get(listener);
            ListenerInjector listenerInjector = new ListenerInjector(originalExecutor);

            executorField.set(listener, listenerInjector);
        }
    }

    public static void uninject(Plugin plugin) {
        List<RegisteredListener> listeners = HandlerList.getRegisteredListeners(plugin);
        for (RegisteredListener listener : listeners) {
            HandlerList.unregisterAll(listener.getListener());
            FieldAccessor<EventExecutor> executorField = Reflection
                    .getField(RegisteredListener.class, "executor", EventExecutor.class);

            EventExecutor executor = executorField.get(listener);
            if (executor instanceof ListenerInjector) {
                executorField.set(listener, ((ListenerInjector) executor).originalExecutor);
            }
        }
    }
}

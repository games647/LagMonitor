package com.github.games647.lagmonitor.inject;

import com.github.games647.lagmonitor.traffic.Reflection;
import com.github.games647.lagmonitor.traffic.Reflection.FieldAccessor;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;

public class CommandInjector implements TabExecutor {

    private final CommandExecutor originalExecutor;
    private final TabCompleter originalCompleter;

    private long totalTime;
    private long count;

    public CommandInjector(CommandExecutor originalCommandExecutor, TabCompleter originalTabCompleter) {
        this.originalExecutor = originalCommandExecutor;
        this.originalCompleter = originalTabCompleter;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        long start = System.nanoTime();
         //todo add a more aggressive 10 ms cpu sample
        boolean result = originalExecutor.onCommand(sender, command, label, args);
        long end = System.nanoTime();

        totalTime += end - start;
        count++;
        return result;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        long start = System.nanoTime();
         //todo add a more aggressive 10 ms cpu sample
        List<String> result = originalCompleter.onTabComplete(sender, command, alias, args);
        long end = System.nanoTime();

        totalTime += end - start;
        count++;
        return result;
    }

    public static void inject(Plugin toInjectPlugin) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        SimpleCommandMap commandMap = Reflection
                .getField(SimplePluginManager.class, "commandMap", SimpleCommandMap.class).get(pluginManager);
        for (Command command : commandMap.getCommands()) {
            if (command instanceof PluginCommand) {
                PluginIdentifiableCommand pluginCommand = (PluginIdentifiableCommand) command;
                Plugin plugin = pluginCommand.getPlugin();
                if (plugin.equals(toInjectPlugin)) {
                    FieldAccessor<CommandExecutor> executorField = Reflection
                            .getField(PluginCommand.class, "executor", CommandExecutor.class);
                    FieldAccessor<TabCompleter> completerField = Reflection
                            .getField(PluginCommand.class, "completer", TabCompleter.class);

                    CommandExecutor executor = executorField.get(pluginCommand);
                    TabCompleter completer = completerField.get(pluginCommand);

                    CommandInjector commandInjector = new CommandInjector(executor, completer);

                    executorField.set(pluginCommand, commandInjector);
                    completerField.set(pluginCommand, commandInjector);
                }
            }

            //idea: inject also vanilla commands?
//            if (command instanceof VanillaCommand) {
//
//            }
        }
    }

    public CommandExecutor getOriginalExecutor() {
        return originalExecutor;
    }

    public TabCompleter getOriginalCompleter() {
        return originalCompleter;
    }

    public static void uninject(Plugin toUninject) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        SimpleCommandMap commandMap = Reflection
                .getField(SimplePluginManager.class, "commandMap", SimpleCommandMap.class).get(pluginManager);
        for (Command command : commandMap.getCommands()) {
            if (command instanceof PluginCommand) {
                PluginIdentifiableCommand pluginCommand = (PluginIdentifiableCommand) command;
                Plugin plugin = pluginCommand.getPlugin();
                if (plugin.equals(toUninject)) {
                    FieldAccessor<CommandExecutor> executorField = Reflection
                            .getField(PluginCommand.class, "executor", CommandExecutor.class);
                    FieldAccessor<TabCompleter> completerField = Reflection
                            .getField(PluginCommand.class, "completer", TabCompleter.class);

                    CommandExecutor executor = executorField.get(pluginCommand);
                    if (executor instanceof CommandInjector) {
                        executorField.set(pluginCommand, ((CommandInjector) executor).originalExecutor);
                    }

                    TabCompleter completer = completerField.get(pluginCommand);
                    if (completer instanceof CommandInjector) {
                        completerField.set(pluginCommand, ((CommandInjector) completer).originalCompleter);
                    }
                }
            }
        }
    }
}

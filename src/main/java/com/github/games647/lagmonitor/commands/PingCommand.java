package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PingCommand implements CommandExecutor {

    private final LagMonitor plugin;

    private Method getHandleMethod;
    private Field pingField;

    public PingCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer != null) {
                int ping = getReflectionPing((Player) sender);
                sender.sendMessage(ChatColor.DARK_GREEN + targetPlayer.getName() + "'s ping is: " + ping + "ms");
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "Player " + args[0] + " is not online or does not extist.");
            }
        } else if (sender instanceof Player) {
            int ping = getReflectionPing((Player) sender);
            sender.sendMessage(ChatColor.DARK_GREEN + "Your ping is: " + ping + "ms");
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "You have to be ingame in order to see your own ping");
        }

        return true;
    }

    private int getReflectionPing(Player player) {
        try {
            if (getHandleMethod == null) {
                getHandleMethod = player.getClass().getDeclaredMethod("getHandle");
                //disable java security check. This will speed it a little
                getHandleMethod.setAccessible(true);
            }

            Object entityPlayer = getHandleMethod.invoke(player);

            if (pingField == null) {
                if (isModdedServer()) {
                    //MCPC has a remapper, but it doesn't work if we get the class dynamic
                    setMCPCPing(entityPlayer);
                } else {
                    pingField = entityPlayer.getClass().getDeclaredField("ping");
                    //disable java security check. This will speed it a little
                    pingField.setAccessible(true);
                }
            }

            //returns the found int value
            return pingField.getInt(entityPlayer);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean isModdedServer() {
        //aggressive checking for modded servers
        List<String> versionNames = Arrays.asList(Bukkit.getVersion(), Bukkit.getName(), Bukkit.getServer().toString());
        for (String version : versionNames) {
            if (version.contains("MCPC") || version.contains("Cauldron")) {
                return true;
            }
        }

        return false;
    }

    private void setMCPCPing(Object entityPlayer) {
        //this isn't a secure, because it detects the ping variable by the ordering
        //a remaping (deobfuscate the variables) would work, but it won't be forwardcompatible
        Class<?> lastType = null;
        Field lastIntField = null;
        for (Field field : entityPlayer.getClass().getDeclaredFields()) {
            if (field.getType() == Integer.TYPE
                    && Modifier.isPublic(field.getModifiers())
                    && lastType == Boolean.TYPE) {
                lastIntField = field;
                continue;
            }

            if (field.getType() == Boolean.TYPE && lastIntField != null) {
                pingField = lastIntField;
                //disable java security check. This will speed it a little
                pingField.setAccessible(true);
                break;
            }

            lastIntField = null;
            lastType = field.getType();
        }
    }
}

package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.Pagination;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.primitives.Ints;
import java.io.File;
import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import net.md_5.bungee.api.chat.BaseComponent;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PaginationCommand implements CommandExecutor {

    private final LagMonitor plugin;

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    private static final String DUMP_FILE_NAME = "pagination";
    private static final String DUMP_FILE_ENDING = ".txt";

    public PaginationCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.isAllowed(sender, command)) {
            sender.sendMessage(org.bukkit.ChatColor.DARK_RED + "Not whitelisted");
            return true;
        }

        Pagination pagination = plugin.getPaginations().get(sender);
        if (pagination == null) {
            sender.sendMessage(ChatColor.DARK_RED + "You have no pagination session");
            return true;
        }

        if (args.length > 0) {
            String subCommand = args[0];
            if ("next".equalsIgnoreCase(subCommand)) {
                onNextPage(pagination, sender);
            } else if ("prev".equalsIgnoreCase(subCommand)) {
                onPrevPage(pagination, sender);
            } else if ("all".equalsIgnoreCase(subCommand)) {
                onShowAll(pagination, sender);
            } else if ("save".equalsIgnoreCase(subCommand)) {
                onSave(pagination, sender);
            } else {
                onPageNumber(subCommand, sender, pagination);
            }
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Not enough arguments");
        }

        return true;
    }

    private void onPageNumber(String subCommand, CommandSender sender, Pagination pagination) {
        Integer page = Ints.tryParse(subCommand);
        if (page == null) {
            sender.sendMessage(ChatColor.DARK_RED + "Unkown subcommand or not a valid page number");
        } else {
            if (page < 1) {
                sender.sendMessage(ChatColor.DARK_RED + "Page number is too small");
                return;
            } else if (page > pagination.getTotalPages(sender instanceof Player)) {
                sender.sendMessage(ChatColor.DARK_RED + "Page number is too high");
                return;
            }

            pagination.send(sender, page);
        }
    }

    private void onNextPage(Pagination pagination, CommandSender sender) {
        int lastPage = pagination.getLastSentPage();
        if (lastPage >= pagination.getTotalPages(sender instanceof Player)) {
            sender.sendMessage(ChatColor.DARK_RED + "You are already on the last page");
            return;
        }

        pagination.send(sender, lastPage + 1);
    }

    private void onPrevPage(Pagination pagination, CommandSender sender) {
        int lastPage = pagination.getLastSentPage();
        if (lastPage <= 1) {
            sender.sendMessage(ChatColor.DARK_RED + "You are already on the first page");
            return;
        }

        pagination.send(sender, lastPage - 1);
    }

    private void onSave(Pagination pagination, CommandSender sender) {
        String timeSuffix = '-' + dateFormat.format(new Date());
        File dumpFile = new File(plugin.getDataFolder(), DUMP_FILE_NAME + timeSuffix + DUMP_FILE_ENDING);

        StringBuilder lineBuilder = new StringBuilder();
        for (BaseComponent[] line : pagination.getAllLines()) {
            for (BaseComponent component : line) {
                lineBuilder.append(component.toLegacyText());
            }

            lineBuilder.append('\n');
        }

        try {
            Files.write(lineBuilder.toString(), dumpFile, Charsets.UTF_8);
            sender.sendMessage(ChatColor.GRAY + "Dump created: " + dumpFile.getCanonicalPath());
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    private void onShowAll(Pagination pagination, CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.spigot().sendMessage(pagination.buildHeader(1, 1));
        } else {
            BaseComponent[] header = pagination.buildHeader(1, 1);
            StringBuilder headerBuilder = new StringBuilder();
            for (BaseComponent component : header) {
                headerBuilder.append(component.toLegacyText());
            }

            sender.sendMessage(headerBuilder.toString());
        }

        for (BaseComponent[] line : pagination.getAllLines()) {
            StringBuilder lineBuilder = new StringBuilder();
            for (BaseComponent component : line) {
                lineBuilder.append(component.toLegacyText());
            }

            sender.sendMessage(lineBuilder.toString());
        }
    }
}

package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.Pagination;
import com.github.games647.lagmonitor.commands.dump.DumpCommand;
import com.google.common.primitives.Ints;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.logging.Level;

import net.md_5.bungee.api.chat.BaseComponent;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PaginationCommand extends DumpCommand {

    public PaginationCommand(LagMonitor plugin) {
        super(plugin, "pagination", "txt");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canExecute(sender, command)) {
            return true;
        }

        Pagination pagination = plugin.getPageManager().getPagination(sender.getName());
        if (pagination == null) {
            sendError(sender, "You have no pagination session");
            return true;
        }

        if (args.length > 0) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "next":
                    onNextPage(pagination, sender);
                    break;
                case "prev":
                    onPrevPage(pagination, sender);
                    break;
                case "all":
                    onShowAll(pagination, sender);
                    break;
                case "save":
                    onSave(pagination, sender);
                    break;
                default:
                    onPageNumber(subCommand, sender, pagination);
            }
        } else {
            sendError(sender, "Not enough arguments");
        }

        return true;
    }

    private void onPageNumber(String subCommand, CommandSender sender, Pagination pagination) {
        Integer page = Ints.tryParse(subCommand);
        if (page == null) {
            sendError(sender, "Unknown subcommand or not a valid page number");
        } else {
            if (page < 1) {
                sendError(sender, "Page number too small");
                return;
            } else if (page > pagination.getTotalPages(sender instanceof Player)) {
                sendError(sender, "Page number too high");
                return;
            }

            pagination.send(sender, page);
        }
    }

    private void onNextPage(Pagination pagination, CommandSender sender) {
        int lastPage = pagination.getLastSentPage();
        if (lastPage >= pagination.getTotalPages(sender instanceof Player)) {
            sendError(sender,"You are already on the last page");
            return;
        }

        pagination.send(sender, lastPage + 1);
    }

    private void onPrevPage(Pagination pagination, CommandSender sender) {
        int lastPage = pagination.getLastSentPage();
        if (lastPage <= 1) {
            sendError(sender,"You are already on the first page");
            return;
        }

        pagination.send(sender, lastPage - 1);
    }

    private void onSave(Pagination pagination, CommandSender sender) {
        StringBuilder lineBuilder = new StringBuilder();
        for (BaseComponent[] line : pagination.getAllLines()) {
            for (BaseComponent component : line) {
                lineBuilder.append(component.toLegacyText());
            }

            lineBuilder.append('\n');
        }

        Path dumpFile = getNewDumpFile();
        try {
            Files.write(dumpFile, Collections.singletonList(lineBuilder.toString()));
            sender.sendMessage(ChatColor.GRAY + "Dump created: " + dumpFile.getFileName());
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

        pagination.getAllLines().stream().map((line) -> {
            StringBuilder lineBuilder = new StringBuilder();
            for (BaseComponent component : line) {
                lineBuilder.append(component.toLegacyText());
            }

            return lineBuilder.toString();
        }).forEach(sender::sendMessage);
    }
}

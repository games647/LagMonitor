package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.Pagination;
import com.google.common.primitives.Ints;


import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PaginationCommand implements CommandExecutor {

    private final LagMonitor plugin;

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
}

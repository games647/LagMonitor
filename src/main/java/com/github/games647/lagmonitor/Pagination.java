package com.github.games647.lagmonitor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

public class Pagination {

    private static final int PAGINATION_LINES = 2;

    private static final int CONSOLE_WIDTH = 120;
    private static final int CONSOLE_HEIGHT = 40 - PAGINATION_LINES;

    private static final int PLAYER_WIDTH = ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH;
    private static final int PLAYER_HEIGHT = ChatPaginator.OPEN_CHAT_PAGE_HEIGHT - PAGINATION_LINES;

    public static String filterPackageNames(String packageName) {
        String text = packageName;
        if (text.contains("net.minecraft.server")) {
            text = text.replace("net.minecraft.server", "NMS");
        } else if (text.contains("org.bukkit.craftbukkit")) {
            text = text.replace("org.bukkit.craftbukkit", "OBC");
        }

        //IDEA: if it's a player we need to shortener the text more aggressivly
        //maybe replacing the package with the plugin name
        //by getting the package name from the plugin.yml?
        return text;
    }

    private final String date = LocalTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT));
    private final String title;

    private final List<BaseComponent[]> lines;

    private int lastSentPage = 1;

    public Pagination(String title, List<BaseComponent[]> lines) {
        this.title = title;
        this.lines = lines;
    }

    public int getTotalPages(boolean isPlayer) {
        if (isPlayer) {
            return (lines.size() / PLAYER_HEIGHT) + 1;
        }

        return (lines.size() / CONSOLE_HEIGHT) + 1;
    }

    public List<BaseComponent[]> getAllLines() {
        return lines;
    }

    public int getLastSentPage() {
        return lastSentPage;
    }

    public void setLastSentPage(int lastSentPage) {
        this.lastSentPage = lastSentPage;
    }

    public List<BaseComponent[]> getPage(int page, boolean isPlayer) {
        int startIndex;
        int endIndex;
        if (isPlayer) {
            startIndex = (page - 1) * PLAYER_HEIGHT;
            endIndex = page * PLAYER_HEIGHT;
        } else {
            startIndex = (page - 1) * CONSOLE_HEIGHT;
            endIndex = page * CONSOLE_HEIGHT;
        }

        if (startIndex >= lines.size()) {
            endIndex = lines.size() - 1;
            startIndex = endIndex;
        } else if (endIndex >= lines.size()) {
            endIndex = lines.size() - 1;
        }

        return lines.subList(startIndex, endIndex);
    }

    public BaseComponent[] buildHeader(int page, int totalPages) {
        return new ComponentBuilder(title + " from " + date)
                .color(ChatColor.GOLD)
                .append(" << ")
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT
                        , new ComponentBuilder("Go to the previous page").create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lagpage " + (page - 1)))
                .color(ChatColor.DARK_AQUA)
                .append(page + " / " + totalPages, FormatRetention.NONE)
                .color(ChatColor.GRAY)
                .append(" >>")
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT
                        , new ComponentBuilder("Go to the next page").create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lagpage " + (page + 1)))
                .color(ChatColor.DARK_AQUA)
                .create();
    }

    public String buildFooter(int page, boolean isPlayer) {
        int endIndex;
        if (isPlayer) {
            endIndex = page * PLAYER_HEIGHT;
        } else {
            endIndex = page * CONSOLE_HEIGHT;
        }

        if (endIndex < lines.size()) {
            //Index starts by 0
            int remaining = lines.size() - endIndex - 1;
            return "... " + remaining + " more entries. Click the arrows above or type /lagpage next";
        }

        return "";
    }

    public void send(CommandSender sender) {
        send(sender, 1);
    }

    public void send(CommandSender sender, int page) {
        this.lastSentPage = page;

        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.spigot().sendMessage(buildHeader(page, getTotalPages(true)));
            
            getPage(page, true).forEach(player.spigot()::sendMessage);

            String footer = buildFooter(page, true);
            if (!footer.isEmpty()) {
                sender.sendMessage(ChatColor.GOLD + footer);
            }
        } else {
            BaseComponent[] header = buildHeader(page, getTotalPages(false));
            StringBuilder headerBuilder = new StringBuilder();
            for (BaseComponent component : header) {
                headerBuilder.append(component.toLegacyText());
            }

            sender.sendMessage(headerBuilder.toString());
            getPage(page, false).stream().map((line) -> {
                StringBuilder lineBuilder = new StringBuilder();
                for (BaseComponent component : line) {
                    lineBuilder.append(component.toLegacyText());
                }
                
                return lineBuilder.toString();
            }).forEach(sender::sendMessage);

            String footer = buildFooter(page, false);
            if (!footer.isEmpty()) {
                sender.sendMessage(ChatColor.GOLD + footer);
            }
        }
    }
}

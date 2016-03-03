package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.google.common.collect.Lists;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.ChatPaginator;

public class TpsHistoryCommand implements CommandExecutor {

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;
    private static final char GRAPH_CHAR = '+';
    private static final int GRAPH_LINES = 7;

    private final LagMonitor plugin;

    public TpsHistoryCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<Float> lastSeconds = plugin.getTpsHistoryTask().getLastSeconds();

        List<StringBuilder> graphLines = Lists.newArrayListWithExpectedSize(GRAPH_LINES);
        for (int line = 1; line <= GRAPH_LINES; line++) {
            graphLines.add(new StringBuilder(ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH * 2));
        }

        //in x-direction
        int xPos = 1;
        for (float sampleSecond : lastSeconds) {
            xPos++;
            if (xPos >= ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH) {
                break;
            }

            ChatColor color = ChatColor.DARK_RED;
            int lines = 0;
            if (sampleSecond > 19.9F) {
                lines = GRAPH_LINES;
                color = ChatColor.DARK_GREEN;
            } else if (sampleSecond > 18F) {
                lines = GRAPH_LINES - 1;
                color = ChatColor.GREEN;
            } else if (sampleSecond > 17F) {
                lines = GRAPH_LINES - 2;
                color = ChatColor.YELLOW;
            } else if (sampleSecond > 16F) {
                lines = GRAPH_LINES - 3;
                color = ChatColor.GOLD;
            } else if (sampleSecond > 15F) {
                lines = GRAPH_LINES - 4;
                color = ChatColor.RED;
            } else if (sampleSecond > 13F) {
                lines = GRAPH_LINES - 5;
                color = ChatColor.DARK_RED;
            } else if (sampleSecond > 10F) {
                lines = GRAPH_LINES - 6;
                color = ChatColor.DARK_RED;
            }

            //in y-direction in reverse order
            for (int line = GRAPH_LINES - 1; line >= 0; line--) {
                if (lines == 0) {
                    graphLines.get(line).append(' ');
                    continue;
                }

                lines--;
                graphLines.get(line).append(color).append("+");
            }
        }

        for (StringBuilder graphLine : graphLines) {
            sender.sendMessage(graphLine.toString());
        }

        sender.sendMessage(PRIMARY_COLOR + "Current TPS: " + plugin.getTpsHistoryTask().getLastSample());
        return true;
    }
}

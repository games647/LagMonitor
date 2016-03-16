package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.google.common.collect.Lists;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpsHistoryCommand implements CommandExecutor {

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;

    private static final char EMPTY_CHAR = 'X';
    private static final char GRAPH_CHAR = '+';

    private static final char PLAYER_EMPTY_CHAR = '▂';
    private static final char PLAYER_GRAPH_CHAR = '▇';

    private static final int GRAPH_WIDTH = 60 / 2;
    private static final int GRAPH_LINES = 7;

    private final LagMonitor plugin;

    public TpsHistoryCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<StringBuilder> graphLines = Lists.newArrayListWithExpectedSize(GRAPH_LINES);
        for (int line = 1; line <= GRAPH_LINES; line++) {
            graphLines.add(new StringBuilder(GRAPH_WIDTH * 2));
        }

        List<Float> lastSeconds = plugin.getTpsHistoryTask().getLastSeconds();

        boolean console = true;
        if (sender instanceof Player) {
            console = false;
        }

        buildGraph(lastSeconds, graphLines, console);
        for (StringBuilder graphLine : graphLines) {
            sender.sendMessage(graphLine.toString());
        }

        sender.sendMessage(PRIMARY_COLOR + "Current TPS: " + plugin.getTpsHistoryTask().getLastSample());
        return true;
    }

    private void buildGraph(List<Float> lastSeconds, List<StringBuilder> graphLines, boolean console) {
        //in x-direction
        int xPos = 1;
        for (float sampleSecond : lastSeconds) {
            xPos++;
            if (xPos >= GRAPH_WIDTH) {
                break;
            }

            buildLine(sampleSecond, graphLines, console);
        }
    }

    private void buildLine(float sampleSecond, List<StringBuilder> graphLines, boolean console) {
        ChatColor color = ChatColor.DARK_RED;
        int lines = 6;
        if (sampleSecond > 19.5F) {
            lines = GRAPH_LINES;
            color = ChatColor.DARK_GREEN;
        } else if (sampleSecond > 18F) {
            lines = GRAPH_LINES - 1;
            color = ChatColor.GREEN;
        } else if (sampleSecond > 17F) {
            lines = GRAPH_LINES - 2;
            color = ChatColor.YELLOW;
        } else if (sampleSecond > 15F) {
            lines = GRAPH_LINES - 3;
            color = ChatColor.GOLD;
        } else if (sampleSecond > 12F) {
            lines = GRAPH_LINES - 4;
            color = ChatColor.RED;
        }

        //in y-direction in reverse order
        for (int line = GRAPH_LINES - 1; line >= 0; line--) {
            if (lines == 0) {
                graphLines.get(line).append(color).append(console ? EMPTY_CHAR : PLAYER_EMPTY_CHAR);
                continue;
            }

            lines--;
            graphLines.get(line).append(color).append(console ? GRAPH_CHAR : PLAYER_GRAPH_CHAR);
        }
    }
}

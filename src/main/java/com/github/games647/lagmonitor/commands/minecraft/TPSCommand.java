package com.github.games647.lagmonitor.commands.minecraft;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.commands.LagCommand;
import com.github.games647.lagmonitor.tasks.TpsHistoryTask;
import com.google.common.collect.Lists;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.IntStream;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

public class TPSCommand extends LagCommand {

    private static final ChatColor PRIMARY_COLOR = ChatColor.DARK_AQUA;
    private static final ChatColor SECONDARY_COLOR = ChatColor.GRAY;

    private static final char EMPTY_CHAR = ' ';
    private static final char GRAPH_CHAR = '+';

    private static final char PLAYER_EMPTY_CHAR = '▂';
    private static final char PLAYER_GRAPH_CHAR = '▇';

    private static final int GRAPH_WIDTH = 60 / 2;
    private static final int GRAPH_LINES = ChatPaginator.CLOSED_CHAT_PAGE_HEIGHT - 3;

    public TPSCommand(LagMonitor plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canExecute(sender, command)) {
            return true;
        }

        List<StringBuilder> graphLines = Lists.newArrayListWithExpectedSize(GRAPH_LINES);
        IntStream.rangeClosed(1, GRAPH_LINES)
                .map(i -> GRAPH_WIDTH * 2)
                .mapToObj(StringBuilder::new)
                .forEach(graphLines::add);

        TpsHistoryTask tpsHistoryTask = plugin.getTpsHistoryTask();

        boolean console = true;
        if (sender instanceof Player) {
            console = false;
        }

        float[] lastSeconds = tpsHistoryTask.getMinuteSample().getSamples();
        int position = tpsHistoryTask.getMinuteSample().getCurrentPosition();
        buildGraph(lastSeconds, position, graphLines, console);
        graphLines.stream().map(Object::toString).forEach(sender::sendMessage);

        printAverageHistory(tpsHistoryTask, sender);
        sender.sendMessage(PRIMARY_COLOR + "Current TPS: " + tpsHistoryTask.getLastSample());
        return true;
    }

    private void printAverageHistory(TpsHistoryTask tpsHistoryTask, CommandSender sender) {
        float minuteAverage = tpsHistoryTask.getMinuteSample().getAverage();
        float quarterAverage = tpsHistoryTask.getQuarterSample().getAverage();
        float halfHourAverage = tpsHistoryTask.getHalfHourSample().getAverage();

        DecimalFormat formatter = new DecimalFormat("###.##");
        sender.sendMessage(PRIMARY_COLOR + "Last Samples (1m, 15m, 30m): " + SECONDARY_COLOR
                + formatter.format(minuteAverage)
                + ' ' + formatter.format(quarterAverage)
                + ' ' + formatter.format(halfHourAverage));
    }

    private void buildGraph(float[] lastSeconds, int lastPos, List<StringBuilder> graphLines, boolean console) {
        int index = lastPos;
        //in x-direction
        for (int xPos = 1; xPos < GRAPH_WIDTH; xPos++) {
            index++;
            if (index == lastSeconds.length) {
                index = 0;
            }

            float sampleSecond = lastSeconds[index];
            buildLine(sampleSecond, graphLines, console);
        }
    }

    private void buildLine(float sampleSecond, List<StringBuilder> graphLines, boolean console) {
        ChatColor color = ChatColor.DARK_RED;
        int lines = 0;
        if (sampleSecond > 19.5F) {
            lines = GRAPH_LINES;
            color = ChatColor.DARK_GREEN;
        } else if (sampleSecond > 18.0F) {
            lines = GRAPH_LINES - 1;
            color = ChatColor.GREEN;
        } else if (sampleSecond > 17.0F) {
            lines = GRAPH_LINES - 2;
            color = ChatColor.YELLOW;
        } else if (sampleSecond > 15.0F) {
            lines = GRAPH_LINES - 3;
            color = ChatColor.GOLD;
        } else if (sampleSecond > 12.0F) {
            lines = GRAPH_LINES - 4;
            color = ChatColor.RED;
        }

        //in y-direction in reverse order
        for (int line = GRAPH_LINES - 1; line >= 0; line--) {
            if (lines == 0) {
                graphLines.get(line).append(ChatColor.WHITE).append(console ? EMPTY_CHAR : PLAYER_EMPTY_CHAR);
                continue;
            }

            lines--;
            graphLines.get(line).append(color).append(console ? GRAPH_CHAR : PLAYER_GRAPH_CHAR);
        }
    }
}

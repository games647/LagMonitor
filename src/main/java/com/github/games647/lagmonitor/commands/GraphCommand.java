package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.graphs.ClassesGraph;
import com.github.games647.lagmonitor.graphs.CpuGraph;
import com.github.games647.lagmonitor.graphs.GraphRenderer;
import com.github.games647.lagmonitor.graphs.HeapGraph;
import com.github.games647.lagmonitor.graphs.ThreadsGraph;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class GraphCommand implements TabExecutor {

    private final LagMonitor plugin;

    public GraphCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            //default is heap usage
            GraphRenderer graphRenderer = new HeapGraph();
            if (args.length > 0) {
                String graph = args[0];

                if ("cpu".equalsIgnoreCase(graph)) {
                    graphRenderer = new CpuGraph();
                } else if ("heap".equalsIgnoreCase(graph)) {
                    graphRenderer = new HeapGraph();
                } else if ("threads".equalsIgnoreCase(graph)) {
                    graphRenderer = new ThreadsGraph();
                } else if ("classes".equalsIgnoreCase(graph)) {
                    graphRenderer = new ClassesGraph();
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "Unknown graph type");
                    return true;
                }
            }

            PlayerInventory inventory = player.getInventory();

            MapView mapView = installRenderer(player, graphRenderer);
            //amount=0 makes the item disappear if the user drop or try to use it
            ItemStack mapItem = new ItemStack(Material.MAP, 0, mapView.getId());
            inventory.addItem(mapItem);

            sender.sendMessage(ChatColor.DARK_GREEN + "You received a map with the graph");
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Not implemented for the console");
            //todo: add a textual graph view for the console
        }

        return true;
    }

    private MapView installRenderer(Player player, GraphRenderer graphType) {
        MapView mapView = Bukkit.createMap(player.getWorld());
        for (MapRenderer mapRenderer : mapView.getRenderers()) {
            mapView.removeRenderer(mapRenderer);
        }

        mapView.addRenderer(graphType);
        return mapView;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = Lists.newArrayListWithExpectedSize(4);

        if (args.length != 1) {
            return Collections.emptyList();
        }

        String lastArg = args[args.length - 1];
        if ("cpu".startsWith(lastArg)) {
            result.add("cpu");
        }

        if ("heap".startsWith(lastArg)) {
            result.add("heap");
        }

        if ("threads".startsWith(lastArg)) {
            result.add("threads");
        }

        if ("classes".startsWith(lastArg)) {
            result.add("classes");
        }

        Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
        return result;
    }
}

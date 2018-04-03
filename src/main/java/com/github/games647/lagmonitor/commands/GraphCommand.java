package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.graphs.ClassesGraph;
import com.github.games647.lagmonitor.graphs.CombinedGraph;
import com.github.games647.lagmonitor.graphs.CpuGraph;
import com.github.games647.lagmonitor.graphs.GraphRenderer;
import com.github.games647.lagmonitor.graphs.HeapGraph;
import com.github.games647.lagmonitor.graphs.ThreadsGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;

import static java.util.stream.Collectors.toList;

public class GraphCommand extends LagCommand implements TabExecutor {

    private static final int MAX_COMBINED = 4;

    private final Map<String, GraphRenderer> graphTypes = new HashMap<>();

    public GraphCommand(LagMonitor plugin) {
        super(plugin);

        graphTypes.put("classes", new ClassesGraph());
        graphTypes.put("cpu", new CpuGraph(plugin, plugin.getNativeData()));
        graphTypes.put("heap", new HeapGraph());
        graphTypes.put("threads", new ThreadsGraph());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canExecute(sender, command)) {
            sendError(sender, "Not whitelisted");
            return true;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length > 0) {
                if (args.length > 1) {
                    buildCombinedGraph(player, args);
                } else {
                    String graph = args[0];
                    GraphRenderer renderer = graphTypes.get(graph);
                    if (renderer == null) {
                        sendError(sender, "Unknown graph type");
                    } else {
                        giveMap(player, installRenderer(player, renderer));
                    }
                }

                return true;
            }

            //default is heap usage
            GraphRenderer graphRenderer = graphTypes.get("heap");
            MapView mapView = installRenderer(player, graphRenderer);
            giveMap(player, mapView);
        } else {
            sendError(sender, "Not implemented for the console");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }

        String lastArg = args[args.length - 1];
        return graphTypes.keySet().stream()
                .filter(type -> type.startsWith(lastArg))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(toList());
    }

    private void buildCombinedGraph(Player player, String[] args) {
        List<GraphRenderer> renderers = new ArrayList<>();
        for (String arg : args) {
            GraphRenderer renderer = graphTypes.get(arg);
            if (renderer == null) {
                sendError(player, "Unknown graph type " + arg);
                return;
            }

            renderers.add(renderer);
        }

        if (renderers.size() > MAX_COMBINED) {
            sendError(player, "Too many graphs");
        } else {
            CombinedGraph combinedGraph = new CombinedGraph(renderers.toArray(new GraphRenderer[renderers.size()]));
            MapView view = installRenderer(player, combinedGraph);
            giveMap(player, view);
        }
    }

    private void giveMap(Player player, MapView mapView) {
        PlayerInventory inventory = player.getInventory();

        ItemStack mapItem = new ItemStack(Material.MAP, 1, mapView.getId());
        inventory.addItem(mapItem);

        player.sendMessage(ChatColor.DARK_GREEN + "You received a map with the graph");
    }

    private MapView installRenderer(Player player, GraphRenderer graphType) {
        MapView mapView = Bukkit.createMap(player.getWorld());
        mapView.getRenderers().forEach(mapView::removeRenderer);

        mapView.addRenderer(graphType);
        return mapView;
    }
}

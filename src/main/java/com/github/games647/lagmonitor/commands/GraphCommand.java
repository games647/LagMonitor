package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.LagMonitor;
import com.github.games647.lagmonitor.graphs.ClassesGraph;
import com.github.games647.lagmonitor.graphs.CombinedGraph;
import com.github.games647.lagmonitor.graphs.CpuGraph;
import com.github.games647.lagmonitor.graphs.GraphRenderer;
import com.github.games647.lagmonitor.graphs.HeapGraph;
import com.github.games647.lagmonitor.graphs.ThreadsGraph;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
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
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class GraphCommand implements TabExecutor {

    private final LagMonitor plugin;
    private final Map<String, GraphRenderer> graphTypes = Maps.newHashMap();
    private int MAX_COMBINED;

    public GraphCommand(LagMonitor plugin) {
        this.plugin = plugin;

        graphTypes.put("classes", new ClassesGraph());
        graphTypes.put("cpu", new CpuGraph());
        graphTypes.put("heap", new HeapGraph());
        graphTypes.put("threads", new ThreadsGraph());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length > 0) {
                if (args.length > 2) {
                    buildCombinedGraph(player, args);
                } else {
                    String graph = args[0];
                    GraphRenderer renderer = graphTypes.get(graph);
                    if (renderer == null) {
                        sender.sendMessage(ChatColor.DARK_RED + "Unknown graph type");
                    } else {
                        installRenderer(player, renderer);
                    }
                }

                return true;
            }

            PlayerInventory inventory = player.getInventory();

            //default is heap usage
            GraphRenderer graphRenderer = graphTypes.get("heap");

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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = Lists.newArrayListWithExpectedSize(4);

        if (args.length != 1) {
            return Collections.emptyList();
        }

        String lastArg = args[args.length - 1];
        for (String type : graphTypes.keySet()) {
            if (type.startsWith(lastArg)) {
                result.add(type);
            }
        }

        Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
        return result;
    }

    private void buildCombinedGraph(Player player, String[] args) {
        List<GraphRenderer> renderers = Lists.newArrayList();
        for (String arg : args) {
            GraphRenderer renderer = graphTypes.get(arg);
            if (renderer == null) {
                player.sendMessage(ChatColor.DARK_RED + "Unknown graph type " + arg);
                return;
            }

            renderers.add(renderer);
        }

        if (renderers.size() > MAX_COMBINED) {
            player.sendMessage(ChatColor.DARK_RED + "Too many graphs");
        } else {
            installRenderer(player, new CombinedGraph(renderers.toArray(new GraphRenderer[renderers.size()])));
        }
    }

    private MapView installRenderer(Player player, GraphRenderer graphType) {
        MapView mapView = Bukkit.createMap(player.getWorld());
        for (MapRenderer mapRenderer : mapView.getRenderers()) {
            mapView.removeRenderer(mapRenderer);
        }

        mapView.addRenderer(graphType);
        return mapView;
    }
}

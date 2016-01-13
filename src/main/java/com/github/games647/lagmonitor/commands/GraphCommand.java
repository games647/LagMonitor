package com.github.games647.lagmonitor.commands;

import com.github.games647.lagmonitor.GraphRenderer;
import com.github.games647.lagmonitor.LagMonitor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class GraphCommand implements CommandExecutor {

    private final LagMonitor plugin;

    public GraphCommand(LagMonitor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerInventory inventory = player.getInventory();

            MapView mapView = installRenderer(player);
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

    private MapView installRenderer(Player player) {
        MapView mapView = Bukkit.createMap(player.getWorld());
        for (MapRenderer mapRenderer : mapView.getRenderers()) {
            mapView.removeRenderer(mapRenderer);
        }

        mapView.addRenderer(new GraphRenderer());
        return mapView;
    }
}

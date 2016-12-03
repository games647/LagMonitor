package com.github.games647.lagmonitor.listeners;

import com.github.games647.lagmonitor.graphs.GraphRenderer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;

public class GraphListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent clickEvent) {
        Player player = clickEvent.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack mainHandItem = inventory.getItemInMainHand();
        if (isOurGraph(mainHandItem)) {
            inventory.setItemInMainHand(new ItemStack(Material.AIR));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDrop(PlayerDropItemEvent dropItemEvent) {
        ItemStack mapItem = dropItemEvent.getItemDrop().getItemStack();
        if (isOurGraph(mapItem)) {
            dropItemEvent.getItemDrop().setItemStack(new ItemStack(Material.AIR));
        }
    }

    private boolean isOurGraph(ItemStack mapItem) {
        if (mapItem != null && mapItem.getType() != Material.MAP) {
            return false;
        }

        short mapId = mapItem.getDurability();
        MapView map = Bukkit.getMap(mapId);
        return map != null && map.getRenderers().stream()
                .anyMatch(renderer -> renderer instanceof GraphRenderer);
    }
}

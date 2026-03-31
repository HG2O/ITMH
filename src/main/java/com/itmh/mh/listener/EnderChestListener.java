package com.itmh.mh.listener;

import com.itmh.mh.ItemFinderPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnderChestListener implements Listener {

    private final ItemFinderPlugin plugin;
    private static final Map<UUID, ItemStack[]> cache = new HashMap<>();

    public EnderChestListener(ItemFinderPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        cache.put(p.getUniqueId(), p.getEnderChest().getContents().clone());
    }

    public static Map<UUID, ItemStack[]> getEnderChestCache() {
        return cache;
    }
}
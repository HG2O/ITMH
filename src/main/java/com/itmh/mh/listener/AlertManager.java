package com.itmh.mh.listener;

import com.itmh.mh.ItemFinderPlugin;
import com.itmh.mh.model.ItemSearchResult;
import com.itmh.mh.search.SearchEngine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class AlertManager {

    private final ItemFinderPlugin plugin;

    public AlertManager(ItemFinderPlugin plugin) {
        this.plugin = plugin;
    }

    public void start(int seconds) {
        long ticks = Math.max(20L, seconds * 20L);

        new BukkitRunnable() {
            @Override
            public void run() {
                List<String> watchlist = plugin.getConfig().getStringList("alert-items");
                if (watchlist.isEmpty()) return;

                List<SearchEngine.InventorySnapshot> snapshots = plugin.getSearchEngine().collectSnapshots();

                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    for (String item : watchlist) {
                        List<ItemSearchResult> found = plugin.getSearchEngine().search(snapshots, item.toLowerCase());
                        if (found.isEmpty()) continue;

                        int total = found.stream().mapToInt(ItemSearchResult::getCount).sum();

                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            String msg = ChatColor.RED + "[ItemFinder] " + ChatColor.YELLOW + total + "x "
                                    + ChatColor.WHITE + item.toUpperCase() + ChatColor.YELLOW + " detected on the server!";
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (p.hasPermission("itemfinder.alerts")) p.sendMessage(msg);
                            }
                            plugin.getLogger().warning("[ALERT] " + total + "x " + item.toUpperCase() + " detected!");
                        });
                    }
                });
            }
        }.runTaskTimer(plugin, ticks, ticks);
    }
}
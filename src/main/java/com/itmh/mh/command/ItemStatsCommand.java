package com.itmh.mh.command;

import com.itmh.mh.ItemFinderPlugin;
import com.itmh.mh.search.SearchEngine;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemStatsCommand implements CommandExecutor {

    private final ItemFinderPlugin plugin;

    public ItemStatsCommand(ItemFinderPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("itemfinder.stats")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        sender.sendMessage(ChatColor.AQUA + "Computing server item stats...");
        List<SearchEngine.InventorySnapshot> snapshots = plugin.getSearchEngine().collectSnapshots();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<String, Integer> counts = new HashMap<>();
            for (SearchEngine.InventorySnapshot snap : snapshots) {
                if (snap.contents() == null) continue;
                for (ItemStack item : snap.contents()) {
                    if (item == null || item.getType().isAir()) continue;
                    counts.merge(item.getType().name(), item.getAmount(), Integer::sum);
                }
            }

            List<Map.Entry<String, Integer>> sorted = new ArrayList<>(counts.entrySet());
            sorted.sort((a, b) -> b.getValue() - a.getValue());
            List<Map.Entry<String, Integer>> top10 = sorted.subList(0, Math.min(10, sorted.size()));

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (counts.isEmpty()) { sender.sendMessage(ChatColor.YELLOW + "No items found."); return; }
                sender.sendMessage(ChatColor.AQUA + "--- Top items on the server ---");
                int i = 1;
                for (Map.Entry<String, Integer> e : top10) {
                    String medal = i == 1 ? "🥇" : i == 2 ? "🥈" : i == 3 ? "🥉" : ChatColor.GRAY + "#" + i;
                    sender.sendMessage(medal + " " + ChatColor.WHITE + e.getKey()
                            + ChatColor.GRAY + " x" + ChatColor.YELLOW + e.getValue());
                    i++;
                }
                sender.sendMessage(ChatColor.GRAY + "Unique types: " + ChatColor.WHITE + counts.size());
            });
        });
        return true;
    }
}
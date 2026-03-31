package com.itmh.mh.command;

import com.itmh.mh.ItemFinderPlugin;
import com.itmh.mh.gui.ResultsGUI;
import com.itmh.mh.model.ItemSearchResult;
import com.itmh.mh.search.SearchEngine;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class FindItemCommand implements CommandExecutor, TabCompleter {

    private final ItemFinderPlugin plugin;

    public FindItemCommand(ItemFinderPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (!player.hasPermission("itemfinder.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /finditem <name or material>");
            return true;
        }

        if (plugin.getCooldowns().isOnCooldown(player)) {
            int left = plugin.getCooldowns().getRemaining(player);
            player.sendMessage(ChatColor.RED + "Wait " + ChatColor.YELLOW + left + "s" + ChatColor.RED + " before searching again.");
            return true;
        }
        plugin.getCooldowns().set(player);

        String query = String.join(" ", args);
        player.sendMessage(ChatColor.AQUA + "Searching for " + ChatColor.WHITE + "\"" + query + "\"...");

        doSearch(plugin, player, query);
        return true;
    }

    public static void doSearch(ItemFinderPlugin plugin, Player player, String query) {
        List<SearchEngine.InventorySnapshot> snapshots = plugin.getSearchEngine().collectSnapshots();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            List<ItemSearchResult> results = plugin.getSearchEngine().search(snapshots, query);

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (results.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No items found for " + ChatColor.WHITE + "\"" + query + "\"");
                    return;
                }
                player.sendMessage(ChatColor.RED.toString() + "No items found for " + ChatColor.WHITE.toString() + "\"" + query + "\"");                ResultsGUI.open(player, results, query, 0);
            });
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1)
            return Arrays.asList("diamond_sword", "netherite_sword", "diamond_pickaxe", "sharpness", "protection");
        return List.of();
    }
}
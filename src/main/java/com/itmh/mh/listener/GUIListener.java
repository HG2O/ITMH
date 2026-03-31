package com.itmh.mh.listener;

import com.itmh.mh.ItemFinderPlugin;
import com.itmh.mh.command.FindItemCommand;
import com.itmh.mh.gui.ResultsGUI;
import com.itmh.mh.gui.ResultsGUISession;
import com.itmh.mh.model.ItemSearchResult;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GUIListener implements Listener {

    private final ItemFinderPlugin plugin;

    public GUIListener(ItemFinderPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.contains(ResultsGUI.TITLE_KEY)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        List<ItemSearchResult> results = ResultsGUISession.getResults(player);
        String query = ResultsGUISession.getQuery(player);
        int page = ResultsGUISession.getPage(player);
        if (results == null || query == null) return;

        int slot = event.getSlot();
        int pages = (int) Math.ceil(results.size() / 45.0);

        if (slot == 45 && page > 0) { ResultsGUI.open(player, results, query, page - 1); return; }
        if (slot == 53 && page < pages - 1) { ResultsGUI.open(player, results, query, page + 1); return; }

        if (slot == 47) {
            player.sendMessage(ChatColor.AQUA + "Refreshing search for " + ChatColor.WHITE + "\"" + query + "\"...");
            player.closeInventory();
            FindItemCommand.doSearch(plugin, player, query);
            return;
        }

        if (slot < 45) {
            int idx = page * 45 + slot;
            if (idx >= results.size()) return;
            ItemSearchResult result = results.get(idx);
            Location loc = result.getLocation();
            if (loc == null) return;

            ItemSearchResult.LocationType t = result.getLocationType();
            if (t == ItemSearchResult.LocationType.PLAYER_INVENTORY
                    || t == ItemSearchResult.LocationType.PLAYER_ENDERCHEST) return;

            sendCoords(player, result, loc);
        }
    }

    private void sendCoords(Player player, ItemSearchResult result, Location loc) {
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        String cmd = "/tp " + x + " " + y + " " + z;

        TextComponent line = new TextComponent(ChatColor.GRAY + result.getOwnerOrCoords() + "  ");

        TextComponent tp = new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "[Teleport]");
        tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
        tp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ChatColor.GRAY + cmd).create()));

        TextComponent copy = new TextComponent("  " + ChatColor.YELLOW + "[Copy]");
        copy.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd));
        copy.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ChatColor.GRAY + "Copy to chat bar").create()));

        player.spigot().sendMessage(line, tp, copy);
    }
}
package com.itmh.mh.gui;

import com.itmh.mh.model.ItemSearchResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ResultsGUI {

    private static final int PAGE_SIZE = 45;
    public static final String TITLE_KEY = "Recherche";

    public static void open(Player player, List<ItemSearchResult> results, String query, int page) {
        int totalPages = Math.max(1, (int) Math.ceil(results.size() / (double) PAGE_SIZE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        String title = ChatColor.DARK_AQUA + "Search: " + ChatColor.WHITE + "\"" + query + "\""
                + ChatColor.GRAY + " (" + results.size() + ")";

        Inventory inv = Bukkit.createInventory(null, 54, title);

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, results.size());
        for (int i = start; i < end; i++)
            inv.setItem(i - start, buildItem(results.get(i), i + 1));

        buildNavBar(inv, page, totalPages, query, results.size());
        player.openInventory(inv);
        ResultsGUISession.store(player, results, query, page);
    }

    private static ItemStack buildItem(ItemSearchResult result, int idx) {
        ItemStack display = result.getItem().clone();
        ItemMeta meta = display.getItemMeta();

        String icon = switch (result.getLocationType()) {
            case PLAYER_INVENTORY  -> "🎒";
            case PLAYER_ENDERCHEST -> "🟣";
            case CHEST             -> "📦";
            case BARREL            -> "🛢";
            case SHULKER_BOX       -> "🟪";
            case OTHER_CONTAINER   -> "🗃";
        };
        String label = switch (result.getLocationType()) {
            case PLAYER_INVENTORY  -> "Inventory";
            case PLAYER_ENDERCHEST -> "Ender Chest";
            case CHEST             -> "Chest";
            case BARREL            -> "Barrel";
            case SHULKER_BOX       -> "Shulker Box";
            case OTHER_CONTAINER   -> "Container";
        };

        meta.setDisplayName(ChatColor.YELLOW + "#" + idx + " " + ChatColor.AQUA + icon + " " + label);

        List<String> lore = new ArrayList<>(meta.hasLore() && meta.getLore() != null ? meta.getLore() : List.of());
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "----------------");
        lore.add(ChatColor.GRAY + "Location: " + ChatColor.WHITE + result.getOwnerOrCoords());
        lore.add(ChatColor.GRAY + "Amount: " + ChatColor.WHITE + result.getCount());
        if (result.getLocation() != null) {
            lore.add(ChatColor.GRAY + "World: " + ChatColor.WHITE + result.getLocation().getWorld().getName());
            if (result.getLocationType() != ItemSearchResult.LocationType.PLAYER_INVENTORY
                    && result.getLocationType() != ItemSearchResult.LocationType.PLAYER_ENDERCHEST)
                lore.add(ChatColor.DARK_AQUA + "Click for coordinates");
        }

        meta.setLore(lore);
        display.setItemMeta(meta);
        return display;
    }

    private static void buildNavBar(Inventory inv, int page, int totalPages, String query, int total) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        fm.setDisplayName(" ");
        filler.setItemMeta(fm);
        for (int i = 45; i < 54; i++) inv.setItem(i, filler);

        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta m = prev.getItemMeta();
            m.setDisplayName(ChatColor.GREEN + "< Previous");
            m.setLore(List.of(ChatColor.GRAY + "Page " + page + "/" + totalPages));
            prev.setItemMeta(m);
            inv.setItem(45, prev);
        }

        ItemStack refresh = new ItemStack(Material.CLOCK);
        ItemMeta rm = refresh.getItemMeta();
        rm.setDisplayName(ChatColor.YELLOW + "Refresh");
        rm.setLore(List.of(ChatColor.GRAY + "Re-run the search"));
        refresh.setItemMeta(rm);
        inv.setItem(47, refresh);

        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName(ChatColor.YELLOW + "Page " + (page + 1) + "/" + totalPages);
        inv.setItem(49, info);

        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta m = next.getItemMeta();
            m.setDisplayName(ChatColor.GREEN + "Next >");
            m.setLore(List.of(ChatColor.GRAY + "Page " + (page + 2) + "/" + totalPages));
            next.setItemMeta(m);
            inv.setItem(53, next);
        }
    }
}
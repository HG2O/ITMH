package com.itmh.mh.util;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemMatcher {

    public static boolean matches(ItemStack item, String query) {
        if (item == null || item.getType().isAir()) return false;
        String q = query.toLowerCase().trim();

        if (item.getType().name().toLowerCase().contains(q)) return true;

        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();

        if (meta.hasDisplayName()) {
            String name = ChatColor.stripColor(meta.getDisplayName());
            if (name != null && name.toLowerCase().contains(q)) return true;
        }

        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                for (String line : lore) {
                    String plain = ChatColor.stripColor(line);
                    if (plain != null && plain.toLowerCase().contains(q)) return true;
                }
            }
        }

        for (Enchantment e : meta.getEnchants().keySet())
            if (e.getKey().getKey().toLowerCase().contains(q)) return true;

        if (meta instanceof EnchantmentStorageMeta esm)
            for (Enchantment e : esm.getStoredEnchants().keySet())
                if (e.getKey().getKey().toLowerCase().contains(q)) return true;

        return false;
    }
}
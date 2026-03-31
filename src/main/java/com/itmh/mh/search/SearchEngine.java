package com.itmh.mh.search;

import com.itmh.mh.ItemFinderPlugin;
import com.itmh.mh.listener.EnderChestListener;
import com.itmh.mh.model.ItemSearchResult;
import com.itmh.mh.util.ItemMatcher;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.*;

public class SearchEngine {

    private final ItemFinderPlugin plugin;

    public SearchEngine(ItemFinderPlugin plugin) {
        this.plugin = plugin;
    }

    public record InventorySnapshot(ItemStack[] contents, ItemSearchResult.LocationType type,
                                    String label, Location location) {}

    public List<InventorySnapshot> collectSnapshots() {
        List<InventorySnapshot> snapshots = new ArrayList<>();
        List<String> ignoredWorlds = plugin.getConfig().getStringList("disabled-worlds");

        for (Player p : Bukkit.getOnlinePlayers()) {
            snapshots.add(new InventorySnapshot(p.getInventory().getContents().clone(),
                    ItemSearchResult.LocationType.PLAYER_INVENTORY, p.getName(), p.getLocation()));
            snapshots.add(new InventorySnapshot(p.getEnderChest().getContents().clone(),
                    ItemSearchResult.LocationType.PLAYER_ENDERCHEST, p.getName(), p.getLocation()));
        }

        Map<UUID, ItemStack[]> cache = EnderChestListener.getEnderChestCache();
        for (Map.Entry<UUID, ItemStack[]> entry : cache.entrySet()) {
            if (Bukkit.getPlayer(entry.getKey()) != null) continue;
            String name = Optional.ofNullable(Bukkit.getOfflinePlayer(entry.getKey()).getName())
                    .orElse(entry.getKey().toString());
            snapshots.add(new InventorySnapshot(entry.getValue().clone(),
                    ItemSearchResult.LocationType.PLAYER_ENDERCHEST, name + " (offline)", null));
        }

        for (World world : Bukkit.getWorlds()) {
            if (ignoredWorlds.contains(world.getName())) continue;
            for (Chunk chunk : world.getLoadedChunks()) {
                for (BlockState state : chunk.getTileEntities()) {
                    if (!(state instanceof Container container)) continue;
                    Location loc = state.getLocation();
                    ItemSearchResult.LocationType type = switch (state.getType()) {
                        case CHEST, TRAPPED_CHEST -> ItemSearchResult.LocationType.CHEST;
                        case BARREL -> ItemSearchResult.LocationType.BARREL;
                        case SHULKER_BOX, WHITE_SHULKER_BOX, ORANGE_SHULKER_BOX, MAGENTA_SHULKER_BOX,
                             LIGHT_BLUE_SHULKER_BOX, YELLOW_SHULKER_BOX, LIME_SHULKER_BOX,
                             PINK_SHULKER_BOX, GRAY_SHULKER_BOX, LIGHT_GRAY_SHULKER_BOX,
                             CYAN_SHULKER_BOX, PURPLE_SHULKER_BOX, BLUE_SHULKER_BOX,
                             BROWN_SHULKER_BOX, GREEN_SHULKER_BOX, RED_SHULKER_BOX,
                             BLACK_SHULKER_BOX -> ItemSearchResult.LocationType.SHULKER_BOX;
                        default -> ItemSearchResult.LocationType.OTHER_CONTAINER;
                    };
                    snapshots.add(new InventorySnapshot(
                            container.getInventory().getContents().clone(), type, coords(loc), loc));
                }
            }
        }

        return snapshots;
    }

    public List<ItemSearchResult> search(List<InventorySnapshot> snapshots, String query) {
        List<ItemSearchResult> results = new ArrayList<>();
        for (InventorySnapshot snap : snapshots)
            scanInv(snap.contents(), query, snap.type(), snap.label(), snap.location(), results);
        return results;
    }

    private void scanInv(ItemStack[] contents, String query, ItemSearchResult.LocationType type,
                         String label, Location loc, List<ItemSearchResult> out) {
        if (contents == null) return;
        for (ItemStack item : contents) {
            if (item == null || item.getType().isAir()) continue;
            if (item.getItemMeta() instanceof BlockStateMeta bsm
                    && bsm.getBlockState() instanceof Container inner) {
                scanInv(inner.getInventory().getContents(), query,
                        ItemSearchResult.LocationType.SHULKER_BOX, label + " [Shulker]", loc, out);
            }
            if (ItemMatcher.matches(item, query))
                out.add(new ItemSearchResult(item.clone(), type, label, loc, item.getAmount()));
        }
    }

    private String coords(Location loc) {
        return loc.getWorld().getName() + " [" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + "]";
    }
}
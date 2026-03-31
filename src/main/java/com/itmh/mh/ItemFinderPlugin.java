package com.itmh.mh;

import com.itmh.mh.command.FindItemCommand;
import com.itmh.mh.command.ItemAlertCommand;
import com.itmh.mh.command.ItemStatsCommand;
import com.itmh.mh.listener.AlertManager;
import com.itmh.mh.listener.EnderChestListener;
import com.itmh.mh.listener.GUIListener;
import com.itmh.mh.search.SearchEngine;
import com.itmh.mh.util.CooldownManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemFinderPlugin extends JavaPlugin {

    private SearchEngine searchEngine;
    private CooldownManager cooldowns;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        searchEngine = new SearchEngine(this);
        cooldowns    = new CooldownManager(getConfig().getInt("cooldown-seconds", 5));

        FindItemCommand findCmd = new FindItemCommand(this);
        getCommand("finditem").setExecutor(findCmd);
        getCommand("finditem").setTabCompleter(findCmd);

        getCommand("itemstats").setExecutor(new ItemStatsCommand(this));

        ItemAlertCommand alertCmd = new ItemAlertCommand(this);
        getCommand("itemalert").setExecutor(alertCmd);
        getCommand("itemalert").setTabCompleter(alertCmd);

        getServer().getPluginManager().registerEvents(new EnderChestListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        int interval = getConfig().getInt("alert-interval-seconds", 60);
        new AlertManager(this).start(interval);

        getLogger().info("ItemFinder enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ItemFinder disabled.");
    }

    public SearchEngine getSearchEngine()    { return searchEngine; }
    public CooldownManager getCooldowns()    { return cooldowns; }
}
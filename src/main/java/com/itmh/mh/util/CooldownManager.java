package com.itmh.mh.util;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Long> lastUsed = new HashMap<>();
    private final int delay;

    public CooldownManager(int seconds) {
        this.delay = seconds;
    }

    public boolean isOnCooldown(Player p) {
        if (delay <= 0) return false;
        Long last = lastUsed.get(p.getUniqueId());
        if (last == null) return false;
        return (System.currentTimeMillis() - last) < delay * 1000L;
    }

    public int getRemaining(Player p) {
        Long last = lastUsed.get(p.getUniqueId());
        if (last == null) return 0;
        return (int) Math.ceil((delay * 1000L - (System.currentTimeMillis() - last)) / 1000.0);
    }

    public void set(Player p) {
        lastUsed.put(p.getUniqueId(), System.currentTimeMillis());
    }
}
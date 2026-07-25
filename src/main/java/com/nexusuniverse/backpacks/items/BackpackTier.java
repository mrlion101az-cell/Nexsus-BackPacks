package com.nexusuniverse.backpacks.items;

import org.bukkit.ChatColor;

public enum BackpackTier {
    SCOUT(1, 9, "Scout Pack", ChatColor.WHITE),
    TRAILBLAZER(2, 18, "Trailblazer Pack", ChatColor.GREEN),
    VOYAGER(3, 27, "Voyager Pack", ChatColor.AQUA),
    SENTINEL(4, 36, "Sentinel Pack", ChatColor.LIGHT_PURPLE),
    OVERLORD(5, 45, "Overlord Pack", ChatColor.GOLD);

    private final int level;
    private final int slots;
    private final String displayName;
    private final ChatColor color;

    BackpackTier(int level, int slots, String displayName, ChatColor color) {
        this.level = level;
        this.slots = slots;
        this.displayName = displayName;
        this.color = color;
    }

    public int level() {
        return level;
    }

    public int slots() {
        return slots;
    }

    public String coloredName() {
        return color + displayName;
    }

    public BackpackTier next() {
        int nextOrdinal = ordinal() + 1;
        return nextOrdinal < values().length ? values()[nextOrdinal] : null;
    }

    public static BackpackTier byLevel(int level) {
        for (BackpackTier tier : values()) {
            if (tier.level == level) return tier;
        }
        return SCOUT;
    }
}

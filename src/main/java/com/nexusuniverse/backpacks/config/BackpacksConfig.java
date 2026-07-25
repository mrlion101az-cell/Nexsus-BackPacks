package com.nexusuniverse.backpacks.config;

import org.bukkit.plugin.java.JavaPlugin;

public class BackpacksConfig {

    private final JavaPlugin plugin;

    public BackpacksConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public double magnetRadius() {
        return plugin.getConfig().getDouble("magnet.radius", 4.0);
    }

    public long magnetTickInterval() {
        return plugin.getConfig().getLong("magnet.tick-interval", 10);
    }

    public int coresPerTierUpgrade() {
        return plugin.getConfig().getInt("upgrade.cores-per-tier", 1);
    }

    public void reload() {
        plugin.reloadConfig();
    }
}

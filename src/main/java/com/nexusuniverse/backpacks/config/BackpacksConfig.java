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

    public boolean hintEnabled() {
        return plugin.getConfig().getBoolean("hint.enabled", true);
    }

    public long hintCooldownSeconds() {
        return plugin.getConfig().getLong("hint.cooldown-seconds", 300);
    }

    public String hintMessageJava() {
        return plugin.getConfig().getString("hint.message-java",
                "&b&lBackpack &7-> Right-click to open. Sneak + right-click with a core in your off-hand to upgrade it.");
    }

    public String hintMessageBedrock() {
        return plugin.getConfig().getString("hint.message-bedrock",
                "&b&lBackpack &7-> Press Use/Interact to open. Crouch + Use while holding a core in your other hand to upgrade it.");
    }

    public void reload() {
        plugin.reloadConfig();
    }
}

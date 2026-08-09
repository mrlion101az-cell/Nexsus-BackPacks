package com.nexusuniverse.backpacks.hint;

import com.nexusuniverse.backpacks.config.BackpacksConfig;
import com.nexusuniverse.backpacks.items.BackpackFactory;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fires whenever a player switches their held hotbar slot to a backpack
 * (equips it "in hand") and sends them a short usage tip -- worded
 * differently for Java Edition players vs. Bedrock/console players
 * connected through Geyser, since the two describe "right-click" and
 * "sneak" differently. A per-player cooldown stops it from re-firing
 * every time someone flicks past the slot.
 */
public class BackpackHintListener implements Listener {

    private final BackpackFactory factory;
    private final BackpacksConfig config;
    private final Map<UUID, Long> lastHintAt = new HashMap<>();

    public BackpackHintListener(BackpackFactory factory, BackpacksConfig config) {
        this.factory = factory;
        this.config = config;
    }

    @EventHandler
    public void onHeldItemChange(PlayerItemHeldEvent event) {
        if (!config.hintEnabled()) return;

        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (!factory.isBackpack(newItem)) return;

        long now = System.currentTimeMillis();
        long cooldownMs = config.hintCooldownSeconds() * 1000L;
        Long last = lastHintAt.get(player.getUniqueId());
        if (last != null && now - last < cooldownMs) return;
        lastHintAt.put(player.getUniqueId(), now);

        boolean bedrock = PlatformUtil.isBedrockPlayer(player);
        String message = bedrock ? config.hintMessageBedrock() : config.hintMessageJava();
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastHintAt.remove(event.getPlayer().getUniqueId());
    }
}

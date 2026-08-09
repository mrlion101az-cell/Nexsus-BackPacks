package com.nexusuniverse.backpacks.ability;

import com.nexusuniverse.backpacks.config.BackpacksConfig;
import com.nexusuniverse.backpacks.items.BackpackFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MagnetTask implements Runnable {

    private final BackpackFactory factory;
    private final BackpacksConfig config;

    public MagnetTask(BackpackFactory factory, BackpacksConfig config) {
        this.factory = factory;
        this.config = config;
    }

    @Override
    public void run() {
        double radius = config.magnetRadius();

        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack backpack = findMagnetBackpack(player);
            if (backpack == null) continue;

            ItemStack[] contents = factory.readContents(backpack);
            boolean changed = false;

            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (!(entity instanceof Item groundItem)) continue;
                if (groundItem.getPickupDelay() > 0) continue; // still in its just-dropped delay window

                ItemStack stack = groundItem.getItemStack();
                ItemStack remainder = tryAdd(contents, stack);

                if (remainder == null) {
                    groundItem.remove();
                    changed = true;
                } else if (remainder.getAmount() < stack.getAmount()) {
                    groundItem.setItemStack(remainder);
                    changed = true;
                }
            }

            if (changed) {
                factory.writeContents(backpack, contents);
            }
        }
    }

    private ItemStack findMagnetBackpack(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (factory.isBackpack(item) && factory.hasMagnet(item)) return item;
        }
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (factory.isBackpack(offHand) && factory.hasMagnet(offHand)) return offHand;
        return null;
    }

    /**
     * Merges `stack` into `contents` in place (topping up matching stacks first, then filling empty
     * slots). Returns the leftover ItemStack if it didn't fully fit, or null if it was fully absorbed.
     */
    private ItemStack tryAdd(ItemStack[] contents, ItemStack stack) {
        ItemStack remaining = stack.clone();

        for (int i = 0; i < contents.length && remaining.getAmount() > 0; i++) {
            ItemStack existing = contents[i];
            if (existing == null || !existing.isSimilar(remaining)) continue;
            int space = existing.getMaxStackSize() - existing.getAmount();
            if (space <= 0) continue;
            int move = Math.min(space, remaining.getAmount());
            existing.setAmount(existing.getAmount() + move);
            remaining.setAmount(remaining.getAmount() - move);
        }

        for (int i = 0; i < contents.length && remaining.getAmount() > 0; i++) {
            if (contents[i] != null) continue;
            int move = Math.min(remaining.getMaxStackSize(), remaining.getAmount());
            ItemStack placed = remaining.clone();
            placed.setAmount(move);
            contents[i] = placed;
            remaining.setAmount(remaining.getAmount() - move);
        }

        return remaining.getAmount() > 0 ? remaining : null;
    }
}

package com.nexusuniverse.backpacks.gui;

import com.nexusuniverse.backpacks.config.BackpacksConfig;
import com.nexusuniverse.backpacks.items.BackpackFactory;
import com.nexusuniverse.backpacks.items.BackpackTier;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BackpackListener implements Listener {

    private final BackpackFactory factory;
    private final BackpackGUI gui;
    private final BackpacksConfig config;

    /** Tracks which hotbar/inventory slot each player's currently-open backpack was held in, so close() writes back to the right item. */
    private final Map<UUID, Integer> openSlotByPlayer = new HashMap<>();

    public BackpackListener(BackpackFactory factory, BackpackGUI gui, BackpacksConfig config) {
        this.factory = factory;
        this.gui = gui;
        this.config = config;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return; // avoid double-firing once for each hand

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        String mainId = factory.idOf(mainHand);

        if (player.isSneaking() && isCore(mainId) && factory.isBackpack(offHand)) {
            event.setCancelled(true);
            applyCore(player, mainId, mainHand, offHand);
            return;
        }

        if (factory.isBackpack(mainHand)) {
            event.setCancelled(true);
            openSlotByPlayer.put(player.getUniqueId(), player.getInventory().getHeldItemSlot());
            gui.open(player, mainHand);
        }
    }

    private boolean isCore(String id) {
        return BackpackFactory.ID_UPGRADE_CORE.equals(id)
                || BackpackFactory.ID_MAGNET_CORE.equals(id)
                || BackpackFactory.ID_SORTING_CORE.equals(id);
    }

    private void applyCore(Player player, String coreId, ItemStack core, ItemStack backpack) {
        switch (coreId) {
            case BackpackFactory.ID_UPGRADE_CORE -> applyUpgrade(player, core, backpack);
            case BackpackFactory.ID_MAGNET_CORE -> applyAbility(player, core, backpack, true);
            case BackpackFactory.ID_SORTING_CORE -> applyAbility(player, core, backpack, false);
        }
    }

    private void applyUpgrade(Player player, ItemStack core, ItemStack backpack) {
        BackpackTier current = factory.tierOf(backpack);
        BackpackTier next = current.next();
        if (next == null) {
            player.sendMessage(ChatColor.YELLOW + "This backpack is already at the maximum tier.");
            return;
        }

        int needed = config.coresPerTierUpgrade();
        if (core.getAmount() < needed) {
            player.sendMessage(ChatColor.RED + "Need " + needed + "x Backpack Upgrade Core for this tier -- you have " + core.getAmount() + ".");
            return;
        }

        core.setAmount(core.getAmount() - needed);
        factory.setTier(backpack, next);
        factory.resizeContents(backpack, next);
        factory.refreshDisplay(backpack);
        player.sendMessage(ChatColor.GREEN + "Backpack upgraded to " + next.coloredName() + ChatColor.GREEN + "! ("
                + next.slots() + " slots)");
    }

    private void applyAbility(Player player, ItemStack core, ItemStack backpack, boolean magnet) {
        boolean already = magnet ? factory.hasMagnet(backpack) : factory.hasSort(backpack);
        if (already) {
            player.sendMessage(ChatColor.YELLOW + "This backpack already has that ability installed.");
            return;
        }

        core.setAmount(core.getAmount() - 1);
        if (magnet) {
            factory.setMagnet(backpack, true);
        } else {
            factory.setSort(backpack, true);
        }
        factory.refreshDisplay(backpack);
        player.sendMessage(ChatColor.GREEN + (magnet ? "Magnet" : "Auto-Sort") + " installed on your backpack!");
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BackpackHolder)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // A backpack can never be stored inside a backpack -- avoids nesting weirdness and dupe edge cases.
        boolean topInventoryInvolved = event.getClickedInventory() != null
                && event.getClickedInventory().getHolder() instanceof BackpackHolder;
        if (topInventoryInvolved && (factory.isBackpack(event.getCursor()) || factory.isBackpack(event.getCurrentItem()))) {
            event.setCancelled(true);
            return;
        }

        // Prevent moving/removing the open backpack itself out of the slot it's being edited from.
        Integer openSlot = openSlotByPlayer.get(player.getUniqueId());
        if (openSlot != null && event.getClickedInventory() == player.getInventory() && event.getSlot() == openSlot) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof BackpackHolder)) return;
        if (factory.isBackpack(event.getOldCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof BackpackHolder)) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        Integer slot = openSlotByPlayer.remove(player.getUniqueId());
        ItemStack[] guiContents = event.getInventory().getContents();

        ItemStack backpack = slot != null ? player.getInventory().getItem(slot) : null;
        if (backpack == null || !factory.isBackpack(backpack)) {
            // The backpack moved or vanished while open (edge case) -- don't silently lose items.
            for (ItemStack item : guiContents) {
                if (item != null) player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            player.sendMessage(ChatColor.RED + "Couldn't find your backpack to save into -- contents were dropped on the ground instead.");
            return;
        }

        if (factory.hasSort(backpack)) {
            sortInPlace(guiContents);
        }
        factory.writeContents(backpack, guiContents);
    }

    private void sortInPlace(ItemStack[] contents) {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : contents) {
            if (item != null) items.add(item);
        }
        items.sort(Comparator.comparing(item -> item.getType().name()));

        Arrays.fill(contents, null);
        for (int i = 0; i < items.size() && i < contents.length; i++) {
            contents[i] = items.get(i);
        }
    }
}

package com.nexusuniverse.backpacks.gui;

import com.nexusuniverse.backpacks.items.BackpackFactory;
import com.nexusuniverse.backpacks.items.BackpackTier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BackpackGUI {

    private final BackpackFactory factory;

    public BackpackGUI(BackpackFactory factory) {
        this.factory = factory;
    }

    public void open(Player player, ItemStack backpack) {
        BackpackTier tier = factory.tierOf(backpack);
        BackpackHolder holder = new BackpackHolder();
        Inventory inventory = Bukkit.createInventory(holder, tier.slots(), tier.coloredName());
        holder.setInventory(inventory);

        ItemStack[] contents = factory.readContents(backpack);
        inventory.setContents(contents);

        player.openInventory(inventory);
    }
}

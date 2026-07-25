package com.nexusuniverse.backpacks.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * A backpack is a fully self-contained physical item: its tier, abilities,
 * and contents all live in the item's own PersistentDataContainer, encoded
 * with ItemStack's NBT (de)serialization helpers. That means a backpack
 * keeps its contents through trades, drops, chests, and item-frame display
 * with zero extra plugin-side storage or lookup -- the item IS the save file.
 */
public class BackpackFactory {

    public static final String ID_BACKPACK = "backpack";
    public static final String ID_UPGRADE_CORE = "upgrade_core";
    public static final String ID_MAGNET_CORE = "magnet_core";
    public static final String ID_SORTING_CORE = "sorting_core";

    private final NamespacedKey idKey;
    private final NamespacedKey tierKey;
    private final NamespacedKey contentsKey;
    private final NamespacedKey magnetKey;
    private final NamespacedKey sortKey;

    public BackpackFactory(Plugin plugin) {
        this.idKey = new NamespacedKey(plugin, "item_id");
        this.tierKey = new NamespacedKey(plugin, "tier");
        this.contentsKey = new NamespacedKey(plugin, "contents");
        this.magnetKey = new NamespacedKey(plugin, "magnet_enabled");
        this.sortKey = new NamespacedKey(plugin, "sort_enabled");
    }

    // --- Generic id tagging, same pattern used across the Nexus plugin family ---

    public String idOf(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR || !stack.hasItemMeta()) return null;
        return stack.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
    }

    public boolean isBackpack(ItemStack stack) {
        return ID_BACKPACK.equals(idOf(stack));
    }

    // --- Backpack creation and tier/ability state ---

    public ItemStack createBackpack(BackpackTier tier) {
        ItemStack stack = new ItemStack(Material.CHEST);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, ID_BACKPACK);
        meta.getPersistentDataContainer().set(tierKey, PersistentDataType.INTEGER, tier.level());
        meta.getPersistentDataContainer().set(contentsKey, PersistentDataType.BYTE_ARRAY, serializeContents(new ItemStack[tier.slots()]));
        stack.setItemMeta(meta);
        refreshDisplay(stack);
        return stack;
    }

    public BackpackTier tierOf(ItemStack backpack) {
        Integer level = backpack.getItemMeta().getPersistentDataContainer().get(tierKey, PersistentDataType.INTEGER);
        return BackpackTier.byLevel(level != null ? level : 1);
    }

    public void setTier(ItemStack backpack, BackpackTier tier) {
        ItemMeta meta = backpack.getItemMeta();
        meta.getPersistentDataContainer().set(tierKey, PersistentDataType.INTEGER, tier.level());
        backpack.setItemMeta(meta);
    }

    public boolean hasMagnet(ItemStack backpack) {
        Byte flag = backpack.getItemMeta().getPersistentDataContainer().get(magnetKey, PersistentDataType.BYTE);
        return flag != null && flag == 1;
    }

    public void setMagnet(ItemStack backpack, boolean enabled) {
        ItemMeta meta = backpack.getItemMeta();
        meta.getPersistentDataContainer().set(magnetKey, PersistentDataType.BYTE, (byte) (enabled ? 1 : 0));
        backpack.setItemMeta(meta);
    }

    public boolean hasSort(ItemStack backpack) {
        Byte flag = backpack.getItemMeta().getPersistentDataContainer().get(sortKey, PersistentDataType.BYTE);
        return flag != null && flag == 1;
    }

    public void setSort(ItemStack backpack, boolean enabled) {
        ItemMeta meta = backpack.getItemMeta();
        meta.getPersistentDataContainer().set(sortKey, PersistentDataType.BYTE, (byte) (enabled ? 1 : 0));
        backpack.setItemMeta(meta);
    }

    /** Rewrites display name and lore from the backpack's current tier/ability state. Call after any tag mutation. */
    public void refreshDisplay(ItemStack backpack) {
        BackpackTier tier = tierOf(backpack);
        ItemMeta meta = backpack.getItemMeta();
        meta.setDisplayName(tier.coloredName());

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + tier.slots() + " slots");
        if (hasMagnet(backpack)) lore.add(ChatColor.BLUE + "\u2726 Magnet");
        if (hasSort(backpack)) lore.add(ChatColor.YELLOW + "\u2726 Auto-Sort (on close)");
        lore.add(ChatColor.DARK_GRAY + "Right-click to open");
        lore.add(ChatColor.DARK_GRAY + "Sneak + right-click with a core in your other hand to upgrade");
        meta.setLore(lore);

        backpack.setItemMeta(meta);
    }

    // --- Contents (de)serialization ---

    public ItemStack[] readContents(ItemStack backpack) {
        byte[] bytes = backpack.getItemMeta().getPersistentDataContainer().get(contentsKey, PersistentDataType.BYTE_ARRAY);
        return deserializeContents(bytes, tierOf(backpack).slots());
    }

    public void writeContents(ItemStack backpack, ItemStack[] contents) {
        ItemMeta meta = backpack.getItemMeta();
        meta.getPersistentDataContainer().set(contentsKey, PersistentDataType.BYTE_ARRAY, serializeContents(contents));
        backpack.setItemMeta(meta);
    }

    /** Called after a tier upgrade: keeps existing items in place, grows the array to the new tier's slot count. */
    public void resizeContents(ItemStack backpack, BackpackTier newTier) {
        ItemStack[] old = readContents(backpack);
        ItemStack[] resized = new ItemStack[newTier.slots()];
        System.arraycopy(old, 0, resized, 0, Math.min(old.length, resized.length));
        writeContents(backpack, resized);
    }

    private byte[] serializeContents(ItemStack[] contents) {
        List<ItemStack> list = new ArrayList<>(contents.length);
        for (ItemStack item : contents) {
            list.add(item == null ? new ItemStack(Material.AIR) : item);
        }
        return ItemStack.serializeItemsAsBytes(list);
    }

    private ItemStack[] deserializeContents(byte[] bytes, int slotCount) {
        ItemStack[] result = new ItemStack[slotCount];
        if (bytes == null || bytes.length == 0) return result;

        ItemStack[] restored = ItemStack.deserializeItemsFromBytes(bytes);
        for (int i = 0; i < slotCount && i < restored.length; i++) {
            ItemStack item = restored[i];
            result[i] = (item == null || item.getType() == Material.AIR) ? null : item;
        }
        return result;
    }

    // --- Core (upgrade/ability) items ---

    public ItemStack createUpgradeCore() {
        return simpleCore(ID_UPGRADE_CORE, Material.ENDER_EYE, ChatColor.AQUA + "Backpack Upgrade Core",
                List.of(ChatColor.GRAY + "Sneak + right-click, holding this in your", ChatColor.GRAY + "main hand and a backpack in your off-hand,", ChatColor.GRAY + "to upgrade it to the next tier."));
    }

    public ItemStack createMagnetCore() {
        return simpleCore(ID_MAGNET_CORE, Material.LODESTONE, ChatColor.BLUE + "Magnet Core",
                List.of(ChatColor.GRAY + "Sneak + right-click, holding this in your", ChatColor.GRAY + "main hand and a backpack in your off-hand,", ChatColor.GRAY + "to install a pickup magnet on it."));
    }

    public ItemStack createSortingCore() {
        return simpleCore(ID_SORTING_CORE, Material.HOPPER, ChatColor.YELLOW + "Sorting Core",
                List.of(ChatColor.GRAY + "Sneak + right-click, holding this in your", ChatColor.GRAY + "main hand and a backpack in your off-hand,", ChatColor.GRAY + "to install auto-sort on it."));
    }

    private ItemStack simpleCore(String id, Material material, String displayName, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, id);
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }
}

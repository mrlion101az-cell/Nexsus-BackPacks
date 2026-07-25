package com.nexusuniverse.backpacks.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

public final class BackpackRecipes {

    private BackpackRecipes() {}

    public static void registerAll(Plugin plugin, BackpackFactory factory) {
        register(plugin, "backpack", factory.createBackpack(BackpackTier.SCOUT),
                new String[]{"LSL", "LCL", "LSL"},
                new RecipeChoice[]{
                        new RecipeChoice.MaterialChoice(Material.LEATHER), new RecipeChoice.MaterialChoice(Material.STRING), new RecipeChoice.MaterialChoice(Material.LEATHER),
                        new RecipeChoice.MaterialChoice(Material.LEATHER), new RecipeChoice.MaterialChoice(Material.CHEST), new RecipeChoice.MaterialChoice(Material.LEATHER),
                        new RecipeChoice.MaterialChoice(Material.LEATHER), new RecipeChoice.MaterialChoice(Material.STRING), new RecipeChoice.MaterialChoice(Material.LEATHER)
                });

        register(plugin, "upgrade_core", factory.createUpgradeCore(),
                new String[]{"ABA", "BCB", "ABA"},
                new RecipeChoice[]{
                        new RecipeChoice.MaterialChoice(Material.IRON_INGOT), new RecipeChoice.MaterialChoice(Material.DIAMOND), new RecipeChoice.MaterialChoice(Material.IRON_INGOT),
                        new RecipeChoice.MaterialChoice(Material.DIAMOND), new RecipeChoice.MaterialChoice(Material.ENDER_PEARL), new RecipeChoice.MaterialChoice(Material.DIAMOND),
                        new RecipeChoice.MaterialChoice(Material.IRON_INGOT), new RecipeChoice.MaterialChoice(Material.DIAMOND), new RecipeChoice.MaterialChoice(Material.IRON_INGOT)
                });

        register(plugin, "magnet_core", factory.createMagnetCore(),
                new String[]{"ABA", "BCB", "ABA"},
                new RecipeChoice[]{
                        new RecipeChoice.MaterialChoice(Material.IRON_INGOT), new RecipeChoice.MaterialChoice(Material.REDSTONE), new RecipeChoice.MaterialChoice(Material.IRON_INGOT),
                        new RecipeChoice.MaterialChoice(Material.REDSTONE), new RecipeChoice.MaterialChoice(Material.COMPASS), new RecipeChoice.MaterialChoice(Material.REDSTONE),
                        new RecipeChoice.MaterialChoice(Material.IRON_INGOT), new RecipeChoice.MaterialChoice(Material.REDSTONE), new RecipeChoice.MaterialChoice(Material.IRON_INGOT)
                });

        register(plugin, "sorting_core", factory.createSortingCore(),
                new String[]{"ABA", "BCB", "ABA"},
                new RecipeChoice[]{
                        new RecipeChoice.MaterialChoice(Material.IRON_INGOT), new RecipeChoice.MaterialChoice(Material.PAPER), new RecipeChoice.MaterialChoice(Material.IRON_INGOT),
                        new RecipeChoice.MaterialChoice(Material.PAPER), new RecipeChoice.MaterialChoice(Material.HOPPER), new RecipeChoice.MaterialChoice(Material.PAPER),
                        new RecipeChoice.MaterialChoice(Material.IRON_INGOT), new RecipeChoice.MaterialChoice(Material.PAPER), new RecipeChoice.MaterialChoice(Material.IRON_INGOT)
                });
    }

    private static void register(Plugin plugin, String id, ItemStack result, String[] shape, RecipeChoice[] ingredients) {
        NamespacedKey key = new NamespacedKey(plugin, id);
        ShapedRecipe recipe = new ShapedRecipe(key, result).shape(shape);

        String flatShape = String.join("", shape);
        var assigned = new java.util.HashSet<Character>();
        for (int i = 0; i < flatShape.length() && i < ingredients.length; i++) {
            char symbol = flatShape.charAt(i);
            if (assigned.contains(symbol)) continue;
            assigned.add(symbol);
            if (ingredients[i] != null) recipe.setIngredient(symbol, ingredients[i]);
        }

        Bukkit.addRecipe(recipe);
    }
}

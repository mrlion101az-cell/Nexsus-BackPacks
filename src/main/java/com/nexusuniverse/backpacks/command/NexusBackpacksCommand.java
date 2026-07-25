package com.nexusuniverse.backpacks.command;

import com.nexusuniverse.backpacks.config.BackpacksConfig;
import com.nexusuniverse.backpacks.items.BackpackFactory;
import com.nexusuniverse.backpacks.items.BackpackTier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NexusBackpacksCommand implements CommandExecutor {

    private final BackpackFactory factory;
    private final BackpacksConfig config;

    public NexusBackpacksCommand(BackpackFactory factory, BackpacksConfig config) {
        this.factory = factory;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("give")) {
            return handleGive(sender, args);
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("nexusbackpacks.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission for that.");
                return true;
            }
            config.reload();
            sender.sendMessage(ChatColor.GREEN + "Config reloaded.");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /nexusbackpacks give <backpack [tier]|upgrade_core|magnet_core|sorting_core> [player]");
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /nexusbackpacks give <backpack [tier]|upgrade_core|magnet_core|sorting_core> [player]");
            return true;
        }

        String itemType = args[1].toLowerCase();
        BackpackTier tier = BackpackTier.SCOUT;
        int nextArgIndex = 2;

        if (itemType.equals("backpack") && args.length > 2) {
            try {
                tier = BackpackTier.byLevel(Integer.parseInt(args[2]));
                nextArgIndex = 3;
            } catch (NumberFormatException ignored) {
                // args[2] wasn't a tier number -- treat it as a player name instead
            }
        }

        Player target = args.length > nextArgIndex
                ? Bukkit.getPlayerExact(args[nextArgIndex])
                : (sender instanceof Player p ? p : null);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found or not specified.");
            return true;
        }

        ItemStack item = switch (itemType) {
            case "backpack" -> factory.createBackpack(tier);
            case "upgrade_core" -> factory.createUpgradeCore();
            case "magnet_core" -> factory.createMagnetCore();
            case "sorting_core" -> factory.createSortingCore();
            default -> null;
        };
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "Unknown item type: " + itemType);
            return true;
        }

        var leftover = target.getInventory().addItem(item);
        leftover.values().forEach(i -> target.getWorld().dropItemNaturally(target.getLocation(), i));
        sender.sendMessage(ChatColor.GREEN + "Gave " + itemType + " to " + target.getName() + ".");
        return true;
    }
}

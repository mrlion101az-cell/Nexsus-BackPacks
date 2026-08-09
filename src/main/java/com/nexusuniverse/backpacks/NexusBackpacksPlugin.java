package com.nexusuniverse.backpacks;

import com.nexusuniverse.backpacks.ability.MagnetTask;
import com.nexusuniverse.backpacks.command.NexusBackpacksCommand;
import com.nexusuniverse.backpacks.config.BackpacksConfig;
import com.nexusuniverse.backpacks.gui.BackpackGUI;
import com.nexusuniverse.backpacks.gui.BackpackListener;
import com.nexusuniverse.backpacks.hint.BackpackHintListener;
import com.nexusuniverse.backpacks.items.BackpackFactory;
import com.nexusuniverse.backpacks.items.BackpackRecipes;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class NexusBackpacksPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        BackpacksConfig config = new BackpacksConfig(this);
        BackpackFactory factory = new BackpackFactory(this);
        BackpackGUI gui = new BackpackGUI(factory);
        BackpackRecipes.registerAll(this, factory);

        Bukkit.getPluginManager().registerEvents(new BackpackListener(factory, gui, config), this);
        Bukkit.getPluginManager().registerEvents(new BackpackHintListener(factory, config), this);

        getCommand("nexusbackpacks").setExecutor(new NexusBackpacksCommand(factory, config));

        long magnetInterval = config.magnetTickInterval();
        Bukkit.getScheduler().runTaskTimer(this, new MagnetTask(factory, config), magnetInterval, magnetInterval);

        getLogger().info("NexusBackpacks enabled.");
    }
}

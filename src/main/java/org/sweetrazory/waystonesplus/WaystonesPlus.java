package org.sweetrazory.waystonesplus;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.sweetrazory.waystonesplus.memoryhandlers.*;
import org.sweetrazory.waystonesplus.menu.MenuListener;
import org.sweetrazory.waystonesplus.menu.MenuManager;
import org.sweetrazory.waystonesplus.utils.ColoredText;
import org.sweetrazory.waystonesplus.utils.ResourcePackManager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class WaystonesPlus extends JavaPlugin implements Listener {
    public static CooldownManager cooldownManager = new CooldownManager();
    public static ConfigManager configMemory = new ConfigManager();
    public static MenuManager menuManager = new MenuManager();
    public static WaystoneMemory waystoneMemory;
    private static WaystonesPlus instance;
    private DatabaseManager databaseManager;
    private ResourcePackManager resourcePackManager;

    public static WaystonesPlus getInstance() {
        return instance;
    }

    public static Logger Logger() {
        return getInstance().getLogger();
    }

    @Override
    public void onEnable() {
        instance = this;
        loadWaystonesConfig();
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        ConfigManager.loadConfig();
        LangManager.loadConfig();
        databaseManager = new DatabaseManager();
        databaseManager.initializeDatabase();
        databaseManager.migrateWaystones();
        
        // Initialize waystone types - must be after ConfigManager.loadConfig()
        waystoneMemory = new WaystoneMemory();

        String bukkitVersion = Bukkit.getVersion();
        if (!bukkitVersion.contains("1.19.4") && !bukkitVersion.contains("1.20")
                && !bukkitVersion.contains("1.21") && !bukkitVersion.contains("26.")) {
            getLogger().warning(ColoredText.getText(LangManager.versionWarning));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        EventController eventController = new EventController();
        getServer().getPluginManager().registerEvents(eventController, this);
        getServer().getPluginManager().registerEvents(new MenuListener(menuManager), this);

        resourcePackManager = new ResourcePackManager();
        resourcePackManager.enable();

        // Unlock waystone recipes for all online players
        if (ConfigManager.enableCrafting) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                EventController.unlockWaystoneRecipes(player);
            }
        }

        List<String> commandAliases = Arrays.asList("waystones", "waystone", "wsp", "waystonesplus", "waystoneplus");

        for (String commandAlias : commandAliases) {
            PluginCommand command = getCommand(commandAlias);
            if (command != null) {
                command.setExecutor(new CommandManager());
            } else {
                getLogger().warning("Command '" + commandAlias + "' not found in plugin.yml! Skipping registration.");
            }
        }
    }

    @Override
    public void onDisable() {
        if (resourcePackManager != null) {
            resourcePackManager.disable();
        }
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
    }

    public void loadWaystonesConfig() {
        File waystonesFile = new File(getDataFolder(), "waystones.yml");
        File configFile = new File(getDataFolder(), "config.yml");
        File localizationFile = new File(getDataFolder(), "localization.yml");
        if (!localizationFile.exists()) {
            saveResource("localization.yml", false);
        }

        if (!waystonesFile.exists()) {
            if (configFile.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                saveWaystonesConfig(config);

                if (!configFile.delete()) {
                    getLogger().warning("Failed to delete config.yml");
                }
                saveResource("config.yml", false);

                return;
            }

            saveResource("waystones.yml", false);
        } else if (configFile.exists()) {
            YamlConfiguration waystonesConfig = YamlConfiguration.loadConfiguration(waystonesFile);
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            if (waystonesConfig.isConfigurationSection("waystones") && config.isConfigurationSection("waystones")) {
                config.set("waystones", null);
                saveConfig();
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(waystonesFile);
        config.options().copyDefaults(true);
        saveWaystonesConfig(config);
    }


    public void saveWaystonesConfig(FileConfiguration config) {
        try {
            config.save(new File(getDataFolder(), "waystones.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


package com.noctivag.dropper;

import com.noctivag.dropper.command.DropperCommand;
import com.noctivag.dropper.listener.PlayerListener;
import com.noctivag.dropper.manager.MapManager;
import com.noctivag.dropper.manager.SessionManager;
import com.noctivag.dropper.manager.StatisticsManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Main plugin class for the Next Level Dropper Plugin
 * Features: Multiple maps, level progression, checkpoints, and persistent storage
 */
public class DropperPlugin extends JavaPlugin {

    private static DropperPlugin instance;

    private MapManager mapManager;
    private SessionManager sessionManager;
    private StatisticsManager statisticsManager;

    @Override
    public void onEnable() {
        instance = this;

        // Create plugin data folder
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Create maps directory
        File mapsFolder = new File(getDataFolder(), "maps");
        if (!mapsFolder.exists()) {
            mapsFolder.mkdirs();
        }

        // Save default config
        saveDefaultConfig();

        // Initialize managers
        this.mapManager = new MapManager(this);
        this.sessionManager = new SessionManager(this);
        this.statisticsManager = new StatisticsManager(this);

        // Load data
        mapManager.loadMaps();
        statisticsManager.loadStatistics();

        // Register commands
        DropperCommand dropperCommand = new DropperCommand(this);
        getCommand("dropper").setExecutor(dropperCommand);
        getCommand("dropper").setTabCompleter(dropperCommand);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Start auto-save task
        int autoSaveInterval = getConfig().getInt("settings.auto-save-interval", 300);
        if (autoSaveInterval > 0) {
            getServer().getScheduler().runTaskTimer(this, () -> {
                mapManager.saveMaps();
                statisticsManager.saveStatistics();
            }, autoSaveInterval * 20L, autoSaveInterval * 20L);
        }

        getLogger().info("Dropper Plugin has been enabled!");
        getLogger().info("Loaded " + mapManager.getMaps().size() + " dropper map(s)");
    }

    @Override
    public void onDisable() {
        // Stop all active sessions
        if (sessionManager != null) {
            sessionManager.stopAllSessions();
        }

        // Save all data
        if (mapManager != null) {
            mapManager.saveMaps();
        }
        if (statisticsManager != null) {
            statisticsManager.saveStatistics();
        }

        getLogger().info("Dropper Plugin has been disabled!");
    }

    public static DropperPlugin getInstance() {
        return instance;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }
}

package com.noctivag.dropper.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.noctivag.dropper.DropperPlugin;
import com.noctivag.dropper.model.PlayerStatistics;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player statistics with JSON file storage
 */
public class StatisticsManager {

    private final DropperPlugin plugin;
    private final Map<UUID, PlayerStatistics> statistics;
    private final Gson gson;
    private final File statsFile;

    public StatisticsManager(DropperPlugin plugin) {
        this.plugin = plugin;
        this.statistics = new HashMap<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.statsFile = new File(plugin.getDataFolder(), "statistics.json");
    }

    /**
     * Load statistics from JSON file
     */
    public void loadStatistics() {
        if (!statsFile.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(statsFile)) {
            Type type = new TypeToken<Map<UUID, PlayerStatistics>>(){}.getType();
            Map<UUID, PlayerStatistics> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                statistics.putAll(loaded);
                plugin.getLogger().info("Loaded statistics for " + statistics.size() + " player(s)");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load statistics: " + e.getMessage());
        }
    }

    /**
     * Save statistics to JSON file
     */
    public void saveStatistics() {
        try (FileWriter writer = new FileWriter(statsFile)) {
            gson.toJson(statistics, writer);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save statistics: " + e.getMessage());
        }
    }

    /**
     * Get player statistics
     */
    public PlayerStatistics getStatistics(UUID playerId) {
        return statistics.computeIfAbsent(playerId, PlayerStatistics::new);
    }

    /**
     * Record a map play
     */
    public void recordPlay(UUID playerId, String mapName) {
        getStatistics(playerId).recordPlay(mapName);
    }

    /**
     * Record a map completion
     */
    public void recordCompletion(UUID playerId, String mapName, long time) {
        getStatistics(playerId).recordCompletion(mapName, time);
    }

    /**
     * Get statistics for a specific map
     */
    public PlayerStatistics.MapStats getMapStats(UUID playerId, String mapName) {
        return getStatistics(playerId).getStats(mapName);
    }
}

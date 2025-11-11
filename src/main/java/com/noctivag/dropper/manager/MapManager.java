package com.noctivag.dropper.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.noctivag.dropper.DropperPlugin;
import com.noctivag.dropper.model.DropperMap;
import com.noctivag.dropper.util.LocationSerializer;
import org.bukkit.Location;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages dropper maps with JSON file storage
 */
public class MapManager {

    private final DropperPlugin plugin;
    private final Map<String, DropperMap> maps;
    private final Gson gson;
    private final File mapsFolder;

    public MapManager(DropperPlugin plugin) {
        this.plugin = plugin;
        this.maps = new HashMap<>();
        this.mapsFolder = new File(plugin.getDataFolder(), "maps");

        // Initialize GSON with Location serializer
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Location.class, new LocationSerializer())
                .setPrettyPrinting()
                .create();

        if (!mapsFolder.exists()) {
            mapsFolder.mkdirs();
        }
    }

    /**
     * Load all maps from JSON files
     */
    public void loadMaps() {
        File[] files = mapsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            return;
        }

        int loaded = 0;
        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                DropperMap map = gson.fromJson(reader, DropperMap.class);
                if (map != null) {
                    maps.put(map.getName().toLowerCase(), map);
                    loaded++;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load map from " + file.getName() + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + loaded + " dropper map(s)");
    }

    /**
     * Save all maps to JSON files
     */
    public void saveMaps() {
        for (DropperMap map : maps.values()) {
            saveMap(map);
        }
    }

    /**
     * Save a specific map to JSON file
     */
    public void saveMap(DropperMap map) {
        File file = new File(mapsFolder, map.getName() + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(map, writer);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save map " + map.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Create a new dropper map
     */
    public boolean createMap(String name, String creator) {
        if (maps.containsKey(name.toLowerCase())) {
            return false;
        }

        DropperMap map = new DropperMap(name, creator);
        maps.put(name.toLowerCase(), map);
        saveMap(map);
        return true;
    }

    /**
     * Delete a dropper map
     */
    public boolean deleteMap(String name) {
        DropperMap map = maps.remove(name.toLowerCase());
        if (map == null) {
            return false;
        }

        File file = new File(mapsFolder, map.getName() + ".json");
        if (file.exists()) {
            file.delete();
        }
        return true;
    }

    /**
     * Get a dropper map by name
     */
    public DropperMap getMap(String name) {
        return maps.get(name.toLowerCase());
    }

    /**
     * Check if a map exists
     */
    public boolean hasMap(String name) {
        return maps.containsKey(name.toLowerCase());
    }

    /**
     * Get all map names
     */
    public Set<String> getMapNames() {
        return maps.keySet();
    }

    /**
     * Get all maps
     */
    public Map<String, DropperMap> getMaps() {
        return maps;
    }
}

package com.noctivag.dropper.model;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Represents an active dropper game session for a player
 */
public class DropperSession {

    private final UUID playerId;
    private final DropperMap map;
    private int currentLevel;
    private long startTime;
    private Location returnLocation;
    private GameMode previousGameMode;
    private ItemStack[] previousInventory;
    private ItemStack[] previousArmor;

    public DropperSession(Player player, DropperMap map) {
        this.playerId = player.getUniqueId();
        this.map = map;
        this.currentLevel = 1;
        this.startTime = System.currentTimeMillis();
        this.returnLocation = player.getLocation().clone();
        this.previousGameMode = player.getGameMode();
        this.previousInventory = player.getInventory().getContents().clone();
        this.previousArmor = player.getInventory().getArmorContents().clone();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public DropperMap getMap() {
        return map;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public long getStartTime() {
        return startTime;
    }

    public Location getReturnLocation() {
        return returnLocation;
    }

    public GameMode getPreviousGameMode() {
        return previousGameMode;
    }

    public ItemStack[] getPreviousInventory() {
        return previousInventory;
    }

    public ItemStack[] getPreviousArmor() {
        return previousArmor;
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    public String getFormattedTime() {
        long elapsed = getElapsedTime();
        long seconds = elapsed / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        long millis = elapsed % 1000;

        if (minutes > 0) {
            return String.format("%d:%02d.%03d", minutes, seconds, millis);
        } else {
            return String.format("%d.%03ds", seconds, millis);
        }
    }

    public void nextLevel() {
        currentLevel++;
    }

    public boolean isCompleted() {
        return currentLevel > map.getMaxLevel();
    }

    public DropperLevel getCurrentLevelData() {
        return map.getLevel(currentLevel);
    }
}

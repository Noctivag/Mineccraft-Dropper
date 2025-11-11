package com.noctivag.dropper.model;

import org.bukkit.Location;

/**
 * Represents a single level in a dropper map
 */
public class DropperLevel {

    private int levelNumber;
    private Location startLocation;
    private Location endLocation;
    private Location checkpointLocation;

    public DropperLevel(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public DropperLevel(int levelNumber, Location startLocation, Location endLocation) {
        this.levelNumber = levelNumber;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }

    public Location getCheckpointLocation() {
        return checkpointLocation;
    }

    public void setCheckpointLocation(Location checkpointLocation) {
        this.checkpointLocation = checkpointLocation;
    }

    public boolean hasCheckpoint() {
        return checkpointLocation != null;
    }

    public boolean isComplete() {
        return startLocation != null && endLocation != null;
    }
}

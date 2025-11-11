package com.noctivag.dropper.model;

import java.util.*;

/**
 * Represents a complete dropper map with multiple levels
 */
public class DropperMap {

    private String name;
    private String creator;
    private long createdDate;
    private Map<Integer, DropperLevel> levels;

    public DropperMap(String name, String creator) {
        this.name = name;
        this.creator = creator;
        this.createdDate = System.currentTimeMillis();
        this.levels = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public Map<Integer, DropperLevel> getLevels() {
        return levels;
    }

    public void setLevels(Map<Integer, DropperLevel> levels) {
        this.levels = levels;
    }

    public void addLevel(DropperLevel level) {
        levels.put(level.getLevelNumber(), level);
    }

    public DropperLevel getLevel(int levelNumber) {
        return levels.get(levelNumber);
    }

    public boolean hasLevel(int levelNumber) {
        return levels.containsKey(levelNumber);
    }

    public int getLevelCount() {
        return levels.size();
    }

    public int getMaxLevel() {
        return levels.keySet().stream().max(Integer::compareTo).orElse(0);
    }

    public List<Integer> getSortedLevelNumbers() {
        List<Integer> levelNumbers = new ArrayList<>(levels.keySet());
        Collections.sort(levelNumbers);
        return levelNumbers;
    }

    public boolean isComplete() {
        if (levels.isEmpty()) {
            return false;
        }
        for (DropperLevel level : levels.values()) {
            if (!level.isComplete()) {
                return false;
            }
        }
        return true;
    }

    public void removeLevel(int levelNumber) {
        levels.remove(levelNumber);
    }
}

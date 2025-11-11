package com.noctivag.dropper.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stores player statistics for dropper maps
 */
public class PlayerStatistics {

    private UUID playerId;
    private Map<String, MapStats> mapStats;

    public PlayerStatistics(UUID playerId) {
        this.playerId = playerId;
        this.mapStats = new HashMap<>();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Map<String, MapStats> getMapStats() {
        return mapStats;
    }

    public void setMapStats(Map<String, MapStats> mapStats) {
        this.mapStats = mapStats;
    }

    public MapStats getStats(String mapName) {
        return mapStats.computeIfAbsent(mapName, k -> new MapStats());
    }

    public void recordPlay(String mapName) {
        getStats(mapName).incrementPlays();
    }

    public void recordCompletion(String mapName, long time) {
        MapStats stats = getStats(mapName);
        stats.incrementCompletions();
        if (time < stats.getBestTime() || stats.getBestTime() == 0) {
            stats.setBestTime(time);
        }
    }

    /**
     * Statistics for a specific map
     */
    public static class MapStats {
        private int plays;
        private int completions;
        private long bestTime;

        public MapStats() {
            this.plays = 0;
            this.completions = 0;
            this.bestTime = 0;
        }

        public int getPlays() {
            return plays;
        }

        public void setPlays(int plays) {
            this.plays = plays;
        }

        public void incrementPlays() {
            this.plays++;
        }

        public int getCompletions() {
            return completions;
        }

        public void setCompletions(int completions) {
            this.completions = completions;
        }

        public void incrementCompletions() {
            this.completions++;
        }

        public long getBestTime() {
            return bestTime;
        }

        public void setBestTime(long bestTime) {
            this.bestTime = bestTime;
        }

        public String getFormattedBestTime() {
            if (bestTime == 0) {
                return "N/A";
            }
            long seconds = bestTime / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            long millis = bestTime % 1000;

            if (minutes > 0) {
                return String.format("%d:%02d.%03d", minutes, seconds, millis);
            } else {
                return String.format("%d.%03ds", seconds, millis);
            }
        }
    }
}

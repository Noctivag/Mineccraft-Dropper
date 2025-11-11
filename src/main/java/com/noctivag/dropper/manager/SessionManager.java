package com.noctivag.dropper.manager;

import com.noctivag.dropper.DropperPlugin;
import com.noctivag.dropper.model.DropperLevel;
import com.noctivag.dropper.model.DropperMap;
import com.noctivag.dropper.model.DropperSession;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages active dropper game sessions
 */
public class SessionManager {

    private final DropperPlugin plugin;
    private final Map<UUID, DropperSession> activeSessions;

    public SessionManager(DropperPlugin plugin) {
        this.plugin = plugin;
        this.activeSessions = new HashMap<>();
    }

    /**
     * Start a new dropper session for a player
     */
    public boolean startSession(Player player, DropperMap map) {
        if (activeSessions.containsKey(player.getUniqueId())) {
            return false;
        }

        // Check if map has at least one complete level
        if (map.getLevelCount() == 0 || !map.getLevel(1).isComplete()) {
            return false;
        }

        // Create session
        DropperSession session = new DropperSession(player, map);
        activeSessions.put(player.getUniqueId(), session);

        // Prepare player
        if (plugin.getConfig().getBoolean("settings.clear-inventory", true)) {
            player.getInventory().clear();
        }

        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(plugin.getConfig().getBoolean("gameplay.allow-flight", false));
        player.setFlying(false);

        // Teleport to first level
        DropperLevel firstLevel = map.getLevel(1);
        if (firstLevel.getStartLocation() != null) {
            player.teleport(firstLevel.getStartLocation());
        }

        // Record play in statistics
        if (plugin.getConfig().getBoolean("settings.enable-stats", true)) {
            plugin.getStatisticsManager().recordPlay(player.getUniqueId(), map.getName());
        }

        return true;
    }

    /**
     * Stop a player's dropper session
     */
    public boolean stopSession(Player player, boolean restore) {
        DropperSession session = activeSessions.remove(player.getUniqueId());
        if (session == null) {
            return false;
        }

        if (restore) {
            // Restore player state
            if (plugin.getConfig().getBoolean("settings.restore-inventory", true)) {
                player.getInventory().setContents(session.getPreviousInventory());
                player.getInventory().setArmorContents(session.getPreviousArmor());
            }

            player.setGameMode(session.getPreviousGameMode());
            player.setAllowFlight(session.getPreviousGameMode() == GameMode.CREATIVE ||
                                 session.getPreviousGameMode() == GameMode.SPECTATOR);

            // Teleport back to original location
            if (session.getReturnLocation() != null) {
                player.teleport(session.getReturnLocation());
            }
        }

        return true;
    }

    /**
     * Stop all active sessions
     */
    public void stopAllSessions() {
        for (UUID playerId : activeSessions.keySet()) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                stopSession(player, true);
            }
        }
        activeSessions.clear();
    }

    /**
     * Get a player's active session
     */
    public DropperSession getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    /**
     * Check if a player has an active session
     */
    public boolean hasSession(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    /**
     * Progress player to next level
     */
    public boolean nextLevel(Player player) {
        DropperSession session = getSession(player);
        if (session == null) {
            return false;
        }

        session.nextLevel();

        // Check if map is completed
        if (session.isCompleted()) {
            completeMap(player, session);
            return true;
        }

        // Teleport to next level
        DropperLevel nextLevel = session.getCurrentLevelData();
        if (nextLevel != null && nextLevel.getStartLocation() != null) {
            player.teleport(nextLevel.getStartLocation());
            return true;
        }

        return false;
    }

    /**
     * Handle map completion
     */
    private void completeMap(Player player, DropperSession session) {
        // Record completion
        if (plugin.getConfig().getBoolean("settings.enable-stats", true)) {
            plugin.getStatisticsManager().recordCompletion(
                player.getUniqueId(),
                session.getMap().getName(),
                session.getElapsedTime()
            );
        }

        // Send completion message
        String message = plugin.getConfig().getString("messages.game-completed", "")
                .replace("%map%", session.getMap().getName())
                .replace("%time%", session.getFormattedTime())
                .replace("&", "§");
        player.sendMessage(message);

        // Execute reward commands
        if (plugin.getConfig().getBoolean("gameplay.rewards.enabled", false)) {
            for (String command : plugin.getConfig().getStringList("gameplay.rewards.completion-commands")) {
                String cmd = command.replace("%player%", player.getName());
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
            }
        }

        // Stop session
        stopSession(player, true);
    }

    /**
     * Teleport player to checkpoint or level start
     */
    public boolean teleportToCheckpoint(Player player) {
        DropperSession session = getSession(player);
        if (session == null) {
            return false;
        }

        DropperLevel currentLevel = session.getCurrentLevelData();
        if (currentLevel == null) {
            return false;
        }

        // Try checkpoint first, then level start
        if (currentLevel.hasCheckpoint()) {
            player.teleport(currentLevel.getCheckpointLocation());
        } else if (currentLevel.getStartLocation() != null) {
            player.teleport(currentLevel.getStartLocation());
        } else {
            return false;
        }

        return true;
    }
}

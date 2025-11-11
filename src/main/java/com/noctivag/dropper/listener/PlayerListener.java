package com.noctivag.dropper.listener;

import com.noctivag.dropper.DropperPlugin;
import com.noctivag.dropper.model.DropperLevel;
import com.noctivag.dropper.model.DropperSession;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

/**
 * Event listener for handling dropper game mechanics
 */
public class PlayerListener implements Listener {

    private final DropperPlugin plugin;

    public PlayerListener(DropperPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle player movement to detect level completion
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        DropperSession session = plugin.getSessionManager().getSession(player);

        if (session == null) {
            return;
        }

        Location to = event.getTo();
        if (to == null) {
            return;
        }

        // Check if player fell below minimum Y level
        double minY = plugin.getConfig().getDouble("gameplay.min-y-level", -64);
        if (to.getY() < minY) {
            if (plugin.getConfig().getBoolean("settings.teleport-on-fall", true)) {
                plugin.getSessionManager().teleportToCheckpoint(player);
                sendMessage(player, "teleported-checkpoint");
            }
            return;
        }

        // Check if player reached the end of current level
        DropperLevel currentLevel = session.getCurrentLevelData();
        if (currentLevel == null || currentLevel.getEndLocation() == null) {
            return;
        }

        Location endLoc = currentLevel.getEndLocation();
        if (isNear(to, endLoc, 2.0)) {
            // Level completed!
            int currentLevelNum = session.getCurrentLevel();
            int nextLevelNum = currentLevelNum + 1;

            if (session.getMap().hasLevel(nextLevelNum)) {
                // Move to next level
                plugin.getSessionManager().nextLevel(player);
                sendMessage(player, "level-completed",
                        "%level%", String.valueOf(currentLevelNum),
                        "%next%", String.valueOf(nextLevelNum));
            } else {
                // Map completed!
                plugin.getSessionManager().nextLevel(player);
            }
        }

        // Check if player reached a checkpoint
        if (currentLevel.hasCheckpoint()) {
            Location checkpointLoc = currentLevel.getCheckpointLocation();
            if (isNear(to, checkpointLoc, 2.0)) {
                // Could add checkpoint notification here if desired
            }
        }
    }

    /**
     * Handle player damage
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        DropperSession session = plugin.getSessionManager().getSession(player);

        if (session == null) {
            return;
        }

        // Apply damage multiplier
        double multiplier = plugin.getConfig().getDouble("gameplay.damage-multiplier", 0.0);
        if (multiplier <= 0.0) {
            event.setCancelled(true);
        } else if (multiplier != 1.0) {
            event.setDamage(event.getDamage() * multiplier);
        }
    }

    /**
     * Handle player death
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        DropperSession session = plugin.getSessionManager().getSession(player);

        if (session == null) {
            return;
        }

        // Prevent death and teleport to checkpoint instead
        event.setCancelled(true);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getSessionManager().teleportToCheckpoint(player);
            sendMessage(player, "teleported-checkpoint");
        }, 1L);
    }

    /**
     * Handle player respawn
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        DropperSession session = plugin.getSessionManager().getSession(player);

        if (session == null) {
            return;
        }

        // Teleport to checkpoint on respawn
        DropperLevel currentLevel = session.getCurrentLevelData();
        if (currentLevel != null) {
            if (currentLevel.hasCheckpoint()) {
                event.setRespawnLocation(currentLevel.getCheckpointLocation());
            } else if (currentLevel.getStartLocation() != null) {
                event.setRespawnLocation(currentLevel.getStartLocation());
            }
        }
    }

    /**
     * Handle player quit
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getSessionManager().hasSession(player)) {
            plugin.getSessionManager().stopSession(player, false);
        }
    }

    /**
     * Handle player kick
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if (plugin.getSessionManager().hasSession(player)) {
            plugin.getSessionManager().stopSession(player, false);
        }
    }

    /**
     * Handle player teleport (external teleports should exit the game)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        DropperSession session = plugin.getSessionManager().getSession(player);

        if (session == null) {
            return;
        }

        // If teleport is caused by plugin or command, allow it
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (cause == PlayerTeleportEvent.TeleportCause.PLUGIN ||
            cause == PlayerTeleportEvent.TeleportCause.COMMAND) {
            return;
        }

        // For other teleports (ender pearl, etc.), check if it's within the map
        // If not, stop the session
        Location to = event.getTo();
        if (to != null && !isInMap(to, session)) {
            plugin.getSessionManager().stopSession(player, true);
            sendMessage(player, "game-stopped");
        }
    }

    /**
     * Check if a location is near another location
     */
    private boolean isNear(Location loc1, Location loc2, double distance) {
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }
        return loc1.distance(loc2) <= distance;
    }

    /**
     * Check if a location is within the map bounds
     */
    private boolean isInMap(Location location, DropperSession session) {
        // Simple check: if player is in the same world as any level
        for (DropperLevel level : session.getMap().getLevels().values()) {
            if (level.getStartLocation() != null &&
                level.getStartLocation().getWorld().equals(location.getWorld())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Send a formatted message to a player
     */
    private void sendMessage(Player player, String key, String... replacements) {
        String prefix = plugin.getConfig().getString("messages.prefix", "§8[§bDropper§8]§r ");
        String message = plugin.getConfig().getString("messages." + key, "");

        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }

        player.sendMessage((prefix + message).replace("&", "§"));
    }
}

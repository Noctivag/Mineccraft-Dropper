# Dropper Plugin - Next Level Edition

A comprehensive Minecraft dropper plugin with multi-level maps, checkpoint system, persistent storage, and player statistics tracking.

## Features

- **Multiple Dropper Maps**: Create unlimited dropper maps with unique names
- **Multi-Level Progression**: Each map supports unlimited levels with automatic progression
- **Checkpoint System**: Set checkpoints within levels to help players
- **Persistent Storage**: All maps are saved to JSON files and automatically loaded on server start
- **Player Statistics**: Track plays, completions, and best times for each map
- **Auto-Save**: Automatic periodic saving of maps and statistics
- **Configurable Gameplay**: Customize damage, teleportation, inventory handling, and more
- **Rewards System**: Execute commands when players complete maps
- **Tab Completion**: Full tab completion support for all commands
- **Permission System**: Granular permissions for different actions

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/dropper create <name>` | Create a new dropper map | `dropper.create` |
| `/dropper delete <name>` | Delete a dropper map | `dropper.delete` |
| `/dropper start <name>` | Start playing a dropper map | `dropper.use` |
| `/dropper stop` | Stop playing and return to original location | `dropper.use` |
| `/dropper setlevel <map> <level> <start\|end>` | Set start or end location for a level | `dropper.setlevel` |
| `/dropper checkpoint <map> <level>` | Set a checkpoint for a level | `dropper.checkpoint` |
| `/dropper list` | List all available dropper maps | `dropper.use` |
| `/dropper stats <map>` | View your statistics for a specific map | `dropper.use` |
| `/dropper tp <map> <level>` | Teleport to a specific level (admin) | `dropper.tp` |

**Aliases**: `/drop`, `/dr`

## Permissions

- `dropper.use` - Basic dropper commands (default: true)
- `dropper.admin` - All admin permissions (default: op)
- `dropper.create` - Create dropper maps (default: op)
- `dropper.delete` - Delete dropper maps (default: op)
- `dropper.setlevel` - Set level locations (default: op)
- `dropper.checkpoint` - Set checkpoints (default: op)
- `dropper.tp` - Teleport to maps/levels (default: op)

## Setup Guide

### 1. Creating a Dropper Map

1. Create a new map:
   ```
   /dropper create MyDropperMap
   ```

2. Set the start location for level 1 (stand at the spawn point):
   ```
   /dropper setlevel MyDropperMap 1 start
   ```

3. Set the end location for level 1 (stand at the finish point):
   ```
   /dropper setlevel MyDropperMap 1 end
   ```

4. (Optional) Set a checkpoint for level 1:
   ```
   /dropper checkpoint MyDropperMap 1
   ```

5. Repeat steps 2-4 for additional levels (level 2, 3, etc.)

### 2. Playing a Dropper Map

Players can start a map with:
```
/dropper start MyDropperMap
```

To stop playing:
```
/dropper stop
```

### 3. Viewing Statistics

Check your stats for a specific map:
```
/dropper stats MyDropperMap
```

## Configuration

The `config.yml` file allows you to customize various aspects of the plugin:

### General Settings
- `auto-save-interval`: How often to auto-save (in seconds)
- `debug`: Enable debug messages
- `teleport-on-fall`: Teleport players to checkpoint when they fall too far
- `clear-inventory`: Clear player inventory when starting a dropper
- `restore-inventory`: Restore inventory when leaving a dropper
- `enable-stats`: Enable statistics tracking

### Gameplay Settings
- `min-y-level`: Minimum Y level before teleporting to checkpoint
- `allow-flight`: Allow players to fly in dropper
- `damage-multiplier`: Control fall damage (0.0 = no damage, 1.0 = normal)
- `time-limit`: Time limit per level in seconds (0 = no limit)

### Rewards
- `rewards.enabled`: Enable reward commands on completion
- `rewards.completion-commands`: Commands to execute (use %player% placeholder)

### Messages
All messages are fully customizable with color codes support (`&` symbol).

## File Storage

The plugin stores data in the following structure:

```
plugins/DropperPlugin/
├── config.yml           # Plugin configuration
├── statistics.json      # Player statistics
└── maps/               # Dropper maps directory
    ├── Map1.json       # Individual map files
    ├── Map2.json
    └── ...
```

All data is automatically saved and loaded, ensuring no data loss.

## How It Works

### Level Progression
1. Player teleports to level 1 start location
2. Player falls through the dropper course
3. When player reaches the end location (within 2 blocks), they progress to the next level
4. If player falls below the minimum Y level, they teleport to the checkpoint (or level start)
5. Process repeats until all levels are completed

### Checkpoints
- Checkpoints are optional save points within a level
- If a player falls, they return to the checkpoint instead of level start
- Set checkpoints at challenging sections to improve player experience

### Statistics
The plugin tracks:
- **Total Plays**: How many times you've attempted the map
- **Completions**: How many times you've completed the map
- **Best Time**: Your fastest completion time

### Session Management
When a player starts a dropper:
- Current location is saved
- Current gamemode is saved
- Inventory is saved and cleared (configurable)
- Player is teleported to level 1

When a player stops or completes:
- Original location is restored
- Original gamemode is restored
- Inventory is restored (configurable)

## Building

Build the plugin using Maven:

```bash
mvn clean package
```

The compiled JAR file will be in `target/dropper-plugin-1.0.0.jar`

## Requirements

- **Minecraft Version**: 1.21.1 (compatible with Paper/Spigot)
- **Java Version**: 21 or higher
- **Server Software**: Paper, Spigot, or Bukkit

## Support

For issues, suggestions, or contributions, please visit the GitHub repository.

## License

This plugin is provided as-is for use on Minecraft servers.

---

**Version**: 1.0.0
**Author**: Noctivag
**API**: Paper 1.21.1
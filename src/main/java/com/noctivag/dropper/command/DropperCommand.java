package com.noctivag.dropper.command;

import com.noctivag.dropper.DropperPlugin;
import com.noctivag.dropper.model.DropperLevel;
import com.noctivag.dropper.model.DropperMap;
import com.noctivag.dropper.model.DropperSession;
import com.noctivag.dropper.model.PlayerStatistics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main command handler for the dropper plugin
 * Commands: create, delete, start, stop, setlevel, checkpoint, list, stats, tp
 */
public class DropperCommand implements CommandExecutor, TabCompleter {

    private final DropperPlugin plugin;
    private final List<String> subcommands = Arrays.asList(
        "create", "delete", "start", "stop", "setlevel", "checkpoint", "list", "stats", "tp"
    );

    public DropperCommand(DropperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "create":
                return handleCreate(sender, args);
            case "delete":
                return handleDelete(sender, args);
            case "start":
                return handleStart(sender, args);
            case "stop":
                return handleStop(sender, args);
            case "setlevel":
                return handleSetLevel(sender, args);
            case "checkpoint":
                return handleCheckpoint(sender, args);
            case "list":
                return handleList(sender, args);
            case "stats":
                return handleStats(sender, args);
            case "tp":
                return handleTeleport(sender, args);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dropper.create")) {
            sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length < 2) {
            sendMessage(sender, "invalid-args", "%usage%", "/dropper create <mapName>");
            return true;
        }

        String mapName = args[1];
        String creator = sender instanceof Player ? sender.getName() : "Console";

        if (plugin.getMapManager().createMap(mapName, creator)) {
            sendMessage(sender, "map-created", "%map%", mapName);
        } else {
            sendMessage(sender, "map-already-exists", "%map%", mapName);
        }
        return true;
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dropper.delete")) {
            sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length < 2) {
            sendMessage(sender, "invalid-args", "%usage%", "/dropper delete <mapName>");
            return true;
        }

        String mapName = args[1];

        if (plugin.getMapManager().deleteMap(mapName)) {
            sendMessage(sender, "map-deleted", "%map%", mapName);
        } else {
            sendMessage(sender, "map-not-found", "%map%", mapName);
        }
        return true;
    }

    private boolean handleStart(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "player-only");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            sendMessage(sender, "invalid-args", "%usage%", "/dropper start <mapName>");
            return true;
        }

        String mapName = args[1];
        DropperMap map = plugin.getMapManager().getMap(mapName);

        if (map == null) {
            sendMessage(sender, "map-not-found", "%map%", mapName);
            return true;
        }

        if (!map.isComplete() || map.getLevelCount() == 0) {
            player.sendMessage(msg("prefix") + "§cThis map is not ready yet! It needs at least one complete level.");
            return true;
        }

        if (plugin.getSessionManager().hasSession(player)) {
            sendMessage(sender, "already-in-game");
            return true;
        }

        if (plugin.getSessionManager().startSession(player, map)) {
            sendMessage(sender, "game-started", "%map%", mapName);
        } else {
            player.sendMessage(msg("prefix") + "§cFailed to start the game. Please try again.");
        }
        return true;
    }

    private boolean handleStop(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "player-only");
            return true;
        }

        Player player = (Player) sender;

        if (!plugin.getSessionManager().hasSession(player)) {
            sendMessage(sender, "no-active-game");
            return true;
        }

        if (plugin.getSessionManager().stopSession(player, true)) {
            sendMessage(sender, "game-stopped");
        }
        return true;
    }

    private boolean handleSetLevel(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "player-only");
            return true;
        }

        if (!sender.hasPermission("dropper.setlevel")) {
            sendMessage(sender, "no-permission");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 4) {
            sendMessage(sender, "invalid-args", "%usage%", "/dropper setlevel <mapName> <level> <start|end>");
            return true;
        }

        String mapName = args[1];
        DropperMap map = plugin.getMapManager().getMap(mapName);

        if (map == null) {
            sendMessage(sender, "map-not-found", "%map%", mapName);
            return true;
        }

        int levelNumber;
        try {
            levelNumber = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(msg("prefix") + "§cInvalid level number!");
            return true;
        }

        String type = args[3].toLowerCase();

        if (!map.hasLevel(levelNumber)) {
            map.addLevel(new DropperLevel(levelNumber));
        }

        DropperLevel level = map.getLevel(levelNumber);

        if (type.equals("start")) {
            level.setStartLocation(player.getLocation().clone());
            player.sendMessage(msg("prefix") + "§aSet start location for level " + levelNumber);
        } else if (type.equals("end")) {
            level.setEndLocation(player.getLocation().clone());
            player.sendMessage(msg("prefix") + "§aSet end location for level " + levelNumber);
        } else {
            sendMessage(sender, "invalid-args", "%usage%", "/dropper setlevel <mapName> <level> <start|end>");
            return true;
        }

        plugin.getMapManager().saveMap(map);
        sendMessage(sender, "level-set", "%level%", String.valueOf(levelNumber), "%map%", mapName);
        return true;
    }

    private boolean handleCheckpoint(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "player-only");
            return true;
        }

        if (!sender.hasPermission("dropper.checkpoint")) {
            sendMessage(sender, "no-permission");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 3) {
            sendMessage(sender, "invalid-args", "%usage%", "/dropper checkpoint <mapName> <level>");
            return true;
        }

        String mapName = args[1];
        DropperMap map = plugin.getMapManager().getMap(mapName);

        if (map == null) {
            sendMessage(sender, "map-not-found", "%map%", mapName);
            return true;
        }

        int levelNumber;
        try {
            levelNumber = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(msg("prefix") + "§cInvalid level number!");
            return true;
        }

        if (!map.hasLevel(levelNumber)) {
            player.sendMessage(msg("prefix") + "§cLevel " + levelNumber + " doesn't exist in this map!");
            return true;
        }

        DropperLevel level = map.getLevel(levelNumber);
        level.setCheckpointLocation(player.getLocation().clone());

        plugin.getMapManager().saveMap(map);
        sendMessage(sender, "checkpoint-set", "%level%", String.valueOf(levelNumber), "%map%", mapName);
        return true;
    }

    private boolean handleList(CommandSender sender, String[] args) {
        if (plugin.getMapManager().getMaps().isEmpty()) {
            sendMessage(sender, "no-maps");
            return true;
        }

        sendMessage(sender, "map-list-header");
        for (DropperMap map : plugin.getMapManager().getMaps().values()) {
            String entry = msg("map-list-entry")
                    .replace("%map%", map.getName())
                    .replace("%levels%", String.valueOf(map.getLevelCount()));
            sender.sendMessage(entry);
        }
        return true;
    }

    private boolean handleStats(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "player-only");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            sendMessage(sender, "invalid-args", "%usage%", "/dropper stats <mapName>");
            return true;
        }

        String mapName = args[1];

        if (!plugin.getMapManager().hasMap(mapName)) {
            sendMessage(sender, "map-not-found", "%map%", mapName);
            return true;
        }

        PlayerStatistics.MapStats stats = plugin.getStatisticsManager()
                .getMapStats(player.getUniqueId(), mapName);

        sendMessage(sender, "stats-header");
        sendMessage(sender, "stats-plays", "%plays%", String.valueOf(stats.getPlays()));
        sendMessage(sender, "stats-completions", "%completions%", String.valueOf(stats.getCompletions()));
        sendMessage(sender, "stats-best-time", "%time%", stats.getFormattedBestTime());
        return true;
    }

    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "player-only");
            return true;
        }

        if (!sender.hasPermission("dropper.tp")) {
            sendMessage(sender, "no-permission");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 3) {
            sendMessage(sender, "invalid-args", "%usage%", "/dropper tp <mapName> <level>");
            return true;
        }

        String mapName = args[1];
        DropperMap map = plugin.getMapManager().getMap(mapName);

        if (map == null) {
            sendMessage(sender, "map-not-found", "%map%", mapName);
            return true;
        }

        int levelNumber;
        try {
            levelNumber = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(msg("prefix") + "§cInvalid level number!");
            return true;
        }

        DropperLevel level = map.getLevel(levelNumber);
        if (level == null || level.getStartLocation() == null) {
            player.sendMessage(msg("prefix") + "§cLevel " + levelNumber + " doesn't have a start location!");
            return true;
        }

        player.teleport(level.getStartLocation());
        player.sendMessage(msg("prefix") + "§aTeleported to level " + levelNumber + " of " + mapName);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m                §r §b§lDropper Commands §8§m                ");
        sender.sendMessage("§7/dropper create <name> §8- §fCreate a new map");
        sender.sendMessage("§7/dropper delete <name> §8- §fDelete a map");
        sender.sendMessage("§7/dropper start <name> §8- §fStart playing a map");
        sender.sendMessage("§7/dropper stop §8- §fStop playing");
        sender.sendMessage("§7/dropper setlevel <map> <level> <start|end> §8- §fSet level location");
        sender.sendMessage("§7/dropper checkpoint <map> <level> §8- §fSet checkpoint");
        sender.sendMessage("§7/dropper list §8- §fList all maps");
        sender.sendMessage("§7/dropper stats <map> §8- §fView your statistics");
        sender.sendMessage("§7/dropper tp <map> <level> §8- §fTeleport to a level");
    }

    private void sendMessage(CommandSender sender, String key, String... replacements) {
        String message = msg(key);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        sender.sendMessage(message);
    }

    private String msg(String key) {
        String prefix = plugin.getConfig().getString("messages.prefix", "§8[§bDropper§8]§r ");
        String message = plugin.getConfig().getString("messages." + key, "");

        if (key.equals("prefix")) {
            return prefix.replace("&", "§");
        }

        return (prefix + message).replace("&", "§");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            return subcommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            if (Arrays.asList("delete", "start", "setlevel", "checkpoint", "stats", "tp").contains(subcommand)) {
                return plugin.getMapManager().getMapNames().stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("setlevel")) {
            return Arrays.asList("start", "end").stream()
                    .filter(s -> s.startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return completions;
    }
}

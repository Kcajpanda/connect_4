package dev.jackb.connectfour;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

final class ConnectFourCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of("drop", "reset", "reload", "status", "scan", "log");

    private final ConnectFourPlugin plugin;

    ConnectFourCommand(ConnectFourPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "drop" -> drop(sender, label, args);
                case "reset", "clear" -> reset(sender);
                case "reload" -> reload(sender);
                case "status" -> status(sender);
                case "scan" -> scan(sender);
                case "log" -> log(sender, args);
                default -> sendUsage(sender, label);
            }
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(color("&c" + exception.getMessage()));
        }
        return true;
    }

    private void drop(CommandSender sender, String label, String[] args) {
        if (!hasPermission(sender, "connectfour.play")) {
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(color("&cUsage: /" + label + " drop <column> [player|selector]"));
            return;
        }

        int column;
        try {
            column = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Column must be a number.");
        }

        String actor = args.length >= 3 ? resolveActor(sender, args[2]) : resolveActor(sender, null);
        plugin.game().drop(sender, column, actor);
    }

    private void reset(CommandSender sender) {
        if (!hasPermission(sender, "connectfour.admin")) {
            return;
        }
        plugin.game().resetBoard(true);
    }

    private void reload(CommandSender sender) {
        if (!hasPermission(sender, "connectfour.admin")) {
            return;
        }
        try {
            plugin.reloadGame();
            sender.sendMessage(color("&aConnect Four config reloaded."));
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(color("&cReload failed: " + exception.getMessage()));
        }
    }

    private void status(CommandSender sender) {
        if (!hasPermission(sender, "connectfour.status")) {
            return;
        }
        sender.sendMessage(plugin.game().statusLine());
    }

    private void scan(CommandSender sender) {
        if (!hasPermission(sender, "connectfour.admin")) {
            return;
        }
        plugin.game().scanFromWorld();
        sender.sendMessage(color("&aScanned the configured board blocks into memory."));
    }

    private void log(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "connectfour.admin")) {
            return;
        }
        int count = 10;
        if (args.length >= 2) {
            try {
                count = Math.max(1, Integer.parseInt(args[1]));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Log count must be a number.");
            }
        }
        List<String> lines = plugin.game().moveLog(count);
        if (lines.isEmpty()) {
            sender.sendMessage(color("&7No moves have been recorded."));
            return;
        }
        lines.forEach(sender::sendMessage);
    }

    private String resolveActor(CommandSender sender, String token) {
        if (token == null || token.isBlank()) {
            return sender instanceof Player player ? player.getName() : null;
        }

        if (token.startsWith("@")) {
            List<Entity> selected;
            try {
                selected = Bukkit.selectEntities(sender, token);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Could not resolve selector '" + token + "'.");
            }

            List<Player> players = selected.stream()
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .toList();
            if (players.isEmpty()) {
                throw new IllegalArgumentException("Selector '" + token + "' did not find a player.");
            }
            return nearestPlayer(sender, players).getName();
        }

        Player online = Bukkit.getPlayerExact(token);
        return online == null ? token : online.getName();
    }

    private Player nearestPlayer(CommandSender sender, List<Player> players) {
        Location origin = null;
        if (sender instanceof BlockCommandSender blockSender) {
            origin = blockSender.getBlock().getLocation().add(0.5, 0.5, 0.5);
        } else if (sender instanceof Player player) {
            origin = player.getLocation();
        }
        if (origin == null) {
            return players.getFirst();
        }

        Location finalOrigin = origin;
        return players.stream()
            .filter(player -> player.getWorld().equals(finalOrigin.getWorld()))
            .min(Comparator.comparingDouble(player -> player.getLocation().distanceSquared(finalOrigin)))
            .orElse(players.getFirst());
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        if (sender instanceof ConsoleCommandSender || sender instanceof BlockCommandSender || sender.hasPermission(permission)) {
            return true;
        }
        sender.sendMessage(color("&cYou do not have permission to use that command."));
        return false;
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(color("&e/" + label + " drop <column> [player|selector]"));
        sender.sendMessage(color("&e/" + label + " reset"));
        sender.sendMessage(color("&e/" + label + " reload"));
        sender.sendMessage(color("&e/" + label + " status"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("drop")) {
            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= plugin.game().columns(); i++) {
                columns.add(Integer.toString(i));
            }
            return filter(columns, args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("drop")) {
            List<String> names = new ArrayList<>(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            names.addAll(Arrays.asList("@p", "@a", "@r"));
            return filter(names, args[2]);
        }
        return List.of();
    }

    private List<String> filter(List<String> values, String prefix) {
        String lower = prefix.toLowerCase();
        return values.stream()
            .filter(value -> value.toLowerCase().startsWith(lower))
            .toList();
    }

    private String color(String message) {
        return message.replace('&', '§');
    }
}

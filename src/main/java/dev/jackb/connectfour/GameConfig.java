package dev.jackb.connectfour;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

final class GameConfig {
    private final World world;
    private final int rows;
    private final int columns;
    private final Material playerOneBlock;
    private final Material playerTwoBlock;
    private final Material emptyBlock;
    private final boolean enforceTurnOrder;
    private final boolean allowSamePlayerForBothSides;
    private final boolean syncWorldBeforeMove;
    private final int animationTicksPerStep;
    private final boolean clearDropLocationsOnReset;
    private final Vec3i origin;
    private final Vec3i columnStep;
    private final Vec3i rowStep;
    private final Vec3i rangeMin;
    private final Vec3i rangeMax;
    private final List<Vec3i> dropLocations;
    private final List<Vec3i> fireworks;
    private final String prefix;
    private final String moveMessage;
    private final String winMessage;
    private final String drawMessage;
    private final String resetMessage;

    private GameConfig(
        World world,
        int rows,
        int columns,
        Material playerOneBlock,
        Material playerTwoBlock,
        Material emptyBlock,
        boolean enforceTurnOrder,
        boolean allowSamePlayerForBothSides,
        boolean syncWorldBeforeMove,
        int animationTicksPerStep,
        boolean clearDropLocationsOnReset,
        Vec3i origin,
        Vec3i columnStep,
        Vec3i rowStep,
        Vec3i rangeMin,
        Vec3i rangeMax,
        List<Vec3i> dropLocations,
        List<Vec3i> fireworks,
        String prefix,
        String moveMessage,
        String winMessage,
        String drawMessage,
        String resetMessage
    ) {
        this.world = world;
        this.rows = rows;
        this.columns = columns;
        this.playerOneBlock = playerOneBlock;
        this.playerTwoBlock = playerTwoBlock;
        this.emptyBlock = emptyBlock;
        this.enforceTurnOrder = enforceTurnOrder;
        this.allowSamePlayerForBothSides = allowSamePlayerForBothSides;
        this.syncWorldBeforeMove = syncWorldBeforeMove;
        this.animationTicksPerStep = animationTicksPerStep;
        this.clearDropLocationsOnReset = clearDropLocationsOnReset;
        this.origin = origin;
        this.columnStep = columnStep;
        this.rowStep = rowStep;
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
        this.dropLocations = List.copyOf(dropLocations);
        this.fireworks = List.copyOf(fireworks);
        this.prefix = prefix;
        this.moveMessage = moveMessage;
        this.winMessage = winMessage;
        this.drawMessage = drawMessage;
        this.resetMessage = resetMessage;
    }

    static GameConfig load(FileConfiguration config) {
        String worldName = config.getString("game.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new IllegalArgumentException("Configured world does not exist or is not loaded: " + worldName);
        }

        int rows = config.getInt("game.rows", 6);
        int columns = config.getInt("game.columns", 7);
        if (rows < 4 || columns < 4) {
            throw new IllegalArgumentException("Connect Four needs at least 4 rows and 4 columns.");
        }

        Material playerOneBlock = material(config.getString("game.player-one-block", "RED_CONCRETE"), "game.player-one-block");
        Material playerTwoBlock = material(config.getString("game.player-two-block", "YELLOW_CONCRETE"), "game.player-two-block");
        Material emptyBlock = material(config.getString("game.empty-block", "AIR"), "game.empty-block");

        ConfigurationSection board = requireSection(config, "board");
        Vec3i origin = Vec3i.from(board, "origin");
        Vec3i columnStep = Vec3i.from(board, "column-step");
        Vec3i rowStep = Vec3i.from(board, "row-step");
        ConfigurationSection range = requireSection(board, "range");
        Vec3i rangeMin = Vec3i.from(range, "min");
        Vec3i rangeMax = Vec3i.from(range, "max");

        List<Vec3i> dropLocations = locationList(config, "drop-locations");
        if (dropLocations.size() != columns) {
            throw new IllegalArgumentException("drop-locations must contain exactly " + columns + " entries.");
        }

        List<Vec3i> fireworks = locationList(config, "fireworks");
        GameConfig loaded = new GameConfig(
            world,
            rows,
            columns,
            playerOneBlock,
            playerTwoBlock,
            emptyBlock,
            config.getBoolean("game.enforce-turn-order", true),
            config.getBoolean("game.allow-same-player-for-both-sides", false),
            config.getBoolean("game.sync-world-before-move", true),
            Math.max(1, config.getInt("game.animation-ticks-per-step", 4)),
            config.getBoolean("game.clear-drop-locations-on-reset", true),
            origin,
            columnStep,
            rowStep,
            rangeMin,
            rangeMax,
            dropLocations,
            fireworks,
            config.getString("messages.prefix", "&6[Connect Four]&r "),
            config.getString("messages.move", "&e%player%&7 dropped in column &f%column%&7."),
            config.getString("messages.win", "&a%player% wins Connect Four!"),
            config.getString("messages.draw", "&eThe board is full. It is a draw."),
            config.getString("messages.reset", "&7The Connect Four board has been reset.")
        );
        loaded.validateBoardRange();
        return loaded;
    }

    private static ConfigurationSection requireSection(ConfigurationSection section, String path) {
        ConfigurationSection child = section.getConfigurationSection(path);
        if (child == null) {
            throw new IllegalArgumentException("Missing config section: " + section.getCurrentPath() + "." + path);
        }
        return child;
    }

    private static Material material(String name, String path) {
        Material material = Material.matchMaterial(name == null ? "" : name);
        if (material == null) {
            throw new IllegalArgumentException("Invalid material at " + path + ": " + name);
        }
        return material;
    }

    private static List<Vec3i> locationList(FileConfiguration config, String path) {
        List<?> raw = config.getList(path);
        if (raw == null) {
            throw new IllegalArgumentException("Missing location list: " + path);
        }
        List<Vec3i> locations = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            Object item = raw.get(i);
            if (item instanceof ConfigurationSection section) {
                locations.add(Vec3i.from(section));
                continue;
            }
            if (item instanceof Map<?, ?> map) {
                locations.add(new Vec3i(number(map, "x", path, i), number(map, "y", path, i), number(map, "z", path, i)));
                continue;
            }
            ConfigurationSection section = config.getConfigurationSection(path + "." + i);
            if (section != null) {
                locations.add(Vec3i.from(section));
                continue;
            }
            throw new IllegalArgumentException("Invalid location at " + path + "[" + i + "]");
        }
        return locations;
    }

    private static int number(Map<?, ?> map, String key, String path, int index) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new IllegalArgumentException("Invalid or missing " + key + " value at " + path + "[" + index + "]");
    }

    private void validateBoardRange() {
        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                Vec3i position = boardPosition(column, row);
                if (!position.inside(rangeMin, rangeMax)) {
                    throw new IllegalArgumentException("Board slot " + (column + 1) + "," + (row + 1)
                        + " at " + position + " is outside board.range.");
                }
            }
        }
    }

    Vec3i boardPosition(int column, int row) {
        return origin.plus(columnStep.times(column)).plus(rowStep.times(row));
    }

    Vec3i dropPosition(int column) {
        return dropLocations.get(column);
    }

    World world() {
        return world;
    }

    int rows() {
        return rows;
    }

    int columns() {
        return columns;
    }

    Material playerOneBlock() {
        return playerOneBlock;
    }

    Material playerTwoBlock() {
        return playerTwoBlock;
    }

    Material emptyBlock() {
        return emptyBlock;
    }

    boolean enforceTurnOrder() {
        return enforceTurnOrder;
    }

    boolean allowSamePlayerForBothSides() {
        return allowSamePlayerForBothSides;
    }

    boolean syncWorldBeforeMove() {
        return syncWorldBeforeMove;
    }

    int animationTicksPerStep() {
        return animationTicksPerStep;
    }

    boolean clearDropLocationsOnReset() {
        return clearDropLocationsOnReset;
    }

    List<Vec3i> fireworks() {
        return fireworks;
    }

    String prefix() {
        return prefix;
    }

    String moveMessage() {
        return moveMessage;
    }

    String winMessage() {
        return winMessage;
    }

    String drawMessage() {
        return drawMessage;
    }

    String resetMessage() {
        return resetMessage;
    }
}

package dev.jackb.connectfour;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

final class ConnectFourGame {
    private final ConnectFourPlugin plugin;
    private final GameConfig config;
    private final Disc[][] board;
    private final List<MoveRecord> moves = new ArrayList<>();
    private String playerOneName;
    private String playerTwoName;
    private int currentPlayer = 1;
    private boolean gameOver;
    private boolean animating;

    ConnectFourGame(ConnectFourPlugin plugin, GameConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.board = new Disc[config.rows()][config.columns()];
        clearMemory();
    }

    int columns() {
        return config.columns();
    }

    void drop(CommandSender sender, int oneBasedColumn, String actorName) {
        if (oneBasedColumn < 1 || oneBasedColumn > config.columns()) {
            sender.sendMessage(color("&cColumn must be between 1 and " + config.columns() + "."));
            return;
        }
        if (animating) {
            sender.sendMessage(color("&cWait for the current token to finish dropping."));
            return;
        }
        if (gameOver) {
            sender.sendMessage(color("&cThe game is over. Run /connectfour reset to start again."));
            return;
        }
        if (config.syncWorldBeforeMove()) {
            scanFromWorld();
        }

        int column = oneBasedColumn - 1;
        int row = firstOpenRow(column);
        if (row == -1) {
            sender.sendMessage(color("&cColumn " + oneBasedColumn + " is full."));
            return;
        }

        Disc disc = currentPlayer == 1 ? Disc.PLAYER_ONE : Disc.PLAYER_TWO;
        String displayName;
        try {
            displayName = bindPlayer(disc, actorName);
        } catch (IllegalStateException exception) {
            sender.sendMessage(color("&c" + exception.getMessage()));
            return;
        }

        animateDrop(column, row, disc, displayName);
    }

    void resetBoard(boolean clearBlocks) {
        clearMemory();
        if (clearBlocks) {
            for (int column = 0; column < config.columns(); column++) {
                for (int row = 0; row < config.rows(); row++) {
                    setBlock(config.boardPosition(column, row), config.emptyBlock());
                }
                if (config.clearDropLocationsOnReset()) {
                    setBlock(config.dropPosition(column), Material.AIR);
                }
            }
        }
        broadcast(config.resetMessage());
    }

    void scanFromWorld() {
        int playerOneCount = 0;
        int playerTwoCount = 0;
        for (int column = 0; column < config.columns(); column++) {
            for (int row = 0; row < config.rows(); row++) {
                Material material = blockType(config.boardPosition(column, row));
                Disc disc = discForMaterial(material);
                board[row][column] = disc;
                if (disc == Disc.PLAYER_ONE) {
                    playerOneCount++;
                } else if (disc == Disc.PLAYER_TWO) {
                    playerTwoCount++;
                }
            }
        }
        currentPlayer = playerOneCount <= playerTwoCount ? 1 : 2;
        gameOver = winningDisc() != Disc.EMPTY || playerOneCount + playerTwoCount == config.rows() * config.columns();
    }

    String statusLine() {
        int playerOneCount = count(Disc.PLAYER_ONE);
        int playerTwoCount = count(Disc.PLAYER_TWO);
        String turn = gameOver ? "game over" : "Player " + currentPlayer + " turn";
        return color(config.prefix() + "&7Status: &f" + turn + "&7, &cP1=" + playerOneCount + "&7, &eP2=" + playerTwoCount);
    }

    List<String> moveLog(int count) {
        int start = Math.max(0, moves.size() - count);
        List<String> lines = new ArrayList<>();
        for (int i = start; i < moves.size(); i++) {
            MoveRecord move = moves.get(i);
            lines.add(color("&7#" + move.moveNumber() + " &f" + move.playerName()
                + " &7column &f" + move.column()
                + " &7row &f" + move.row()
                + " &8(" + move.placedAt() + ")"));
        }
        return lines;
    }

    private void animateDrop(int column, int targetRow, Disc disc, String displayName) {
        animating = true;
        Material material = materialForDisc(disc);
        Vec3i targetPosition = config.boardPosition(column, targetRow);
        List<Vec3i> path = new ArrayList<>();
        path.add(config.dropPosition(column));
        for (int row = config.rows() - 1; row >= targetRow; row--) {
            path.add(config.boardPosition(column, row));
        }

        new BukkitRunnable() {
            private int index;
            private Vec3i previous;

            @Override
            public void run() {
                if (previous != null && !previous.equals(targetPosition)) {
                    setBlock(previous, config.emptyBlock());
                }
                if (index >= path.size()) {
                    finishMove(column, targetRow, disc, displayName);
                    animating = false;
                    cancel();
                    return;
                }
                Vec3i current = path.get(index++);
                setBlock(current, material);
                previous = current;
            }
        }.runTaskTimer(plugin, 0L, config.animationTicksPerStep());
    }

    private void finishMove(int column, int row, Disc disc, String displayName) {
        board[row][column] = disc;
        MoveRecord record = new MoveRecord(moves.size() + 1, column + 1, row + 1, disc, displayName, Instant.now());
        moves.add(record);

        broadcast(config.moveMessage()
            .replace("%player%", displayName)
            .replace("%color%", discLabel(disc))
            .replace("%column%", Integer.toString(column + 1))
            .replace("%row%", Integer.toString(row + 1)));

        if (winningDisc() == disc) {
            gameOver = true;
            launchFireworks(disc);
            broadcast(config.winMessage().replace("%player%", displayName));
            return;
        }

        if (isBoardFull()) {
            gameOver = true;
            broadcast(config.drawMessage());
            return;
        }

        currentPlayer = currentPlayer == 1 ? 2 : 1;
    }

    private String bindPlayer(Disc disc, String actorName) {
        String cleanActor = actorName == null || actorName.isBlank() ? null : actorName.trim();
        if (disc == Disc.PLAYER_ONE) {
            playerOneName = bindExisting(playerOneName, cleanActor, "Player 1");
            enforceExpectedPlayer(playerOneName, cleanActor);
            return playerOneName;
        }

        String candidate = cleanActor == null ? "Player 2" : cleanActor;
        if (!config.allowSamePlayerForBothSides() && playerOneName != null && playerOneName.equalsIgnoreCase(candidate)) {
            throw new IllegalStateException("Player two must be a different player.");
        }
        playerTwoName = bindExisting(playerTwoName, cleanActor, "Player 2");
        enforceExpectedPlayer(playerTwoName, cleanActor);
        return playerTwoName;
    }

    private String bindExisting(String existing, String actorName, String fallback) {
        if (existing == null) {
            return actorName == null ? fallback : actorName;
        }
        if (existing.equals(fallback) && actorName != null) {
            return actorName;
        }
        return existing;
    }

    private void enforceExpectedPlayer(String expected, String actorName) {
        if (config.enforceTurnOrder() && actorName != null && !expected.equalsIgnoreCase(actorName)) {
            throw new IllegalStateException("It is " + expected + "'s turn.");
        }
    }

    private int firstOpenRow(int column) {
        for (int row = 0; row < config.rows(); row++) {
            if (board[row][column] == Disc.EMPTY) {
                return row;
            }
        }
        return -1;
    }

    private Disc winningDisc() {
        int[][] directions = {
            {1, 0},
            {0, 1},
            {1, 1},
            {1, -1}
        };

        for (int row = 0; row < config.rows(); row++) {
            for (int column = 0; column < config.columns(); column++) {
                Disc disc = board[row][column];
                if (disc == Disc.EMPTY) {
                    continue;
                }
                for (int[] direction : directions) {
                    if (hasFourFrom(column, row, direction[0], direction[1], disc)) {
                        return disc;
                    }
                }
            }
        }
        return Disc.EMPTY;
    }

    private boolean hasFourFrom(int column, int row, int columnStep, int rowStep, Disc disc) {
        for (int offset = 1; offset < 4; offset++) {
            int nextColumn = column + columnStep * offset;
            int nextRow = row + rowStep * offset;
            if (nextColumn < 0 || nextColumn >= config.columns() || nextRow < 0 || nextRow >= config.rows()) {
                return false;
            }
            if (board[nextRow][nextColumn] != disc) {
                return false;
            }
        }
        return true;
    }

    private boolean isBoardFull() {
        for (int column = 0; column < config.columns(); column++) {
            if (board[config.rows() - 1][column] == Disc.EMPTY) {
                return false;
            }
        }
        return true;
    }

    private int count(Disc disc) {
        int count = 0;
        for (int row = 0; row < config.rows(); row++) {
            for (int column = 0; column < config.columns(); column++) {
                if (board[row][column] == disc) {
                    count++;
                }
            }
        }
        return count;
    }

    private void launchFireworks(Disc disc) {
        Color color = disc == Disc.PLAYER_ONE ? Color.RED : Color.YELLOW;
        for (Vec3i fireworkLocation : config.fireworks()) {
            Location location = fireworkLocation.toLocation(config.world()).add(0.5, 0.5, 0.5);
            Firework firework = (Firework) config.world().spawnEntity(location, EntityType.FIREWORK_ROCKET);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                .withColor(color)
                .withFade(Color.WHITE)
                .with(FireworkEffect.Type.BALL_LARGE)
                .trail(true)
                .flicker(true)
                .build());
            meta.setPower(1);
            firework.setFireworkMeta(meta);
        }
    }

    private void clearMemory() {
        for (int row = 0; row < config.rows(); row++) {
            for (int column = 0; column < config.columns(); column++) {
                board[row][column] = Disc.EMPTY;
            }
        }
        moves.clear();
        playerOneName = null;
        playerTwoName = null;
        currentPlayer = 1;
        gameOver = false;
        animating = false;
    }

    private Disc discForMaterial(Material material) {
        if (material == config.playerOneBlock()) {
            return Disc.PLAYER_ONE;
        }
        if (material == config.playerTwoBlock()) {
            return Disc.PLAYER_TWO;
        }
        return Disc.EMPTY;
    }

    private Material materialForDisc(Disc disc) {
        return switch (disc) {
            case PLAYER_ONE -> config.playerOneBlock();
            case PLAYER_TWO -> config.playerTwoBlock();
            case EMPTY -> config.emptyBlock();
        };
    }

    private String discLabel(Disc disc) {
        Material material = materialForDisc(disc);
        return material.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private Material blockType(Vec3i position) {
        return position.toLocation(config.world()).getBlock().getType();
    }

    private void setBlock(Vec3i position, Material material) {
        position.toLocation(config.world()).getBlock().setType(material, false);
    }

    private void broadcast(String message) {
        Bukkit.broadcastMessage(color(config.prefix() + message));
    }

    private String color(String message) {
        return message.replace('&', '§');
    }
}

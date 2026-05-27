package dev.jackb.connectfour;

import java.time.Instant;

record MoveRecord(int moveNumber, int column, int row, Disc disc, String playerName, Instant placedAt) {
}

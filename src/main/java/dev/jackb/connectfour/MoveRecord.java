package dev.jackb.connectfour;

import java.time.Instant;

/**
 * One recorded Connect Four move.
 *
 * @param moveNumber one-based move number
 * @param column one-based column number recorded for display
 * @param row one-based row number recorded for display
 * @param disc player token that was placed
 * @param playerName player credited for the move
 * @param placedAt timestamp when the move completed
 */
record MoveRecord(int moveNumber, int column, int row, Disc disc, String playerName, Instant placedAt) {
}

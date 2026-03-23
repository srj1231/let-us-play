package com.game.let_us_play.common;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable value object representing a single move in the game.
 * - Moves are facts. Once made, they don't change.
 * - Safe to pass across threads, queues, and network boundaries without copying.
 * - Can be used directly as event log entries (event sourcing).
 * Replaying all moves for a gameId reconstructs full game state.
 */
@Getter
public final class Move {

    private final String playerId;
    private final int row;
    private final int col;
    private final Symbol symbol;
    private final Instant timestamp;   // for event log ordering and audit

    public Move(String playerId, int row, int col, Symbol symbol) {
        validateCoordinates(row, col);
        Objects.requireNonNull(playerId, "playerId must not be null");
        Objects.requireNonNull(symbol, "symbol must not be null");

        this.playerId  = playerId;
        this.row       = row;
        this.col       = col;
        this.symbol    = symbol;
        this.timestamp = Instant.now();
    }

    private void validateCoordinates(int row, int col) {
        if (row < 0 || col < 0) {
            throw new IllegalArgumentException(
                    "Row and col must be non-negative. Got: row=" + row + ", col=" + col
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return row == move.row &&
                col == move.col &&
                Objects.equals(playerId, move.playerId) &&
                symbol == move.symbol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, row, col, symbol);
    }

    @Override
    public String toString() {
        return String.format("Move{player=%s, row=%d, col=%d, symbol=%s, at=%s}",
                playerId, row, col, symbol, timestamp);
    }
}

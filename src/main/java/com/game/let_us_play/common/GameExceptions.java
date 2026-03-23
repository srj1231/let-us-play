package com.game.let_us_play.common;

import lombok.Getter;

/**
 * Domain exceptions: one per distinct failure mode.
 */
public class GameExceptions {
    private GameExceptions() {}

    /** Thrown when a player attempts to mark a cell that is already occupied. */
    @Getter
    public static class CellAlreadyOccupiedException extends RuntimeException {
        private final int row;
        private final int col;

        public CellAlreadyOccupiedException(int row, int col) {
            super(String.format("Cell (%d, %d) is already occupied.", row, col));
            this.row = row;
            this.col = col;
        }

    }

    /** Thrown when a move is made on a game that has already ended. */
    @Getter
    public static class GameAlreadyOverException extends RuntimeException {
        private final String gameId;

        public GameAlreadyOverException(String gameId) {
            super("Game " + gameId + " has already concluded.");
            this.gameId = gameId;
        }

    }

    /** Thrown when coordinates fall outside the board dimensions. */
    public static class InvalidMoveException extends RuntimeException {
        public InvalidMoveException(String message) {
            super(message);
        }
    }

    /** Thrown when a game cannot be found by ID — e.g. in GameRepository. */
    @Getter
    public static class GameNotFoundException extends RuntimeException {
        private final String gameId;

        public GameNotFoundException(String gameId) {
            super("Game not found: " + gameId);
            this.gameId = gameId;
        }

    }

    /** Thrown when no players are available for matchmaking. */
    public static class MatchmakingException extends RuntimeException {
        public MatchmakingException(String message) {
            super(message);
        }
    }
}

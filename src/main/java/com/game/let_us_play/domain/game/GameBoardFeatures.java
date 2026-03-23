package com.game.let_us_play.domain.game;

import com.game.let_us_play.domain.board.Board;

/**
 * Contract for board initialisation and display.
 *
 * Design decisions:
 * - Separated from BoardFeatures intentionally.
 *   BoardFeatures = judgement (called every turn).
 *   GameBoardFeatures = setup + display (called once at start,
 *   and whenever the board needs to be shown).
 *
 * - initBoard() returns a new Board — the factory method
 *   pattern. Game doesn't call new Board() directly.
 *   This means board creation logic is swappable —
 *   a ToroidalBoard, a HexBoard, a CustomBoard can all
 *   be returned here without Game knowing the difference.
 *
 * - printBoard() takes a Board and returns a String —
 *   pure function, no side effects. The caller (Game or
 *   GameController) decides what to do with the string
 *   (log it, send it in API response, etc.)
 *
 * Implementations:
 * - StandardBoardSetup — square grid, standard display (Stage 1)
 * - Future: HexBoardSetup, ToroidalBoardSetup (Stage 2+)
 */

public interface GameBoardFeatures {

    /**
     * Creates and returns a fresh board of the given size.
     *
     * @param size number of rows and columns (e.g. 3 for 3x3)
     * @return a new, empty Board ready for play
     */
    Board initBoard(int size);

    /**
     * Returns a human-readable string representation of the board.
     * Used for logging, API responses, and future console play.
     *
     * @param board the board to display
     * @return formatted string representation
     */
    String printBoard(Board board);
}

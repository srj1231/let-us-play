package com.game.let_us_play.domain.game;

import com.game.let_us_play.common.Move;
import com.game.let_us_play.domain.board.Board;

/**
 * Contract for win and draw detection.
 * <p>
 * Design decisions:
 * - Separated from Game intentionally — Game orchestrates,
 *   BoardFeatures judges. Single Responsibility.
 * <p>
 * - Takes the last Move as a parameter for checkWin() —
 *   this is the O(n) optimisation. We only check the row,
 *   column, and diagonals that the last move touched.
 *   Not the entire board. For a 3x3 that's 9 cells max.
 *   For a 10x10 it's still 10+10+10+10 = 40 cells max,
 *   not 100.
 * <p>
 * - isDraw() takes only Board - draw detection doesn't
 *   need to know about the last move, just whether the
 *   board is full without a winner.
 * <p>
 * Implementations:
 * - RowColDiagStrategy — standard win detection (Stage 1)
 * - Future: ConnectFourStrategy, CustomWinLengthStrategy (Stage 2)
 */
public interface BoardFeatures {

    /**
     * Checks if the last move resulted in a win.
     * <p>
     * Only checks lines that pass through the last move's
     * position — not the entire board.
     *
     * @param board    current board state
     * @param lastMove the move just made
     * @return true if the player who made lastMove has won
     */
    boolean checkWin(Board board, Move lastMove);

    /**
     * Checks if the game is a draw.
     * Called only after checkWin() returns false.
     *
     * @param board current board state
     * @return true if board is full and no winner
     */
    boolean isDraw(Board board);
}

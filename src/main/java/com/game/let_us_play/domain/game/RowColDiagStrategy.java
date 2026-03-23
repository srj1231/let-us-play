package com.game.let_us_play.domain.game;

import com.game.let_us_play.common.Move;
import com.game.let_us_play.common.Symbol;
import com.game.let_us_play.domain.board.Board;
import org.springframework.stereotype.Component;

/**
 * Standard win detection — checks the row, column, and
 * diagonals touched by the last move.
 * <p>
 * Design decisions:
 * - O(n) not O(n²) — we only check lines that pass through
 *   the last move's cell. Maximum cells checked = 4n-2
 *   (row + col + 2 diagonals). Never the full board.
 * <p>
 * - Diagonal check is conditional — only checks a diagonal
 *   if the last move's cell actually lies on that diagonal.
 *   Main diagonal: row == col.
 *   Anti-diagonal: row + col == size - 1.
 *   Skipping irrelevant diagonals avoids false checks.
 * <p>
 * - @Component("rowColDiagStrategy") — named Spring bean
 *   so it can be injected by name when multiple
 *   BoardFeatures implementations exist in Stage 2.
 * <p>
 * - isDraw() delegates to board.isFull() — draw is simply
 *   "board full, no winner." Called only after checkWin()
 *   returns false so no double-checking needed.
 */
@Component("rowColDiagStrategy")
public class RowColDiagStrategy implements BoardFeatures {

    @Override
    public boolean checkWin(Board board, Move lastMove) {
        int row    = lastMove.getRow();
        int col    = lastMove.getCol();
        Symbol sym = lastMove.getSymbol();
        int size   = board.getSize();

        return checkRow(board, row, sym, size)
                || checkCol(board, col, sym, size)
                || checkMainDiag(board, sym, size, row, col)
                || checkAntiDiag(board, sym, size, row, col);
    }

    @Override
    public boolean isDraw(Board board) {
        return board.isFull();
    }

    /**
     * Checks all cells in the row of the last move.
     * Win if every cell in that row has the same symbol.
     */
    private boolean checkRow(Board board, int row, Symbol sym, int size) {
        for (int col = 0; col < size; col++) {
            if (board.getCell(row, col).getSymbol() != sym) return false;
        }
        return true;
    }

    /**
     * Checks all cells in the column of the last move.
     */
    private boolean checkCol(Board board, int col, Symbol sym, int size) {
        for (int row = 0; row < size; row++) {
            if (board.getCell(row, col).getSymbol() != sym) return false;
        }
        return true;
    }

    /**
     * Checks the main diagonal (top-left to bottom-right).
     * Only relevant if the last move lies on this diagonal: row == col.
     */
    private boolean checkMainDiag(Board board, Symbol sym, int size, int row, int col) {
        if (row != col) return false;   // move not on main diagonal — skip
        for (int i = 0; i < size; i++) {
            if (board.getCell(i, i).getSymbol() != sym) return false;
        }
        return true;
    }

    /**
     * Checks the anti-diagonal (top-right to bottom-left).
     * Only relevant if the last move lies on this diagonal: row + col == size - 1.
     */
    private boolean checkAntiDiag(Board board, Symbol sym, int size, int row, int col) {
        if (row + col != size - 1) return false;   // move not on anti-diagonal — skip
        for (int i = 0; i < size; i++) {
            if (board.getCell(i, size - 1 - i).getSymbol() != sym) return false;
        }
        return true;
    }
}

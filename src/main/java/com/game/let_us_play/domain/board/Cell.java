package com.game.let_us_play.domain.board;

import com.game.let_us_play.common.Symbol;
import lombok.Getter;

/**
 * Represents a single cell on the game board.
 * Row and col are stored on Cell — makes win detection and board printing cleaner.
 */
@Getter
public class Cell {
    private final int row;
    private final int col;
    private Symbol symbol;

    public Cell(int row, int col, Symbol symbol) {
        this.row = row;
        this.col = col;
        this.symbol = null;
    }

    public boolean isEmpty() {
        return symbol == null;
    }

    /**
     * Mark the cell with given symbol.
     * @param symbol - symbol type
     */
    public void mark(Symbol symbol) {
        if (!isEmpty()) {
            throw new IllegalStateException(
                    String.format("Cell (%d, %d) is already occupied by %s", row, col, this.symbol)
            );
        }
        this.symbol = symbol;
    }

    /**
     * Used when simulating moves.
     * Need to place and undo moves on a board without creating new objects.
     */
    public void reset() {
        this.symbol = null;
    }

    @Override
    public String toString() {
        return isEmpty() ? "." : symbol.getDisplay();
    }
}

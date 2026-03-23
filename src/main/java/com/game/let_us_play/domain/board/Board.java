package com.game.let_us_play.domain.board;

import com.game.let_us_play.common.GameExceptions;
import com.game.let_us_play.common.Symbol;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the game grid.
 * Board owns its cells (composition). Cells are created in the
 *   constructor and die with the board.
 * Board is dumb. It knows how to store and retrieve cells,
 *   mark them, and report fullness. It does NOT know about
 *   win conditions, players, or game rules. That's BoardFeatures' job.
 * Size is configurable: supports 3x3 (Tic-Tac-Toe),
 *   but also 10x10 for future variants. Nothing is hardcoded.
 * Grid is a 2D array of Cell - direct O(1) access by row/col.
 */
public class Board {
    @Getter
    private final int size;
    private final Cell[][] grid;
    private int markedCellsCount;

    public Board(int size) {
        if (size < 3) {
            throw new IllegalArgumentException("Board size must be greater than or equal to 3. Got " + size);
        }
        this.size = size;
        this.grid = new Cell[size][size];
        this.markedCellsCount = 0;
        initGrid();
    }

    private void initGrid() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                grid[row][col] = new Cell(row, col, null);
            }
        }
    }

    /**
     * Marks the cell at (row, col) with the given symbol.
     * Validates bounds and occupancy before marking.
     * Increments markedCellsCount for O(1) isFull() checks.
     */
    public void markCell(int row, int col, Symbol symbol) {
        validateBounds(row, col);

        Cell cell = grid[row][col];

        if (!cell.isEmpty()) {
            throw new GameExceptions.CellAlreadyOccupiedException(row, col);
        }

        cell.mark(symbol);
        markedCellsCount++;
    }

    /**
     * Unmarks the cell at (row, col).
     * Used to undo simulated moves without cloning the board.
     */
    public void unmarkCell(int row, int col) {
        validateBounds(row, col);

        Cell cell = grid[row][col];
        if (!cell.isEmpty()) {
            cell.reset();
            markedCellsCount--;
        }
    }

    public Cell getCell(int row, int col) {
        validateBounds(row, col);
        return grid[row][col];
    }

    public boolean isFull() {
        return markedCellsCount == size * size;
    }

    public boolean isEmpty(int row, int col) {
        validateBounds(row, col);
        return grid[row][col].isEmpty();
    }

    /**
     * Returns all empty cells.
     * Used by game strategies to find available moves.
     */
    public List<Cell> getEmptyCells() {
        List<Cell> emptyCells = new ArrayList<>();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (grid[row][col].isEmpty()) {
                    emptyCells.add(grid[row][col]);
                }
            }
        }
        return emptyCells;
    }

    /**
     * Returns a flat string snapshot of the board.
     * Used in GameState for serialization and persistence.
     * Format: "X.O.X.O.X" for a 3x3 board (. = empty)
     */
    public String toSnapshot() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                sb.append(grid[row][col].toString());
            }
        }
        return sb.toString();
    }

    /**
     * Human-readable board for logging and console play.
     * Example output for 3x3:
     *   X . O
     *   . X .
     *   O . X
     */
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                sb.append(grid[row][col].toString());
                if (col < size - 1) sb.append(" ");
            }
            if (row < size - 1) sb.append("\n");
        }
        return sb.toString();
    }

    private void validateBounds(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            throw new GameExceptions.InvalidMoveException(
                    String.format("Position (%d, %d) is out of bounds for board size %d.", row, col, size)
            );
        }
    }

    @Override
    public String toString() {
        return toDisplayString();
    }
}

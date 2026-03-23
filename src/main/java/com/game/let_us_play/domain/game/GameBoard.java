package com.game.let_us_play.domain.game;

import com.game.let_us_play.common.Move;
import com.game.let_us_play.common.Symbol;
import com.game.let_us_play.domain.board.Board;
import lombok.Getter;

/**
 * Game-level wrapper around Board.
 *
 * Design decisions:
 * - Why does GameBoard exist if Board already exists?
 *   Board is a pure data structure — it knows cells, size,
 *   and how to mark them. It has no concept of a game.
 *   GameBoard adds game-level concerns on top:
 *   - tracks total symbols placed (for draw detection shortcut)
 *   - validates moves before delegating to Board
 *   - owns the Board (composition — filled diamond)
 *   - provides a single entry point for all game moves
 *
 * - GameBoard owns Board — Board is created inside GameBoard
 *   via GameBoardFeatures.initBoard() and dies with it.
 *
 * - placedSymbolsCount is redundant with Board.markedCellsCount
 *   intentionally — GameBoard tracks it at its own level for
 *   game-specific logic without reaching into Board internals.
 *
 * - addSymbol() is the single entry point for placing a symbol.
 *   All validation happens here before delegating to Board.
 */
public class GameBoard {
    @Getter
    private final Board board;
    private final GameBoardFeatures gameBoardFeatures;
    @Getter
    private int placedSymbolsCount;

    public GameBoard(int size, GameBoardFeatures gameBoardFeatures) {
        if (gameBoardFeatures == null) {
            throw new IllegalArgumentException("GameBoardFeatures must not be null.");
        }
        this.gameBoardFeatures = gameBoardFeatures;
        this.board = gameBoardFeatures.initBoard(size);
        this.placedSymbolsCount = 0;
    }

    /**
     * Places a symbol on the board at the position specified in the Move.
     *
     * Validates:
     * 1. Move is not null
     * 2. Cell is within bounds (delegated to Board)
     * 3. Cell is not already occupied (delegated to Board)
     *
     * @param move the move to apply
     */
    public void addSymbol(Move move) {
        if (move == null) {
            throw new IllegalArgumentException("Move must not be null.");
        }
        board.markCell(move.getRow(), move.getCol(), move.getSymbol());
        placedSymbolsCount++;
    }

    public boolean isFull() {
        return board.isFull();
    }

    public boolean isCellEmpty(int row, int col) {
        return board.isEmpty(row, col);
    }

    public Symbol getSymbolAt(int row, int col) {
        return board.getCell(row, col).getSymbol();
    }

    public int getBoardSize() {
        return board.getSize();
    }

    /**
     * Returns a snapshot string of the board — used in GameState
     * for serialization and persistence.
     */
    public String toBoardSnapshot() {
        return board.toSnapshot();
    }

    /**
     * Returns a human-readable display string — used in logs
     * and API responses during development.
     */
    public String toDisplayString() {
        return gameBoardFeatures.printBoard(board);
    }

    @Override
    public String toString() {
        return toDisplayString();
    }
}

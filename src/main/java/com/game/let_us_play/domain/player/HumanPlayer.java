package com.game.let_us_play.domain.player;

import com.game.let_us_play.common.Move;
import com.game.let_us_play.common.Symbol;
import com.game.let_us_play.domain.board.Board;
import lombok.Getter;

public class HumanPlayer extends Player {
    @Getter
    private final String userId;    // foreign key to User Management
    private int pendingRow = -1;
    private int pendingCol = -1;

    public HumanPlayer(String name, Symbol symbol, String userId) {
        super(name,  symbol);
        this.userId = userId;
    }

    // Constructor for guest players — no User account linked
    public HumanPlayer(String name, Symbol symbol) {
        super(name, symbol);
        this.userId = null;
    }

    /**
     * Formalizes the pending move into a Move value object.
     * <p>
     * Game sets the pending row/col via setPendingMove() before
     * calling this. If no pending move has been set, throws -
     * that's a programming error in Game, not a user error.
     */
    @Override
    public Move makeMove(Board board) {
        if (pendingRow == -1 || pendingCol == -1) {
            throw new IllegalStateException(
                    "HumanPlayer.makeMove() called without a pending move being set. " +
                            "Call setPendingMove(row, col) first."
            );
        }

        Move move = new Move(getPlayerId(), pendingRow, pendingCol, getSymbol());

        // Reset pending move after use - ready for next turn
        pendingRow = -1;
        pendingCol = -1;

        return move;
    }

    /**
     * Called by Game when it receives a move request from the API.
     * Sets the coordinates the human wants to play.
     */
    public void setPendingMove(int row, int col) {
        this.pendingRow = row;
        this.pendingCol = col;
    }

    public boolean hasPendingMove() {
        return pendingRow != -1 && pendingCol != -1;
    }

    public boolean isGuest()  {
        return userId == null;
    }
}

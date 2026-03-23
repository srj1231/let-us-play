package com.game.let_us_play.domain.player;

import com.game.let_us_play.common.Move;
import com.game.let_us_play.common.Symbol;
import com.game.let_us_play.domain.board.Board;
import lombok.Getter;

import java.util.UUID;

/**
 * Abstract base for all player types.
 * - Abstract class not interface — players share state
 *   (playerId, name, symbol) that would need repeating in every
 *   implementation if this were an interface.
 *   Abstract class lets us define that state once.
 *   <p>
 * - makeMove() is abstract - the one thing that differs between
 *   HumanPlayer and BotPlayer. Everything else is shared.
 *   <p>
 * - playerId is a UUID generated at construction - unique across
 *   all game sessions. Not the userId from User Management.
 *   Player is a session role, not an account.
 * - symbol is final - assigned once at creation, never changes
 *   mid-game.
 */
@Getter
public abstract class Player {
    private final String playerId;
    private final String name;
    private final Symbol symbol;

    protected Player(String name, Symbol symbol) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Player name must not be blank.");
        }
        if (symbol == null) {
            throw new IllegalArgumentException("Player symbol must not be null.");
        }

        this.playerId = UUID.randomUUID().toString();
        this.name = name;
        this.symbol = symbol;
    }

    /**
     * Makes a move on the given board.
     * <p>
     * Game doesn't know or care whether it's talking to a
     * HumanPlayer or BotPlayer.
     * <p>
     * Board is passed as a parameter - NOT stored as a field.
     * Player never owns the board. Game does.
     * Player just reads it momentarily to decide where to play.
     *
     * @param board current board state
     * @return the Move this player wants to make
     */
    public abstract Move makeMove(Board board);

    @Override
    public String toString() {
        return String.format("Player{id=%s, name=%s, symbol=%s, type=%s}",
                playerId, name, symbol, getClass().getSimpleName());
    }
}

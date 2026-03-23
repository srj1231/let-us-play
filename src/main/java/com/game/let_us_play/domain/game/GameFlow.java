package com.game.let_us_play.domain.game;

import com.game.let_us_play.common.GameEvent;
import com.game.let_us_play.domain.player.Player;

/**
 * Contract for game lifecycle management.
 *
 * Design decisions:
 * - Game implements this interface.
 * - A GameRunner or Tournament class holds a GameFlow reference
 *   and can drive any game type — TicTacToe, Chess, Connect4 —
 *   through the same loop without knowing what's inside.
 *
 * - playTurn() returns GameEvent — the result of that turn.
 *   IN_PROGRESS means keep going.
 *   WIN or DRAW means the game is over.
 *
 * - startGame() sets up initial state — resets board,
 *   assigns symbols, sets first player.
 */
public interface GameFlow {

    /**
     * Initialises the game — sets up board, players, first turn.
     * Must be called before any playTurn() calls.
     */
    void startGame();

    /**
     * Executes one full turn for the given player.
     * Asks the player for a move, applies it to the board,
     * checks win/draw, advances the turn.
     *
     * @param player the player whose turn it is
     * @return GameEvent — WIN, DRAW, or IN_PROGRESS
     */
    GameEvent playTurn(Player player);
}

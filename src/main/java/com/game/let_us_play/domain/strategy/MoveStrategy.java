package com.game.let_us_play.domain.strategy;

import com.game.let_us_play.common.Move;
import com.game.let_us_play.common.Symbol;
import com.game.let_us_play.domain.board.Board;

/**
 * Contract for all bot move decision algorithms.
 * - Takes Board + playerId + symbol: everything the strategy
 *   needs to make a decision and construct a valid Move.
 * - Symbol passed in from BotPlayer: strategy is stateless,
 *   same instance can serve multiple bots.
 * Implementations:
 * - RandomStrategy: picks any empty cell randomly
 * - MinimaxStrategy: optimal play via minimax
 * - HeuristicStrategy: rule-based medium difficulty
 */
@FunctionalInterface
public interface MoveStrategy {

    /**
     * Decides the next move for the bot.
     * @param board    current board state: read only
     * @param playerId the bot's player ID: goes into the Move
     * @param symbol   the bot's symbol: for move construction and win simulation
     * @return the chosen Move
     */
    Move decideMove(Board board, String playerId, Symbol symbol);
}

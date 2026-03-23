package com.game.let_us_play.domain.strategy;

import com.game.let_us_play.common.Move;
import com.game.let_us_play.common.Symbol;
import com.game.let_us_play.domain.board.Board;
import com.game.let_us_play.domain.board.Cell;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * Simplest bot strategy — picks a random empty cell.
 * Design decisions:
 * - @Component so Spring can inject it into BotPlayer
 *   without manual instantiation.
 * - Random is instantiated once per strategy instance —
 *   not a new Random() on every call. Avoids overhead
 *   and allows seeding in tests for deterministic behaviour.
 * - Throws if no empty cells exist — this should never happen
 *   in practice because Game checks isFull() before asking
 *   for a move. If it does happen it's a programming error,
 *   not a user error — so IllegalStateException is correct.
 * Stage 2: MinimaxStrategy and HeuristicStrategy will follow
 * the exact same contract — just implement MoveStrategy.
 */
@Component("randomStrategy")
public class RandomStrategy implements MoveStrategy {
    private final Random random;

    public RandomStrategy() {
        this.random = new Random();
    }

    // Constructor for testing - allows seeding Random for determinism
    public RandomStrategy(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public Move decideMove(Board board, String playerId, Symbol symbol) {
        List<Cell> emptyCells = board.getEmptyCells();

        if (emptyCells.isEmpty()) {
            throw new IllegalStateException(
                    "RandomStrategy asked to move but no empty cells exist on the board."
            );
        }

        Cell chosen = emptyCells.get(random.nextInt(emptyCells.size()));

        return new Move(playerId, chosen.getRow(), chosen.getCol(), symbol);
    }
}

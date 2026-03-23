package com.game.let_us_play.domain.player;

import com.game.let_us_play.common.Move;
import com.game.let_us_play.common.Symbol;
import com.game.let_us_play.domain.board.Board;
import com.game.let_us_play.domain.strategy.MoveStrategy;
import lombok.Getter;

/**
 * A player controlled by a bot strategy.
 * <p>
 * Design decisions:
 * - BotPlayer delegates ALL move logic to MoveStrategy.
 *   BotPlayer itself has zero intelligence — it just calls
 *   moveStrategy.decideMove(). This is the Strategy pattern.
 * <p>
 * - Swapping difficulty = swapping the injected MoveStrategy.
 *   No if/else, no switch on difficulty level, no changes
 *   to BotPlayer ever needed for new difficulty levels.
 * <p>
 * - difficulty is metadata only — used for display and
 *   matchmaking purposes. The actual behaviour comes from
 *   which MoveStrategy is injected, not from this int.
 * <p>
 * - MoveStrategy is injected via constructor — BotPlayer
 *   doesn't create its own strategy (no new RandomStrategy()
 *   inside). This keeps it testable and follows DIP.
 * <p>
 * Relationship: Aggregation (open diamond) — MoveStrategy
 * is injected from outside and can be shared across bots.
 */
@Getter
public class BotPlayer extends Player {

    private final MoveStrategy moveStrategy;
    private final int difficulty;   // 1 = easy, 2 = medium, 3 = hard

    public BotPlayer(String name, Symbol symbol, MoveStrategy moveStrategy, int difficulty) {
        super(name, symbol);
        if (moveStrategy == null) {
            throw new IllegalArgumentException("MoveStrategy must not be null.");
        }
        if (difficulty < 1 || difficulty > 3) {
            throw new IllegalArgumentException("Difficulty must be 1 (easy), 2 (medium), or 3 (hard).");
        }
        this.moveStrategy = moveStrategy;
        this.difficulty   = difficulty;
    }

    // Convenience constructor — defaults to difficulty 1
    public BotPlayer(String name, Symbol symbol, MoveStrategy moveStrategy) {
        this(name, symbol, moveStrategy, 1);
    }

    /**
     * Delegates entirely to the injected MoveStrategy.
     * BotPlayer has no move logic of its own — that's intentional.
     * <p>
     * Game calls this the same way it calls HumanPlayer.makeMove()
     * polymorphically, without knowing it's a bot.
     */
    @Override
    public Move makeMove(Board board) {
        return moveStrategy.decideMove(board, getPlayerId(), getSymbol());
    }

    public String getDifficultyLabel() {
        return switch (difficulty) {
            case 1 -> "Easy";
            case 2 -> "Medium";
            case 3 -> "Hard";
            default -> "Unknown";
        };
    }
}

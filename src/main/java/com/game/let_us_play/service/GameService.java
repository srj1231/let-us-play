package com.game.let_us_play.service;

import com.game.let_us_play.api.response.GameResponse;
import com.game.let_us_play.common.GameEvent;
import com.game.let_us_play.common.GameExceptions;
import com.game.let_us_play.common.Move;
import com.game.let_us_play.common.Symbol;
import com.game.let_us_play.domain.game.BoardFeatures;
import com.game.let_us_play.domain.game.Game;
import com.game.let_us_play.domain.game.GameBoardFeatures;
import com.game.let_us_play.domain.player.BotPlayer;
import com.game.let_us_play.domain.player.HumanPlayer;
import com.game.let_us_play.domain.player.Player;
import com.game.let_us_play.domain.strategy.MoveStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages game lifecycle.
 *
 * Stage 1: ConcurrentHashMap in-memory store.
 * Stage 2: GameRepository + PostgreSQL replaces the map.
 *
 * makeMove() now returns GameResponse directly — it's the
 * only place that knows about lastMoves for this turn.
 * Controller stays thin — just calls service and returns.
 */
@Service
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final BoardFeatures boardFeatures;
    private final GameBoardFeatures gameBoardFeatures;
    private final MoveStrategy randomStrategy;

    private final Map<String, Game> activeGames = new ConcurrentHashMap<>();

    public GameService(
            @Qualifier("rowColDiagStrategy") BoardFeatures boardFeatures,
            @Qualifier("standardBoardSetup") GameBoardFeatures gameBoardFeatures,
            @Qualifier("randomStrategy")     MoveStrategy randomStrategy
    ) {
        this.boardFeatures     = boardFeatures;
        this.gameBoardFeatures = gameBoardFeatures;
        this.randomStrategy    = randomStrategy;
    }

    // ── Game creation ─────────────────────────────────────────────────────

    public Game createHumanVsHumanGame(
            String playerOneName,
            String playerOneUserId,
            String playerTwoName,
            String playerTwoUserId,
            int boardSize
    ) {
        List<Player> players = new ArrayList<>();
        players.add(new HumanPlayer(playerOneName, Symbol.CROSS, playerOneUserId));
        players.add(new HumanPlayer(playerTwoName, Symbol.ZERO, playerTwoUserId));
        return createAndStartGame(players, boardSize);
    }

    public Game createHumanVsBotGame(
            String humanName,
            String humanUserId,
            int boardSize,
            int botDifficulty
    ) {
        List<Player> players = new ArrayList<>();
        players.add(new HumanPlayer(humanName, Symbol.CROSS, humanUserId));
        players.add(new BotPlayer("Bot", Symbol.ZERO, randomStrategy, botDifficulty));
        return createAndStartGame(players, boardSize);
    }

    // ── Move handling ─────────────────────────────────────────────────────

    /**
     * Applies human move, auto-triggers bot if next.
     * Collects all moves made this turn into lastMoves.
     * Returns GameResponse with full structured data.
     */
    public GameResponse makeMove(String gameId, int row, int col) {
        Game game = findGame(gameId);
        List<Move> lastMoves = new ArrayList<>();

        GameEvent result = game.makeMove(row, col);
        lastMoves.add(getLastMove(game));

        if (result != GameEvent.IN_PROGRESS) {
            cleanupIfOver(game);
            return GameResponse.from(game, lastMoves);
        }

        if (isCurrentPlayerBot(game)) {
            game.makeBotMove();
            lastMoves.add(getLastMove(game));
            cleanupIfOver(game);
        }

        return GameResponse.from(game, lastMoves);
    }

    // ── Queries ───────────────────────────────────────────────────────────

    public Game getGame(String gameId) {
        return findGame(gameId);
    }

    public void abandonGame(String gameId) {
        findGame(gameId);
        activeGames.remove(gameId);
        log.info("Game [{}] abandoned.", gameId);
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private Game createAndStartGame(List<Player> players, int boardSize) {
        Game game = new Game(players, boardSize, boardFeatures, gameBoardFeatures);
        game.startGame();
        activeGames.put(game.getGameId(), game);
        log.info("Game [{}] created. Active games: {}", game.getGameId(), activeGames.size());
        return game;
    }

    private Game findGame(String gameId) {
        Game game = activeGames.get(gameId);
        if (game == null) {
            throw new GameExceptions.GameNotFoundException(gameId);
        }
        return game;
    }

    private Move getLastMove(Game game) {
        List<Move> history = game.getMoveHistory();
        return history.get(history.size() - 1);
    }

    private boolean isCurrentPlayerBot(Game game) {
        return game.getCurrentPlayer() instanceof BotPlayer;
    }

    private void cleanupIfOver(Game game) {
        if (game.isOver()) {
            log.info("Game [{}] over — status: [{}].", game.getGameId(), game.getStatus());
            activeGames.remove(game.getGameId());
        }
    }
}

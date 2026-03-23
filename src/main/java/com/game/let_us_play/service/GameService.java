package com.game.let_us_play.service;

import com.game.let_us_play.api.response.GameResponse;
import com.game.let_us_play.common.GameEvent;
import com.game.let_us_play.common.GameExceptions;
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
 * Spring service — manages game lifecycle.
 *
 * Design decisions:
 * - GameService is the only place where Game instances are
 *   created. Nothing else calls new Game() directly.
 *   Single place to control game creation logic.
 *
 * - In-memory store (ConcurrentHashMap) for Stage 1.
 *   Stage 2 replaces this with GameRepository + DB.
 *   The rest of the code doesn't change — only this class.
 *
 * - ConcurrentHashMap not HashMap — multiple HTTP requests
 *   can arrive simultaneously (two players in different games).
 *   ConcurrentHashMap is thread-safe for concurrent reads/writes.
 *
 * - BoardFeatures and GameBoardFeatures are injected by Spring
 *   via @Qualifier — GameService doesn't know which concrete
 *   implementation it's using. Fully decoupled.
 *
 * - After every human move, GameService checks if the next
 *   player is a bot and auto-triggers the bot move.
 *   This means the API caller gets both results in one response.
 *
 * - makeMove() returns MoveResult — a clean response object
 *   that carries everything the controller needs without
 *   exposing the full Game object to the API layer.
 */
@Service
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final BoardFeatures boardFeatures;
    private final GameBoardFeatures gameBoardFeatures;
    private final MoveStrategy randomStrategy;

    // Stage 1: in-memory store. Stage 2: replaced by GameRepository.
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

    /**
     * Creates a Human vs Human game.
     *
     * @param playerOneName name of player one
     * @param playerOneUserId userId of player one (nullable for guests)
     * @param playerTwoName name of player two
     * @param playerTwoUserId userId of player two (nullable for guests)
     * @param boardSize size of the board (3 for standard TicTacToe)
     * @return the created Game
     */
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

    /**
     * Creates a Human vs Bot game.
     *
     * @param humanName name of the human player
     * @param humanUserId userId of the human (nullable for guests)
     * @param boardSize size of the board
     * @param botDifficulty 1 = easy (random), more levels in Stage 2
     * @return the created Game
     */
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

    /**
     * Processes a human player's move.
     *
     * Flow:
     * 1. Find the game
     * 2. Apply the human's move
     * 3. If game not over and next player is a bot — auto-trigger bot move
     * 4. Return the result
     *
     * @param gameId the game to make a move in
     * @param row    row of the move
     * @param col    col of the move
     * @return MoveResult containing the outcome and updated board state
     */
    public MoveResult makeMove(String gameId, int row, int col) {
        Game game = findGame(gameId);

        // Apply human move
        GameEvent result = game.makeMove(row, col);

        // If game is over after human move — return immediately
        if (result != GameEvent.IN_PROGRESS) {
            cleanupIfOver(game);
            return MoveResult.of(game, result);
        }

        // If next player is a bot — auto-trigger bot move
        if (isCurrentPlayerBot(game)) {
            GameEvent botResult = game.makeBotMove();
            cleanupIfOver(game);
            return MoveResult.of(game, botResult);
        }

        return MoveResult.of(game, result);
    }

    /**
     * Returns the current state of a game.
     * Used by GET /game/{id} to poll game state.
     */
    public Game getGame(String gameId) {
        return findGame(gameId);
    }

    public Map<String, Game> getAllActiveGames() {
        return activeGames;
    }

    private Game createAndStartGame(List<Player> players, int boardSize) {
        Game game = new Game(players, boardSize, boardFeatures, gameBoardFeatures);
        game.startGame();
        activeGames.put(game.getGameId(), game);
        log.info("Game [{}] created and stored. Active games: {}",
                game.getGameId(), activeGames.size());
        return game;
    }

    private Game findGame(String gameId) {
        Game game = activeGames.get(gameId);
        if (game == null) {
            throw new GameExceptions.GameNotFoundException(gameId);
        }
        return game;
    }

    private boolean isCurrentPlayerBot(Game game) {
        return game.getCurrentPlayer() instanceof BotPlayer;
    }

    private void cleanupIfOver(Game game) {
        if (game.isOver()) {
            log.info("Game [{}] is over with status [{}]. Removing from active games.",
                    game.getGameId(), game.getStatus());
            // Stage 2: persist to DB before removing
            // gameRepository.save(GameState.from(game));
            activeGames.remove(game.getGameId());
        }
    }

    /**
     * Response object for a move — carries everything the
     * controller needs without exposing the full Game object.
     *
     * Inner class because it only exists to serve GameService
     * responses. If it grows complex, extract to its own file.
     */
    public record MoveResult(
            String gameId,
            GameEvent status,
            String winnerId,
            String currentPlayerId,
            String currentPlayerName,
            String boardDisplay,
            String boardSnapshot,
            int totalMoves,
            boolean isOver
    ) {
        public static MoveResult of(Game game, GameEvent result) {
            Player currentPlayer = game.getCurrentPlayer();
            return new MoveResult(
                    game.getGameId(),
                    result,
                    game.getWinnerId(),
                    currentPlayer.getPlayerId(),
                    currentPlayer.getName(),
                    game.getGameBoard().toDisplayString(),
                    game.getGameBoard().toBoardSnapshot(),
                    game.getTotalMoves(),
                    game.isOver()
            );
        }
    }

    public void abandonGame(String gameId) {
        findGame(gameId);
        activeGames.remove(gameId);
        log.info("Game [{}] abandoned.", gameId);
    }

    public GameResponse toGameResponse(MoveResult result) {
        Game game = activeGames.get(result.gameId());
        if (game != null) {
            return GameResponse.from(result, game);
        }
        return new GameResponse(
                result.gameId(),
                result.status(),
                result.winnerId(),
                result.currentPlayerId(),
                result.currentPlayerName(),
                result.boardDisplay(),
                result.boardSnapshot(),
                0,
                result.totalMoves(),
                true,
                java.util.List.of()
        );
    }

}
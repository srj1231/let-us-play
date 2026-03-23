package com.game.let_us_play.domain.game;

import com.game.let_us_play.common.GameEvent;
import com.game.let_us_play.common.GameExceptions;
import com.game.let_us_play.common.Move;
import com.game.let_us_play.domain.player.Player;
import com.game.let_us_play.domain.player.HumanPlayer;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * The game orchestrator — implements GameFlow.
 *
 * Design decisions:
 * - Game coordinates but owns no logic itself.
 *   Win detection → BoardFeatures.
 *   Board setup    → GameBoardFeatures (via GameBoard).
 *   Move decision  → Player (HumanPlayer or BotPlayer).
 *   Game is purely a coordinator — the conductor analogy.
 *
 * - Game is NOT a Spring @Component — it is a domain object,
 *   not a service. A new Game instance is created per game session
 *   by GameService. Spring manages GameService, not Game.
 *
 * - players is a List<Player> not just two Player fields —
 *   supports N players from day one. TicTacToe uses 2,
 *   but the structure doesn't limit future variants.
 *
 * - currentPlayerIndex cycles via modulo — clean N-player
 *   turn rotation without any special casing.
 *
 * - gameId is a UUID — unique across all game sessions,
 *   safe to use as a key in storage and API URLs.
 *
 * - status starts as IN_PROGRESS — never null.
 *   Terminal states are WIN and DRAW only.
 *
 * - moveHistory is a List<Move> — full audit log of every
 *   move made. Used for GameState serialisation (Stage 2+).
 */
@Getter
public class Game implements GameFlow {

    private static final Logger log =
            LoggerFactory.getLogger(Game.class);

    private final String gameId;
    private final List<Player> players;
    private final GameBoard gameBoard;
    private final BoardFeatures boardFeatures;
    private final int boardSize;

    private int currentPlayerIndex;
    private GameEvent status;
    private String winnerId;
    private final List<Move> moveHistory;
    private boolean started;

    public Game(
            List<Player> players,
            int boardSize,
            BoardFeatures boardFeatures,
            GameBoardFeatures gameBoardFeatures
    ) {
        validatePlayers(players);

        this.gameId             = UUID.randomUUID().toString();
        this.players            = Collections.unmodifiableList(new ArrayList<>(players));
        this.boardSize          = boardSize;
        this.boardFeatures      = boardFeatures;
        this.gameBoard          = new GameBoard(boardSize, gameBoardFeatures);
        this.currentPlayerIndex = 0;
        this.status             = GameEvent.IN_PROGRESS;
        this.winnerId           = null;
        this.moveHistory        = new ArrayList<>();
        this.started            = false;
    }

    /**
     * Initialises the game.
     * Logs the starting state and marks the game as started.
     * Must be called before makeMove().
     */
    @Override
    public void startGame() {
        started = true;
        log.info("Game [{}] started. Players: {}. Board size: {}x{}",
                gameId, players.stream().map(Player::getName).toList(),
                boardSize, boardSize);
        log.info("Initial board:\n{}", gameBoard.toDisplayString());
    }

    /**
     * Executes one full turn for the given player.
     *
     * Flow:
     * 1. Validate game is in progress
     * 2. Ask the player for their move
     * 3. Apply the move to the board
     * 4. Check for win
     * 5. Check for draw
     * 6. Advance to next player
     * 7. Return the result
     */
    @Override
    public GameEvent playTurn(Player player) {
        validateGameInProgress();

        Move move = player.makeMove(gameBoard.getBoard());
        applyMove(move);

        if (boardFeatures.checkWin(gameBoard.getBoard(), move)) {
            status   = GameEvent.WIN;
            winnerId = player.getPlayerId();
            log.info("Game [{}] — {} ({}) wins! Winning move: ({}, {})",
                    gameId, player.getName(), player.getSymbol(),
                    move.getRow(), move.getCol());
            return GameEvent.WIN;
        }

        if (boardFeatures.isDraw(gameBoard.getBoard())) {
            status = GameEvent.DRAW;
            log.info("Game [{}] — Draw! Board is full.", gameId);
            return GameEvent.DRAW;
        }

        advanceTurn();
        log.info("Game [{}] — Move ({},{}) by {}. Board:\n{}",
                gameId, move.getRow(), move.getCol(),
                player.getName(), gameBoard.toDisplayString());

        return GameEvent.IN_PROGRESS;
    }

    /**
     * Entry point for human player moves coming from the REST API.
     *
     * GameController calls this with the row/col from the HTTP request.
     * This method:
     * 1. Sets the pending move on the current HumanPlayer
     * 2. Calls playTurn() with the current player
     * 3. Returns the result
     *
     * Throws if the current player is not a HumanPlayer —
     * the API should not be called on a bot's turn.
     */
    public GameEvent makeMove(int row, int col) {
        validateGameInProgress();

        Player currentPlayer = getCurrentPlayer();

        if (!(currentPlayer instanceof HumanPlayer humanPlayer)) {
            throw new IllegalStateException(
                    "Current player is a bot. Bot moves are handled automatically."
            );
        }

        humanPlayer.setPendingMove(row, col);
        return playTurn(currentPlayer);
    }

    /**
     * Triggers the bot's turn automatically.
     * Called by GameService after a human move when the next
     * player is a bot. Returns the result of the bot's turn.
     */
    public GameEvent makeBotMove() {
        validateGameInProgress();
        Player currentPlayer = getCurrentPlayer();
        return playTurn(currentPlayer);
    }

    private void applyMove(Move move) {
        gameBoard.addSymbol(move);
        moveHistory.add(move);
    }

    private void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    private void validateGameInProgress() {
        if (!started) {
            throw new IllegalStateException(
                    "Game [" + gameId + "] has not been started. Call startGame() first."
            );
        }
        if (status != GameEvent.IN_PROGRESS) {
            throw new GameExceptions.GameAlreadyOverException(gameId);
        }
    }

    private void validatePlayers(List<Player> players) {
        if (players == null || players.size() < 2) {
            throw new IllegalArgumentException(
                    "A game requires at least 2 players. Got: " +
                            (players == null ? 0 : players.size())
            );
        }
    }

    public List<Move> getMoveHistory() {
        return Collections.unmodifiableList(moveHistory);
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public boolean isOver() {
        return status != GameEvent.IN_PROGRESS;
    }

    public int getTotalMoves() {
        return moveHistory.size();
    }
}

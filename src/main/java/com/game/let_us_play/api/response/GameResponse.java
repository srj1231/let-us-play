package com.game.let_us_play.api.response;

import com.game.let_us_play.common.GameEvent;
import com.game.let_us_play.domain.game.Game;
import com.game.let_us_play.domain.player.Player;
import com.game.let_us_play.service.GameService;

import java.util.List;

/**
 * API response representing the current state of a game.
 *
 * Design decisions:
 * - Record — immutable, no boilerplate.
 * - Never exposes domain objects (Game, Player, Board) directly.
 *   The API contract is independent of the domain model.
 *   If Game internals change, this response stays stable.
 * - Two static factory methods — one from Game (for GET),
 *   one from MoveResult (for POST move response).
 */
public record GameResponse(
        String gameId,
        GameEvent status,
        String winnerId,
        String currentPlayerId,
        String currentPlayerName,
        String boardDisplay,
        String boardSnapshot,
        int boardSize,
        int totalMoves,
        boolean isOver,
        List<PlayerInfo> players
) {

    /**
     * Builds a GameResponse from a full Game object.
     * Used by GET /game/{id}.
     */
    public static GameResponse from(Game game) {
        Player current = game.getCurrentPlayer();
        List<PlayerInfo> playerInfos = game.getPlayers()
                .stream()
                .map(PlayerInfo::from)
                .toList();

        return new GameResponse(
                game.getGameId(),
                game.getStatus(),
                game.getWinnerId(),
                current.getPlayerId(),
                current.getName(),
                game.getGameBoard().toDisplayString(),
                game.getGameBoard().toBoardSnapshot(),
                game.getGameBoard().getBoardSize(),
                game.getTotalMoves(),
                game.isOver(),
                playerInfos
        );
    }

    /**
     * Builds a GameResponse from a MoveResult.
     * Used by POST /game/{id}/move.
     */
    public static GameResponse from(GameService.MoveResult result, Game game) {
        List<PlayerInfo> playerInfos = game.getPlayers()
                .stream()
                .map(PlayerInfo::from)
                .toList();

        return new GameResponse(
                result.gameId(),
                result.status(),
                result.winnerId(),
                result.currentPlayerId(),
                result.currentPlayerName(),
                result.boardDisplay(),
                result.boardSnapshot(),
                game.getGameBoard().getBoardSize(),
                result.totalMoves(),
                result.isOver(),
                playerInfos
        );
    }

    public record PlayerInfo(
            String playerId,
            String name,
            String symbol,
            String type
    ) {
        public static PlayerInfo from(Player player) {
            return new PlayerInfo(
                    player.getPlayerId(),
                    player.getName(),
                    player.getSymbol().getDisplay(),
                    player.getClass().getSimpleName()
            );
        }
    }
}

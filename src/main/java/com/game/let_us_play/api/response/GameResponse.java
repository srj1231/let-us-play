package com.game.let_us_play.api.response;

import com.game.let_us_play.common.GameEvent;
import com.game.let_us_play.common.Move;
import com.game.let_us_play.domain.board.Board;
import com.game.let_us_play.domain.game.Game;
import com.game.let_us_play.domain.player.Player;
import com.game.let_us_play.domain.player.BotPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * API response wrapper.
 *
 * All success responses:
 * {
 *   "data": { ... },
 *   "message": "..."
 * }
 *
 * Errors handled by GlobalExceptionHandler — different shape.
 * Consistent data wrapper means Angular FE always reads response.data.
 */
public record GameResponse(
        GameData data,
        String message
) {

    public static GameResponse created(Game game) {
        return new GameResponse(
                GameData.from(game, List.of()),
                "Game created successfully."
        );
    }

    public static GameResponse from(Game game) {
        return new GameResponse(
                GameData.from(game, List.of()),
                "Game retrieved successfully."
        );
    }

    public static GameResponse from(Game game, List<Move> lastMoves) {
        String message = switch (game.getStatus()) {
            case WIN  -> game.getPlayers().stream()
                    .filter(p -> p.getPlayerId().equals(game.getWinnerId()))
                    .findFirst()
                    .map(p -> p.getName() + " wins!")
                    .orElse("Game over!");
            case DRAW -> "It's a draw!";
            default   -> "Move applied successfully.";
        };
        return new GameResponse(GameData.from(game, lastMoves), message);
    }

    public record GameData(
            String gameId,
            GameEvent status,
            BoardData board,
            List<PlayerInfo> players,
            CurrentTurn currentTurn,
            List<LastMove> lastMoves,
            int totalMoves,
            String winnerId,
            String winnerName,
            boolean isOver
    ) {
        public static GameData from(Game game, List<Move> lastMoves) {

            Player winner = game.getWinnerId() == null ? null :
                    game.getPlayers().stream()
                            .filter(p -> p.getPlayerId().equals(game.getWinnerId()))
                            .findFirst().orElse(null);

            CurrentTurn currentTurn = game.isOver()
                    ? null
                    : CurrentTurn.from(game.getCurrentPlayer());

            List<LastMove> lastMoveList = lastMoves.stream()
                    .map(m -> {
                        String name = game.getPlayers().stream()
                                .filter(p -> p.getPlayerId().equals(m.getPlayerId()))
                                .findFirst()
                                .map(Player::getName)
                                .orElse("Unknown");
                        return new LastMove(name, m.getSymbol().getDisplay(), m.getRow(), m.getCol());
                    })
                    .toList();

            return new GameData(
                    game.getGameId(),
                    game.getStatus(),
                    BoardData.from(game.getGameBoard().getBoard()),
                    game.getPlayers().stream().map(PlayerInfo::from).toList(),
                    currentTurn,
                    lastMoveList,
                    game.getTotalMoves(),
                    game.getWinnerId(),
                    winner != null ? winner.getName() : null,
                    game.isOver()
            );
        }
    }

    public record BoardData(
            int size,
            List<List<String>> grid
    ) {
        public static BoardData from(Board board) {
            int size = board.getSize();
            List<List<String>> grid = new ArrayList<>();
            for (int row = 0; row < size; row++) {
                List<String> rowList = new ArrayList<>();
                for (int col = 0; col < size; col++) {
                    rowList.add(board.getCell(row, col).toString());
                }
                grid.add(rowList);
            }
            return new BoardData(size, grid);
        }
    }

    public record PlayerInfo(
            String playerId,
            String name,
            String symbol,
            String type,
            Integer difficulty
    ) {
        public static PlayerInfo from(Player player) {
            return new PlayerInfo(
                    player.getPlayerId(),
                    player.getName(),
                    player.getSymbol().getDisplay(),
                    player instanceof BotPlayer ? "BOT" : "HUMAN",
                    player instanceof BotPlayer bot ? bot.getDifficulty() : null
            );
        }
    }

    public record CurrentTurn(
            String playerId,
            String name,
            String symbol
    ) {
        public static CurrentTurn from(Player player) {
            if (player == null) {
                return null;
            }

            return new CurrentTurn(
                    player.getPlayerId(),
                    player.getName(),
                    player.getSymbol().getDisplay()
            );
        }
    }

    public record LastMove(
            String player,
            String symbol,
            int row,
            int col
    ) {}
}

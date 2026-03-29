package com.game.let_us_play.api.response;

import com.game.let_us_play.common.GameEvent;
import com.game.let_us_play.common.Move;
import com.game.let_us_play.common.Symbol;
import com.game.let_us_play.domain.board.Board;
import com.game.let_us_play.domain.board.Cell;
import com.game.let_us_play.domain.game.Game;
import com.game.let_us_play.domain.game.GameBoard;
import com.game.let_us_play.domain.player.BotPlayer;
import com.game.let_us_play.domain.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("GameResponse")
class GameResponseTest {

    private static final String GAME_ID = "game-123";
    private static final String PLAYER_ONE = "Alice";
    private static final String USER_ID_ONE = "uid-1";

    // ─────────────────────────────────────────────────────────
    // created()
    // ─────────────────────────────────────────────────────────

    @Nested
    class CreatedTests {

        @Test
        void returnsCorrectMessage() {
            GameResponse response = GameResponse.created(buildMockGame());

            assertThat(response.message()).isEqualTo("Game created successfully.");
        }

        @Test
        void dataIsCorrect() {
            GameResponse response = GameResponse.created(buildMockGame());

            assertThat(response.data()).isNotNull();
            assertThat(response.data().gameId()).isEqualTo(GAME_ID);
            assertThat(response.data().lastMoves()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────
    // from(Game)
    // ─────────────────────────────────────────────────────────

    @Nested
    class FromGameTests {

        @Test
        void returnsCorrectMessage() {
            GameResponse response = GameResponse.from(buildMockGame());

            assertThat(response.message()).isEqualTo("Game retrieved successfully.");
        }

        @Test
        void dataIsCorrect() {
            GameResponse response = GameResponse.from(buildMockGame());

            assertThat(response.data().gameId()).isEqualTo(GAME_ID);
            assertThat(response.data().lastMoves()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────
    // from(Game, moves)
    // ─────────────────────────────────────────────────────────

    @Nested
    class FromGameWithMovesTests {

        @Test
        void message_whenInProgress() {
            Game game = buildMockGame();
            when(game.getStatus()).thenReturn(GameEvent.IN_PROGRESS);

            GameResponse response = GameResponse.from(game, List.of());

            assertThat(response.message()).isEqualTo("Move applied successfully.");
        }

        @Test
        void message_whenDraw() {
            Game game = buildMockGame();
            when(game.getStatus()).thenReturn(GameEvent.DRAW);

            GameResponse response = GameResponse.from(game, List.of());

            assertThat(response.message()).isEqualTo("It's a draw!");
        }

        @Test
        void message_whenWin() {
            Game game = buildMockGame();
            when(game.getStatus()).thenReturn(GameEvent.WIN);
            when(game.getWinnerId()).thenReturn(USER_ID_ONE);

            GameResponse response = GameResponse.from(game, List.of());

            assertThat(response.message()).isEqualTo(PLAYER_ONE + " wins!");
        }

        @Test
        void lastMovesMappedCorrectly() {
            Game game = buildMockGame();

            Move move = mock(Move.class);
            when(move.getPlayerId()).thenReturn(USER_ID_ONE);
            when(move.getSymbol()).thenReturn(Symbol.CROSS);
            when(move.getRow()).thenReturn(1);
            when(move.getCol()).thenReturn(2);

            GameResponse response = GameResponse.from(game, List.of(move));

            var lastMove = response.data().lastMoves().get(0);

            assertThat(lastMove.player()).isEqualTo(PLAYER_ONE);
            assertThat(lastMove.row()).isEqualTo(1);
            assertThat(lastMove.col()).isEqualTo(2);
        }
    }

    // ─────────────────────────────────────────────────────────
    // GameData
    // ─────────────────────────────────────────────────────────

    @Nested
    class GameDataTests {

        @Test
        void currentTurn_null_whenGameOver() {
            Game game = buildMockGame();
            when(game.isOver()).thenReturn(true);

            GameResponse response = GameResponse.from(game);

            assertThat(response.data().currentTurn()).isNull();
        }

        @Test
        void currentTurn_present_whenInProgress() {
            GameResponse response = GameResponse.from(buildMockGame());

            assertThat(response.data().currentTurn()).isNotNull();
            assertThat(response.data().currentTurn().playerId()).isEqualTo(USER_ID_ONE);
        }

        @Test
        void winner_whenGameWon() {
            Game game = buildMockGame();
            when(game.getStatus()).thenReturn(GameEvent.WIN);
            when(game.getWinnerId()).thenReturn(USER_ID_ONE);
            when(game.isOver()).thenReturn(true);

            GameResponse response = GameResponse.from(game);

            assertThat(response.data().winnerId()).isEqualTo(USER_ID_ONE);
            assertThat(response.data().winnerName()).isEqualTo(PLAYER_ONE);
        }

        @Test
        void boardDimensionsCorrect() {
            GameResponse response = GameResponse.from(buildMockGame());

            assertThat(response.data().board().size()).isEqualTo(3);
            assertThat(response.data().board().grid()).hasSize(3);
            assertThat(response.data().board().grid().get(0)).hasSize(3);
        }

        @Test
        void playersMappedCorrectly() {
            GameResponse response = GameResponse.from(buildMockGame());

            var player = response.data().players().get(0);

            assertThat(player.playerId()).isEqualTo(USER_ID_ONE);
            assertThat(player.name()).isEqualTo(PLAYER_ONE);
            assertThat(player.type()).isEqualTo("HUMAN");
        }
    }

    // ─────────────────────────────────────────────────────────
    // PlayerInfo
    // ─────────────────────────────────────────────────────────

    @Test
    void botPlayerMapping() {
        BotPlayer bot = mock(BotPlayer.class);
        when(bot.getPlayerId()).thenReturn("bot-1");
        when(bot.getName()).thenReturn("Bot");
        when(bot.getSymbol()).thenReturn(Symbol.ZERO);
        when(bot.getDifficulty()).thenReturn(2);

        var info = GameResponse.PlayerInfo.from(bot);

        assertThat(info.type()).isEqualTo("BOT");
        assertThat(info.difficulty()).isEqualTo(2);
    }

    // ─────────────────────────────────────────────────────────
    // helper
    // ─────────────────────────────────────────────────────────

    private Game buildMockGame() {
        Cell cell = mock(Cell.class);
        when(cell.toString()).thenReturn(" ");

        Board board = mock(Board.class);
        when(board.getSize()).thenReturn(3);
        when(board.getCell(anyInt(), anyInt())).thenReturn(cell);

        GameBoard gameBoard = mock(GameBoard.class);
        when(gameBoard.getBoard()).thenReturn(board);

        Player player = mock(Player.class);
        when(player.getPlayerId()).thenReturn(USER_ID_ONE);
        when(player.getName()).thenReturn(PLAYER_ONE);
        when(player.getSymbol()).thenReturn(Symbol.CROSS);

        Game game = mock(Game.class);
        when(game.getGameId()).thenReturn(GAME_ID);
        when(game.getGameBoard()).thenReturn(gameBoard);
        when(game.getCurrentPlayer()).thenReturn(player);
        when(game.getPlayers()).thenReturn(List.of(player));
        when(game.getStatus()).thenReturn(GameEvent.IN_PROGRESS);
        when(game.isOver()).thenReturn(false);
        when(game.getTotalMoves()).thenReturn(0);

        return game;
    }
}

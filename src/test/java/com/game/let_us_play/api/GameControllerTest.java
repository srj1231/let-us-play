package com.game.let_us_play.api;

import com.game.let_us_play.api.response.GameResponse;
import com.game.let_us_play.common.GameEvent;
import com.game.let_us_play.common.GameExceptions;
import com.game.let_us_play.common.Symbol;
import com.game.let_us_play.domain.board.Board;
import com.game.let_us_play.domain.board.Cell;
import com.game.let_us_play.domain.game.Game;
import com.game.let_us_play.domain.game.GameBoard;
import com.game.let_us_play.domain.player.Player;
import com.game.let_us_play.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
@DisplayName("GameController")
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    // ── shared fixtures ───────────────────────────────────────────────────────

    private static final String BASE_URL = "/api/v1/games";
    private static final String GAME_ID = "game-123";
    private static final String PLAYER_ONE = "Alice";
    private static final String PLAYER_TWO = "Bob";
    private static final String USER_ID_ONE = "uid-1";
    private static final String USER_ID_TWO = "uid-2";
    private static final int BOARD_SIZE = 3;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /human-vs-human
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /human-vs-human")
    class CreateHumanVsHumanGameTests {
        @Test
        @DisplayName("returns 201 CREATED when request is valid")
        void returns201_whenRequestIsValid() throws Exception {
            Game mockGame = buildMockGame();
            when(gameService.createHumanVsHumanGame(
                    PLAYER_ONE, USER_ID_ONE, PLAYER_TWO, USER_ID_TWO, BOARD_SIZE)
            ).thenReturn(mockGame);

            String body = """
                    {
                      "playerOneName": "Alice",
                      "playerOneUserId": "uid-1",
                      "playerTwoName": "Bob",
                      "playerTwoUserId": "uid-2",
                      "boardSize": 3
                    }
                    """;

            mockMvc.perform(post(BASE_URL + "/human-vs-human")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("returns 400 BAD REQUEST when playerOneName is missing")
        void returns400_whenPlayerOneNameMissing() throws Exception {
            String body = """
                    {
                      "playerOneUserId": "uid-1",
                      "playerTwoName": "Bob",
                      "playerTwoUserId": "uid-2",
                      "boardSize": 3
                    }
                    """;

            mockMvc.perform(post(BASE_URL + "/human-vs-human")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 BAD REQUEST when playerTwoName is missing")
        void returns400_whenPlayerTwoNameMissing() throws Exception {
            String body = """
                    {
                      "playerOneName": "Alice",
                      "playerOneUserId": "uid-1",
                      "playerTwoUserId": "uid-2",
                      "boardSize": 3
                    }
                    """;

            mockMvc.perform(post(BASE_URL + "/human-vs-human")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 BAD REQUEST when body is empty")
        void returns400_whenBodyIsEmpty() throws Exception {
            mockMvc.perform(post(BASE_URL + "/human-vs-human")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 415 UNSUPPORTED MEDIA TYPE when Content-Type is not JSON")
        void returns415_whenContentTypeIsNotJson() throws Exception {
            mockMvc.perform(post(BASE_URL + "/human-vs-human")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("some text"))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("delegates correct fields to GameService")
        void delegatesCorrectFieldsToService() throws Exception {
            Game mockGame = buildMockGame();
            when(gameService.createHumanVsHumanGame(any(), any(), any(), any(), anyInt()))
                    .thenReturn(mockGame);

            String body = """
                    {
                      "playerOneName": "Alice",
                      "playerOneUserId": "uid-1",
                      "playerTwoName": "Bob",
                      "playerTwoUserId": "uid-2",
                      "boardSize": 3
                    }
                    """;

            mockMvc.perform(post(BASE_URL + "/human-vs-human")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());

            verify(gameService).createHumanVsHumanGame(
                    PLAYER_ONE, USER_ID_ONE, PLAYER_TWO, USER_ID_TWO, BOARD_SIZE
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /human-vs-bot
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /human-vs-bot")
    class CreateHumanVsBotGameTests {

        @BeforeEach
        void setUp() {
            Player mockPlayer = mock(Player.class);
            when(mockPlayer.getPlayerId()).thenReturn(USER_ID_ONE);
            when(mockPlayer.getName()).thenReturn(PLAYER_ONE);
        }

        @Test
        @DisplayName("returns 201 CREATED when request is valid")
        void returns201_whenRequestIsValid() throws Exception {
            Game mockGame = buildMockGame();
            when(gameService.createHumanVsBotGame(any(), any(), anyInt(), anyInt()))
                    .thenReturn(mockGame);

            String body = """
                    {
                      "playerName": "Alice",
                      "playerUserId": "uid-1",
                      "boardSize": 3,
                      "botDifficulty": 1
                    }
                    """;

            mockMvc.perform(post(BASE_URL + "/human-vs-bot")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("returns 400 BAD REQUEST when playerName is missing")
        void returns400_whenPlayerNameMissing() throws Exception {
            String body = """
                    {
                      "playerUserId": "uid-1",
                      "boardSize": 3,
                      "botDifficulty": 1
                    }
                    """;

            mockMvc.perform(post(BASE_URL + "/human-vs-bot")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 BAD REQUEST when botDifficulty is missing")
        void returns400_whenBotDifficultyMissing() throws Exception {
            String body = """
                    {
                      "playerName": "Alice",
                      "playerUserId": "uid-1",
                      "boardSize": 3
                    }
                    """;

            mockMvc.perform(post(BASE_URL + "/human-vs-bot")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("delegates correct fields to GameService")
        void delegatesCorrectFieldsToService() throws Exception {
            Game mockGame = buildMockGame();
            when(gameService.createHumanVsBotGame(any(), any(), anyInt(), anyInt()))
                    .thenReturn(mockGame);

            String body = """
                    {
                      "playerName": "Alice",
                      "playerUserId": "uid-1",
                      "boardSize": 3,
                      "botDifficulty": 1
                    }
                    """;

            mockMvc.perform(post(BASE_URL + "/human-vs-bot")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());

            verify(gameService).createHumanVsBotGame("Alice", "uid-1", 3, 1);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /{gameId}
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /{gameId}")
    class GetGameTests {

        @BeforeEach
        void setUp() {
            Player mockPlayer = mock(Player.class);
            when(mockPlayer.getPlayerId()).thenReturn(USER_ID_ONE);
            when(mockPlayer.getName()).thenReturn(PLAYER_ONE);
        }

        @Test
        @DisplayName("returns 200 OK when game exists")
        void returns200_whenGameExists() throws Exception {
            Game mockGame = buildMockGame();
            when(gameService.getGame(GAME_ID)).thenReturn(mockGame);

            mockMvc.perform(get(BASE_URL + "/{gameId}", GAME_ID))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns 404 NOT FOUND when game does not exist")
        void returns404_whenGameNotFound() throws Exception {
            when(gameService.getGame(GAME_ID))
                    .thenThrow(new GameExceptions.GameNotFoundException("Game not found: " + GAME_ID));

            mockMvc.perform(get(BASE_URL + "/{gameId}", GAME_ID))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("passes gameId path variable to GameService")
        void passesGameIdToService() throws Exception {
            Game mockGame = buildMockGame();
            when(gameService.getGame(GAME_ID)).thenReturn(mockGame);

            mockMvc.perform(get(BASE_URL + "/{gameId}", GAME_ID))
                    .andExpect(status().isOk());

            verify(gameService).getGame(GAME_ID);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /{gameId}/move
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /{gameId}/move")
    class MakeMoveTests {

        @Test
        @DisplayName("returns 200 OK when move is valid")
        void returns200_whenMoveIsValid() throws Exception {
            GameResponse mockResponse = mock(GameResponse.class);
            when(gameService.makeMove(GAME_ID, 1, 2)).thenReturn(mockResponse);

            String body = """
                    {
                      "row": 1,
                      "col": 2
                    }
                    """;

            mockMvc.perform(post(BASE_URL + "/{gameId}/move", GAME_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("returns 404 NOT FOUND when game does not exist")
        void returns404_whenGameNotFound() throws Exception {
            when(gameService.makeMove(GAME_ID, 1, 2))
                    .thenThrow(new GameExceptions.GameNotFoundException("Game not found: " + GAME_ID));

            String body = """
                    {
                      "row": 1,
                      "col": 2
                    }
                    """;

            mockMvc.perform(post(BASE_URL + "/{gameId}/move", GAME_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("delegates gameId, row and col to GameService")
        void delegatesAllArgsToService() throws Exception {
            GameResponse mockResponse = mock(GameResponse.class);
            when(gameService.makeMove(GAME_ID, 1, 2)).thenReturn(mockResponse);

            String body = """
                    {
                      "row": 1,
                      "col": 2
                    }
                    """;

            mockMvc.perform(post(BASE_URL + "/{gameId}/move", GAME_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());

            verify(gameService).makeMove(GAME_ID, 1, 2);
        }

        @Test
        @DisplayName("returns 409 CONFLICT when cell is already occupied")
        void returns409_whenCellOccupied() throws Exception {
            when(gameService.makeMove(GAME_ID, 1, 2))
                    .thenThrow(new GameExceptions.CellAlreadyOccupiedException(1, 2));

            mockMvc.perform(post(BASE_URL + "/{gameId}/move", GAME_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            { "row": 1, "col": 2 }
                            """))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("returns 409 CONFLICT when game is already over")
        void returns409_whenGameAlreadyOver() throws Exception {
            when(gameService.makeMove(GAME_ID, 1, 2))
                    .thenThrow(new GameExceptions.GameAlreadyOverException(GAME_ID));

            mockMvc.perform(post(BASE_URL + "/{gameId}/move", GAME_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            { "row": 1, "col": 2 }
                            """))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("returns 400 BAD REQUEST when move is invalid")
        void returns400_whenMoveInvalid() throws Exception {
            when(gameService.makeMove(GAME_ID, 1, 2))
                    .thenThrow(new GameExceptions.InvalidMoveException("Move out of bounds"));

            mockMvc.perform(post(BASE_URL + "/{gameId}/move", GAME_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            { "row": 1, "col": 2 }
                            """))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /{gameId}
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /{gameId}")
    class AbandonGameTests {

        @Test
        @DisplayName("returns 204 NO CONTENT when game is abandoned successfully")
        void returns204_whenGameAbandonedSuccessfully() throws Exception {
            doNothing().when(gameService).abandonGame(GAME_ID);

            mockMvc.perform(delete(BASE_URL + "/{gameId}", GAME_ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 404 NOT FOUND when game does not exist")
        void returns404_whenGameNotFound() throws Exception {
            doThrow(new GameExceptions.GameNotFoundException("Game not found: " + GAME_ID))
                    .when(gameService).abandonGame(GAME_ID);

            mockMvc.perform(delete(BASE_URL + "/{gameId}", GAME_ID))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("response body is empty on successful abandon")
        void responseBodyIsEmpty_onSuccessfulAbandon() throws Exception {
            doNothing().when(gameService).abandonGame(GAME_ID);

            mockMvc.perform(delete(BASE_URL + "/{gameId}", GAME_ID))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));
        }

        @Test
        @DisplayName("delegates gameId to GameService")
        void delegatesGameIdToService() throws Exception {
            doNothing().when(gameService).abandonGame(GAME_ID);

            mockMvc.perform(delete(BASE_URL + "/{gameId}", GAME_ID))
                    .andExpect(status().isNoContent());

            verify(gameService).abandonGame(GAME_ID);
        }
    }

    private Game buildMockGame() {
        Cell mockCell = mock(Cell.class);
        when(mockCell.toString()).thenReturn(" ");

        Board mockBoard = mock(Board.class);
        when(mockBoard.getSize()).thenReturn(3);
        when(mockBoard.getCell(anyInt(), anyInt())).thenReturn(mockCell);

        GameBoard mockGameBoard = mock(GameBoard.class);
        when(mockGameBoard.getBoard()).thenReturn(mockBoard);

        Player mockPlayer = mock(Player.class);
        when(mockPlayer.getPlayerId()).thenReturn(USER_ID_ONE);
        when(mockPlayer.getName()).thenReturn(PLAYER_ONE);
        when(mockPlayer.getSymbol()).thenReturn(Symbol.ZERO);

        Game mockGame = mock(Game.class);
        when(mockGame.getGameId()).thenReturn(GAME_ID);
        when(mockGame.getGameBoard()).thenReturn(mockGameBoard);
        when(mockGame.getCurrentPlayer()).thenReturn(mockPlayer);
        when(mockGame.getPlayers()).thenReturn(List.of(mockPlayer));
        when(mockGame.getStatus()).thenReturn(GameEvent.IN_PROGRESS);
        when(mockGame.isOver()).thenReturn(false);
        when(mockGame.getTotalMoves()).thenReturn(0);

        return mockGame;
    }
}

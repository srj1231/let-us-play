package com.game.let_us_play.api;

import com.game.let_us_play.api.request.CreateHumanVsBotRequest;
import com.game.let_us_play.api.request.CreateHumanVsHumanRequest;
import com.game.let_us_play.api.request.MakeMoveRequest;
import com.game.let_us_play.api.response.GameResponse;
import com.game.let_us_play.domain.game.Game;
import com.game.let_us_play.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for game management.
 *
 * Design decisions:
 * - Intentionally thin — no business logic here.
 *   Validates input, calls GameService, maps to response.
 * - @Valid triggers Jakarta validation. Failures go to
 *   GlobalExceptionHandler automatically.
 * - Domain objects never returned directly — always GameResponse.
 * - @CrossOrigin for Angular FE on different port locally.
 *   Tighten to specific origin in production.
 *
 * API:
 *   POST   /api/v1/games/human-vs-human    Create HvH game
 *   POST   /api/v1/games/human-vs-bot      Create HvB game
 *   GET    /api/v1/games/{gameId}           Get game state
 *   POST   /api/v1/games/{gameId}/move      Make a move
 *   DELETE /api/v1/games/{gameId}           Abandon a game
 */
@RestController
@RequestMapping("/api/v1/games")
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/human-vs-human")
    public ResponseEntity<GameResponse> createHumanVsHumanGame(
            @Valid @RequestBody CreateHumanVsHumanRequest request
    ) {
        Game game = gameService.createHumanVsHumanGame(
                request.playerOneName(),
                request.playerOneUserId(),
                request.playerTwoName(),
                request.playerTwoUserId(),
                request.boardSize()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(GameResponse.from(game));
    }

    @PostMapping("/human-vs-bot")
    public ResponseEntity<GameResponse> createHumanVsBotGame(
            @Valid @RequestBody CreateHumanVsBotRequest request
    ) {
        Game game = gameService.createHumanVsBotGame(
                request.playerName(),
                request.playerUserId(),
                request.boardSize(),
                request.botDifficulty()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(GameResponse.from(game));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable String gameId) {
        Game game = gameService.getGame(gameId);
        return ResponseEntity.ok(GameResponse.from(game));
    }

    /**
     * Makes a move in the game.
     * If the next player is a bot, bot move is auto-triggered.
     * Response reflects the state after all moves in this turn.
     */
    @PostMapping("/{gameId}/move")
    public ResponseEntity<GameResponse> makeMove(
            @PathVariable String gameId,
            @Valid @RequestBody MakeMoveRequest request
    ) {
        GameService.MoveResult result = gameService.makeMove(gameId, request.row(), request.col());
        return ResponseEntity.ok(gameService.toGameResponse(result));
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> abandonGame(@PathVariable String gameId) {
        gameService.abandonGame(gameId);
        return ResponseEntity.noContent().build();
    }
}
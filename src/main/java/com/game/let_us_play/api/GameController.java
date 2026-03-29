package com.game.let_us_play.api;

import com.game.let_us_play.api.request.CreateHumanVsBotRequest;
import com.game.let_us_play.api.request.CreateHumanVsHumanRequest;
import com.game.let_us_play.api.request.MakeMoveRequest;
import com.game.let_us_play.api.response.GameResponse;
import com.game.let_us_play.domain.game.Game;
import com.game.let_us_play.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for game management.
 * Intentionally thin — validate, delegate, return.
 *
 * API:
 *   POST   /api/v1/games/human-vs-human
 *   POST   /api/v1/games/human-vs-bot
 *   GET    /api/v1/games/{gameId}
 *   POST   /api/v1/games/{gameId}/move
 *   DELETE /api/v1/games/{gameId}
 */
@RestController
@RequestMapping("/api/v1/games")
@CrossOrigin(origins = "*")
@Tag(name = "Game Management", description = "Endpoints for creating, playing and ending games.")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @Operation(summary = "Start a Human vs Human game")
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
        return ResponseEntity.status(HttpStatus.CREATED).body(GameResponse.created(game));
    }

    @Operation(summary = "Start a Human vs Bot game")
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
        return ResponseEntity.status(HttpStatus.CREATED).body(GameResponse.created(game));
    }

    @Operation(summary = "Get game state by id")
    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable String gameId) {
        Game game = gameService.getGame(gameId);
        return ResponseEntity.ok(GameResponse.from(game));
    }

    @Operation(summary = "Submit a move")
    @PostMapping("/{gameId}/move")
    public ResponseEntity<GameResponse> makeMove(
            @PathVariable String gameId,
            @Valid @RequestBody MakeMoveRequest request
    ) {
        GameResponse response = gameService.makeMove(gameId, request.row(), request.col());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "End a game")
    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> abandonGame(@PathVariable String gameId) {
        gameService.abandonGame(gameId);
        return ResponseEntity.noContent().build();
    }
}

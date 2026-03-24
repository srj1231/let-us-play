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
        return ResponseEntity.status(HttpStatus.CREATED).body(GameResponse.created(game));
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
        return ResponseEntity.status(HttpStatus.CREATED).body(GameResponse.created(game));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable String gameId) {
        Game game = gameService.getGame(gameId);
        return ResponseEntity.ok(GameResponse.from(game));
    }

    @PostMapping("/{gameId}/move")
    public ResponseEntity<GameResponse> makeMove(
            @PathVariable String gameId,
            @Valid @RequestBody MakeMoveRequest request
    ) {
        GameResponse response = gameService.makeMove(gameId, request.row(), request.col());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> abandonGame(@PathVariable String gameId) {
        gameService.abandonGame(gameId);
        return ResponseEntity.noContent().build();
    }
}

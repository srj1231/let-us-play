package com.game.let_us_play.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for creating a Human vs Bot game.
 */
public record CreateHumanVsBotRequest(

        @NotBlank(message = "Player name must not be blank.")
        @Schema(example = "Alice")
        String playerName,

        @Schema(example = "123")
        String playerUserId,       // nullable — guest players have no userId

        @Min(value = 3, message = "Board size must be at least 3.")
        @Schema(example = "4")
        int boardSize,

        @Min(value = 1, message = "Difficulty must be between 1 and 3.")
        @Max(value = 3, message = "Difficulty must be between 1 and 3.")
        int botDifficulty
) {}

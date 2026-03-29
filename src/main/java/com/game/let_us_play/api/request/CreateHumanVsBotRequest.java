package com.game.let_us_play.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * Request body for creating a Human vs Bot game.
 */
public record CreateHumanVsBotRequest(

        @NotBlank(message = "Player name must not be blank.")
        @Size(max = 50)
        @Schema(example = "Alice")
        String playerName,

        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "playerUserId must be a valid UUID."
        )
        @Schema(example = "123")
        String playerUserId,       // nullable — guest players have no userId

        @Min(value = 3, message = "Board size must be between 3 and 10.")
        @Max(value = 10, message = "Board size must be between 3 and 10.")
        @Schema(example = "4")
        int boardSize,

        @Min(value = 1, message = "Difficulty must be between 1 and 3.")
        @Max(value = 3, message = "Difficulty must be between 1 and 3.")
        @Schema(example = "3")
        int botDifficulty
) {}

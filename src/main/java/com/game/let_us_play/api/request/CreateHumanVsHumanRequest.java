package com.game.let_us_play.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for creating a Human vs Human game.
 *
 * Jakarta validation annotations — Spring validates these
 * before the controller method is called. If validation
 * fails, Spring returns 400 Bad Request automatically.
 * No manual null checks needed in the controller.
 */
public record CreateHumanVsHumanRequest(

        @NotBlank(message = "Player one name must not be blank.")
        @Schema(example = "Alice")
        String playerOneName,

        @Schema(example = "123")
        String playerOneUserId,    // nullable — guest players have no userId

        @NotBlank(message = "Player two name must not be blank.")
        @Schema(example = "Brendon")
        String playerTwoName,

        @Schema(example = "123")
        String playerTwoUserId,    // nullable — guest players have no userId

        @Min(value = 3, message = "Board size must be at least 3.")
        @Schema(example = "4")
        int boardSize
) {}
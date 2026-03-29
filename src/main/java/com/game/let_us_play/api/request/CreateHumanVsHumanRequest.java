package com.game.let_us_play.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

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
        @Size(max = 50)
        String playerOneName,

        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "playerUserId must be a valid UUID."
        )
        @Schema(example = "123")
        String playerOneUserId,    // nullable — guest players have no userId

        @NotBlank(message = "Player two name must not be blank.")
        @Schema(example = "Brendon")
        @Size(max = 50)
        String playerTwoName,

        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "playerUserId must be a valid UUID."
        )
        @Schema(example = "123")
        String playerTwoUserId,    // nullable — guest players have no userId

        @Min(value = 3, message = "Board size must be between 3 and 10.")
        @Max(value = 10, message = "Board size must be between 3 and 10.")
        @Schema(example = "4")
        int boardSize
) {}
package com.game.let_us_play.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

/**
 * Request body for making a move.
 */
public record MakeMoveRequest(

        @Min(value = 0, message = "Row must be 0 or greater.")
        @Schema(example = "1")
        int row,

        @Min(value = 0, message = "Col must be 0 or greater.")
        @Schema(example = "2")
        int col
) {}

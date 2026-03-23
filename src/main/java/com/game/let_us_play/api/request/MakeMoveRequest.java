package com.game.let_us_play.api.request;

import jakarta.validation.constraints.Min;

/**
 * Request body for making a move.
 */
public record MakeMoveRequest(

        @Min(value = 0, message = "Row must be 0 or greater.")
        int row,

        @Min(value = 0, message = "Col must be 0 or greater.")
        int col
) {}

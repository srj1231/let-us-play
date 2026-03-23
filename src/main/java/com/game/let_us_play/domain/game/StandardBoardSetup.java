package com.game.let_us_play.domain.game;

import com.game.let_us_play.domain.board.Board;
import org.springframework.stereotype.Component;

/**
 * Standard square board — default implementation of GameBoardFeatures.
 *
 * Design decisions:
 * - Creates a standard square Board of the given size.
 *   Nothing special — just new Board(size). The value of
 *   this class is that Game never calls new Board() directly.
 *   All board creation goes through this interface, keeping
 *   Game decoupled from the Board constructor.
 *
 * - printBoard() adds row/col index headers — makes the
 *   output readable for debugging and API responses.
 *   The Angular FE will use the structured GameState for
 *   rendering, but this string is useful for logs and
 *   during development.
 *
 * - @Component("standardBoardSetup") — named bean for
 *   explicit injection when multiple implementations exist.
 *
 * Example output for 3x3:
 *
 *     0   1   2
 *  0  X | . | O
 *     ---+---+---
 *  1  . | X | .
 *     ---+---+---
 *  2  O | . | X
 */
@Component("standardBoardSetup")
public class StandardBoardSetup implements GameBoardFeatures {

    @Override
    public Board initBoard(int size) {
        if (size < 3) {
            throw new IllegalArgumentException(
                    "Board size must be at least 3. Got: " + size
            );
        }
        return new Board(size);
    }

    @Override
    public String printBoard(Board board) {
        int size = board.getSize();
        StringBuilder sb = new StringBuilder();

        // Column headers
        sb.append("    ");
        for (int col = 0; col < size; col++) {
            sb.append(String.format("%-4d", col));
        }
        sb.append("\n");

        // Rows
        for (int row = 0; row < size; row++) {
            // Row index
            sb.append(String.format("%-4d", row));

            // Cells
            for (int col = 0; col < size; col++) {
                sb.append(board.getCell(row, col).toString());
                if (col < size - 1) sb.append(" | ");
            }
            sb.append("\n");

            // Row divider (not after last row)
            if (row < size - 1) {
                sb.append("    ");
                sb.append("----+".repeat(size - 1));
                sb.append("----\n");
            }
        }

        return sb.toString();
    }
}

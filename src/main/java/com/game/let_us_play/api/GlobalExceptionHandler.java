package com.game.let_us_play.api;

import com.game.let_us_play.common.GameExceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Centralised exception handling for all controllers.
 *
 * Design decisions:
 * - @RestControllerAdvice intercepts all exceptions thrown
 *   from any controller and converts them to clean JSON responses.
 *   Controllers never write error responses manually.
 *
 * - Each domain exception maps to a specific HTTP status.
 *   GameNotFoundException    → 404
 *   CellAlreadyOccupied      → 409 Conflict
 *   InvalidMove              → 400 Bad Request
 *   GameAlreadyOver          → 409 Conflict
 *   Validation failures      → 400 Bad Request
 *   Everything else          → 500 Internal Server Error
 *
 * - ErrorResponse is a record — consistent JSON shape every time.
 *   Angular FE can rely on this shape for error handling.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(GameExceptions.GameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGameNotFound(GameExceptions.GameNotFoundException ex) {
        log.warn("Game not found: {}", ex.getGameId());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(GameExceptions.CellAlreadyOccupiedException.class)
    public ResponseEntity<ErrorResponse> handleCellOccupied(GameExceptions.CellAlreadyOccupiedException ex) {
        log.warn("Cell already occupied: ({}, {})", ex.getRow(), ex.getCol());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(GameExceptions.InvalidMoveException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMove(GameExceptions.InvalidMoveException ex) {
        log.warn("Invalid move: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(GameExceptions.GameAlreadyOverException.class)
    public ResponseEntity<ErrorResponse> handleGameOver(GameExceptions.GameAlreadyOverException ex) {
        log.warn("Move on finished game: {}", ex.getGameId());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred."));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ErrorResponse(415, "Unsupported Media Type", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, "Bad Request", "Malformed or unreadable request body", Instant.now()));
    }

    public record ErrorResponse(
            int status,
            String error,
            String message,
            Instant timestamp
    ) {
        public static ErrorResponse of(HttpStatus status, String message) {
            return new ErrorResponse(
                    status.value(),
                    status.getReasonPhrase(),
                    message,
                    Instant.now()
            );
        }
    }
}


package com.pfe.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler — gestionnaire centralisé des erreurs HTTP pour MICROFINA++.
 *
 * <h2>Erreurs gérées</h2>
 * <ul>
 *   <li>{@link BusinessException}              → 422 Unprocessable Entity</li>
 *   <li>{@link ResourceNotFoundException}      → 404 Not Found</li>
 *   <li>{@link ClotureException}               → 409 Conflict</li>
 *   <li>{@link MethodArgumentNotValidException}→ 400 Bad Request (validation @Valid)</li>
 *   <li>{@link AccessDeniedException}          → 403 Forbidden</li>
 *   <li>{@link Exception}                      → 500 Internal Server Error (fallback)</li>
 * </ul>
 *
 * <p>Toutes les réponses d'erreur suivent la structure {@link ErrorResponse}.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── BusinessException — 422 ───────────────────────────────────────────────

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex, HttpServletRequest req) {

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of(
                        422,
                        "Unprocessable Entity",
                        ex.getCode(),
                        ex.getMessage(),
                        req.getRequestURI()
                ));
    }

    // ── ResourceNotFoundException — 404 ──────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest req) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        404,
                        "Not Found",
                        "RESOURCE_NOT_FOUND",
                        ex.getMessage(),
                        req.getRequestURI()
                ));
    }

    // ── ClotureException — 409 ────────────────────────────────────────────────

    @ExceptionHandler(ClotureException.class)
    public ResponseEntity<ErrorResponse> handleCloture(
            ClotureException ex, HttpServletRequest req) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        409,
                        "Conflict",
                        "CLOTURE_CONFLICT",
                        ex.getMessage(),
                        req.getRequestURI()
                ));
    }

    // ── MethodArgumentNotValidException — 400 ────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        String message = "Données invalides : " + String.join(", ", details);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        400,
                        "Bad Request",
                        "VALIDATION_ERROR",
                        message,
                        req.getRequestURI(),
                        details
                ));
    }

    // ── AccessDeniedException — 403 ───────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest req) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(
                        403,
                        "Forbidden",
                        "ACCESS_DENIED",
                        "Accès refusé — privilège insuffisant pour cette opération.",
                        req.getRequestURI()
                ));
    }

    // ── NoResourceFoundException — 404 (static resources, favicon, etc.) ───────

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(
            NoResourceFoundException ex, HttpServletRequest req) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        404,
                        "Not Found",
                        "RESOURCE_NOT_FOUND",
                        ex.getMessage(),
                        req.getRequestURI()
                ));
    }

    // ── Fallback — 500 ────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest req) {

        log.error("Unhandled exception on {} {}", req.getMethod(), req.getRequestURI(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        500,
                        "Internal Server Error",
                        "INTERNAL_ERROR",
                        "Une erreur inattendue s'est produite. Contactez l'administrateur.",
                        req.getRequestURI()
                ));
    }
}

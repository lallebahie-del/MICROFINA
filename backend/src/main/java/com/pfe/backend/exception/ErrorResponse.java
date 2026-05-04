package com.pfe.backend.exception;

import java.time.Instant;
import java.util.List;

/**
 * Structure de réponse d'erreur standardisée pour toutes les API MICROFINA++.
 *
 * <pre>
 * {
 *   "timestamp": "2025-01-15T10:30:00Z",
 *   "status": 422,
 *   "error": "Unprocessable Entity",
 *   "code": "BUSINESS_ERROR",
 *   "message": "Transition interdite : BROUILLON → DEBLOQUE",
 *   "path": "/api/v1/credits/42/transitionner",
 *   "details": []
 * }
 * </pre>
 */
public record ErrorResponse(
        Instant    timestamp,
        int        status,
        String     error,
        String     code,
        String     message,
        String     path,
        List<String> details
) {
    /** Constructeur rapide sans détails de validation. */
    public static ErrorResponse of(int status, String error, String code,
                                   String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, code,
                                 message, path, List.of());
    }

    /** Constructeur avec liste de détails (validation field errors). */
    public static ErrorResponse of(int status, String error, String code,
                                   String message, String path, List<String> details) {
        return new ErrorResponse(Instant.now(), status, error, code,
                                 message, path, details);
    }
}

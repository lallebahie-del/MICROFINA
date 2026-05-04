package com.pfe.backend.dto;

/**
 * Corps de la réponse POST /api/auth/login.
 *
 * <p>Le frontend Angular stocke {@code token} dans {@code localStorage}
 * et l'envoie ensuite dans chaque requête via
 * {@code Authorization: Bearer <token>}.</p>
 */
public record LoginResponse(
        String token,
        String username,
        String role,
        long   expiresInMs
) {}

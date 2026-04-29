package com.pfe.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Corps de la requête POST /api/auth/login.
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {}

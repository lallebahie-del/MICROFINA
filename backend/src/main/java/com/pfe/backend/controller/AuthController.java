package com.pfe.backend.controller;

import com.microfina.security.JwtService;
import com.pfe.backend.dto.LoginRequest;
import com.pfe.backend.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController — endpoints d'authentification JWT.
 *
 * <h2>Endpoints</h2>
 * <pre>
 *   POST /api/auth/login   — authentification, retourne { token, username, role, expiresInMs }
 *   GET  /api/auth/me      — identité de l'utilisateur courant (token requis)
 * </pre>
 *
 * <h2>Usage Angular</h2>
 * <ol>
 *   <li>POST /api/auth/login → stocker le token dans localStorage</li>
 *   <li>Toutes les requêtes suivantes : Authorization: Bearer {token}</li>
 * </ol>
 */
@Tag(name = "Authentification", description = "Login JWT et identité utilisateur")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService            jwtService;

    public AuthController(AuthenticationManager authManager, JwtService jwtService) {
        this.authManager = authManager;
        this.jwtService  = jwtService;
    }

    // ── POST /api/auth/login ──────────────────────────────────────────────

    /**
     * Authentifie l'utilisateur et retourne un token JWT.
     *
     * @param request { username, password }
     * @return 200 + LoginResponse ou 401 si les identifiants sont incorrects
     */
    @Operation(summary = "Authentification — retourne un token JWT")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(), request.password()));

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            // Priorité : retourner un ROLE_* si présent, sinon fallback sur une authority PRIV_*
            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> a != null && a.startsWith("ROLE_"))
                    .findFirst()
                    .orElseGet(() -> userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .findFirst()
                            .orElse("ROLE_AGENT"));

            return ResponseEntity.ok(new LoginResponse(
                    token,
                    userDetails.getUsername(),
                    role,
                    jwtService.getExpirationMs()
            ));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Identifiants incorrects"));
        }
    }

    // ── GET /api/auth/me ─────────────────────────────────────────────────

    /**
     * Retourne l'identité de l'utilisateur porteur du token JWT.
     *
     * @param auth injecté par Spring Security après validation du JWT
     * @return { username, role }
     */
    @Operation(summary = "Identité de l'utilisateur courant")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> me(Authentication auth) {
        // Même logique que /login : renvoyer un ROLE_* lisible si présent
        String role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a != null && a.startsWith("ROLE_"))
                .findFirst()
                .orElseGet(() -> auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse("ROLE_AGENT"));

        return ResponseEntity.ok(Map.of(
                "username", auth.getName(),
                "role",     role
        ));
    }
}

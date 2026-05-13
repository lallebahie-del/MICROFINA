package com.microfina.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JwtService — génération et validation des tokens JWT (JJWT 0.12.x).
 *
 * <h2>Algorithme</h2>
 * <p>HMAC-SHA256 (HS256). La clé secrète est lue depuis la propriété
 * {@code microfina.jwt.secret} (min 32 caractères UTF-8). En production,
 * injecter via variable d'environnement {@code MICROFINA_JWT_SECRET}.</p>
 *
 * <h2>Structure du token</h2>
 * <pre>
 *   Header : { alg: HS256, typ: JWT }
 *   Payload: { sub: username, roles: "ROLE_ADMIN,ROLE_AGENT", iat: ..., exp: ... }
 * </pre>
 *
 * <h2>Durée de vie</h2>
 * <p>Configurable via {@code microfina.jwt.expiration-ms} (défaut : 8 heures).</p>
 */
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(
            @Value("${microfina.jwt.secret:microfina-secret-key-change-in-production-2024!}")
            String secret,
            @Value("${microfina.jwt.expiration-ms:28800000}")   // 8 heures
            long expirationMs) {

        this.secretKey   = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // ── Génération ────────────────────────────────────────────────────────

    /**
     * Génère un token JWT signé pour l'utilisateur authentifié.
     *
     * @param userDetails utilisateur Spring Security
     * @return token JWT compact (String)
     */
    public String generateToken(UserDetails userDetails) {
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    // ── Validation ────────────────────────────────────────────────────────

    /**
     * Valide un token JWT : signature + expiration.
     *
     * @param token       token JWT
     * @param userDetails utilisateur à comparer
     * @return {@code true} si le token est valide pour cet utilisateur
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // ── Extraction de claims ──────────────────────────────────────────────

    /**
     * Extrait le nom d'utilisateur ({@code sub}) du token.
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Durée de validité restante en millisecondes (négative si expiré).
     */
    public long getExpirationMs() {
        return expirationMs;
    }

    // ── Privés ────────────────────────────────────────────────────────────

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}

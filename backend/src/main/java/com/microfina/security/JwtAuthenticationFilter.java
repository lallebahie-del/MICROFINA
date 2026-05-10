package com.microfina.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter — filtre HTTP qui extrait et valide le token JWT
 * présent dans l'en-tête {@code Authorization: Bearer <token>}.
 *
 * <p>Exécuté une seule fois par requête ({@link OncePerRequestFilter}).
 * Si le token est valide, un {@link UsernamePasswordAuthenticationToken}
 * est positionné dans le {@link SecurityContextHolder} pour que Spring
 * Security considère la requête comme authentifiée.</p>
 *
 * <p>Si l'en-tête est absent ou malformé, le filtre laisse passer la requête
 * sans token — Spring Security refusera ensuite les endpoints protégés.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final MicrofinaUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   MicrofinaUserDetailsService userDetailsService) {
        this.jwtService       = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest  request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain         filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Aucun header Bearer → passer au filtre suivant
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraire le token (après "Bearer ")
        final String token    = authHeader.substring(7);
        final String username;

        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            // Token malformé ou signature invalide → laisser Security rejeter
            filterChain.doFilter(request, response);
            return;
        }

        // Valider uniquement si l'utilisateur n'est pas déjà authentifié
        if (username != null &&
            SecurityContextHolder.getContext().getAuthentication() == null) {

            // L'utilisateur référencé par le JWT peut avoir été supprimé ou désactivé.
            // Dans ce cas, on laisse le contexte non authentifié → Spring Security
            // renverra 401/403, le client mobile pourra clear son token et re-login.
            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(username);
            } catch (Exception e) {
                filterChain.doFilter(request, response);
                return;
            }

            if (jwtService.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Ne pas appliquer ce filtre sur l'endpoint de login (pas encore de token).
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/auth/login") || path.equals("/actuator/health");
    }
}

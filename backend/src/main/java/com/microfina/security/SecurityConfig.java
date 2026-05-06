package com.microfina.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * SecurityConfig – Spring Security 6 configuration for Microfina++.
 *
 * ══════════════════════════════════════════════════════════════════════
 * How authentication wires up automatically
 * ══════════════════════════════════════════════════════════════════════
 * Spring Security 6 detects two beans in the application context:
 *   1. MicrofinaUserDetailsService  (@Service, implements UserDetailsService)
 *   2. BCryptPasswordEncoder        (@Bean PasswordEncoder, defined below)
 * From these two beans Spring Security automatically builds a
 * DaoAuthenticationProvider and an AuthenticationManager – no explicit
 * @Bean declarations for either are needed (and adding them would
 * conflict with Security's internally managed AuthenticationManager).
 *
 * ══════════════════════════════════════════════════════════════════════
 * Authorisation matrix
 * ══════════════════════════════════════════════════════════════════════
 *
 *   URL pattern                    │  Required role
 *   ───────────────────────────────┼──────────────────────────
 *   /api/admin/**                  │  ROLE_ADMIN
 *   POST /api/credits/valider/**   │  ROLE_COMITE
 *   /api/membres/**                │  ROLE_AGENT or ROLE_ADMIN
 *   /api/reporting/**              │  ROLE_ADMIN
 *   /api/cloture/**                │  ROLE_ADMIN
 *   /actuator/health               │  public
 *   Any other /api/**              │  authenticated (any role)
 *
 * ══════════════════════════════════════════════════════════════════════
 * Test credentials  (seeded by test-data-seeder.xml SEED-003)
 * ══════════════════════════════════════════════════════════════════════
 *   admin        / Admin@1234    → ROLE_ADMIN
 *   aminata.sow  / Agent@1234    → ROLE_AGENT
 *   oumar.ba     / Comite@1234   → ROLE_COMITE
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // ── Password encoder ──────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // ── AuthenticationManager exposé pour le login endpoint ──────────

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ── SessionRegistry pour AdminMonitoringController ────────────────
    //
    // Bien que l'API soit stateless (JWT, pas de HttpSession), un bean
    // SessionRegistry doit exister pour que AdminMonitoringController
    // puisse l'injecter et exposer /api/admin/monitoring/sessions.
    // En mode stateless le registre reste vide — c'est attendu.
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    // ── HTTP security filter chain ────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // Stateless REST API – no CSRF tokens, no HttpSession
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Authorisation rules — Phase 11 : la sécurité fine est portée par les
            // @PreAuthorize de chaque contrôleur (hasAuthority('PRIV_xxx')).
            // Ici on se contente de la règle minimale : toute route /api/** exige
            // une authentification valide, sauf les endpoints publics déclarés.
            .authorizeHttpRequests(auth -> auth
                // ── Public ────────────────────────────────────────────────
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/v1/wallet/callback").permitAll()

                // ── Swagger UI / OpenAPI (Phase 11.1) ────────────────────
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**"
                ).permitAll()

                // ── Tout le reste : authentifié ───────────────────────────
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )

            // JWT Bearer Token — remplace HTTP Basic
            .addFilterBefore(jwtAuthenticationFilter,
                             UsernamePasswordAuthenticationFilter.class)

            // CORS – permit the Angular dev server to call this API
            .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    // ── CORS ──────────────────────────────────────────────────────────

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Dev : autoriser tous les ports localhost (Angular CLI choisit parfois
        // un port aléatoire si 4200 est occupé). En prod, restreindre à l'origine
        // exacte du frontend déployé.
        // Note Spring : avec allowCredentials=true, on ne peut pas utiliser
        // setAllowedOrigins("*") — il faut passer par setAllowedOriginPatterns.
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "http://127.0.0.1:*"
        ));
        config.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        config.setAllowedHeaders(List.of(
            "Authorization", "Content-Type", "Accept", "X-Requested-With"
        ));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

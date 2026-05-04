package com.pfe.backend.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * MicrofinaHealthIndicator — indicateur de santé personnalisé.
 * Exposé via GET /actuator/health avec détails membres actifs et utilisateurs actifs.
 */
@Component
public class MicrofinaHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbc;

    public MicrofinaHealthIndicator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Health health() {
        try {
            Integer membres = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Membres WHERE ETAT = 'ACTIF'", Integer.class);
            Integer utilisateurs = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Utilisateur WHERE actif = 1", Integer.class);
            return Health.up()
                .withDetail("membres_actifs", membres != null ? membres : 0)
                .withDetail("utilisateurs_actifs", utilisateurs != null ? utilisateurs : 0)
                .withDetail("base_de_donnees", "SQL Server — connectée")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("erreur", e.getMessage())
                .build();
        }
    }
}

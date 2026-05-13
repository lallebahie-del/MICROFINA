package com.pfe.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

/**
 * Réapplique les définitions corrigées des vues {@code vue_indicateurs_performance}
 * et {@code vue_liste_clients} (P10-410 — colonnes FERME/BLOQUE en NVARCHAR).
 *
 * <p>Sans cela, des bases déjà migrées avec l’ancienne vue provoquent une erreur SQL
 * Server « Conversion failed when converting the nvarchar value 'N' to data type int »
 * sur {@code GET /api/v1/reporting/indicateurs}.</p>
 *
 * <p>Exécuté uniquement pour une URL JDBC SQL Server.</p>
 */
@Component
public class ReportingViewsP10_410Sync implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ReportingViewsP10_410Sync.class);

    private final DataSource dataSource;
    private final Environment  environment;

    public ReportingViewsP10_410Sync(DataSource dataSource, Environment environment) {
        this.dataSource   = dataSource;
        this.environment  = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        String url = environment.getProperty("spring.datasource.url", "");
        if (!url.contains("sqlserver")) {
            log.debug("Synchronisation P10-410 des vues reporting ignorée (pas SQL Server).");
            return;
        }

        var encoded = new EncodedResource(
                new ClassPathResource("sql/P10-410-reporting-views.sql"),
                StandardCharsets.UTF_8);

        var conn = DataSourceUtils.getConnection(dataSource);
        try {
            ScriptUtils.executeSqlScript(conn, encoded);
            log.info("Vues reporting P10-410 synchronisées (FERME/BLOQUE NVARCHAR).");
        } catch (Exception e) {
            log.warn("Impossible d’appliquer le script P10-410 des vues reporting : {}", e.getMessage());
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}

package com.pfe.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Microfina++ – Spring Boot entry point.
 *
 * scanBasePackages on @SpringBootApplication covers both the application
 * package AND the domain package so that @Service, @Configuration, and
 * other beans defined in com.microfina are detected automatically.
 * Using scanBasePackages (rather than a separate @ComponentScan) preserves
 * the TypeExcludeFilter and AutoConfigurationExcludeFilter that Boot adds
 * by default, avoiding auto-configuration conflicts.
 *
 * @EntityScan is required because the JPA entities live in com.microfina.entity,
 * which is outside the default scan root (com.pfe.backend).
 * No @EnableJpaRepositories is needed because all data access uses
 * EntityManager directly – there are no Spring Data repository interfaces.
 */
@SpringBootApplication(scanBasePackages = {"com.pfe.backend", "com.microfina"})
@EntityScan(basePackages = "com.microfina.entity")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = "com.pfe.backend.repository")
public class BackendApplication {

    public static void main(String[] args) {
        loadDotEnvFromRepositoryRoot();
        SpringApplication.run(BackendApplication.class, args);
    }

    /**
     * Charge un fichier {@code .env} à la racine du dépôt ou dans {@code backend/}
     * (même format que Docker Compose) et pousse les paires clé=valeur dans les
     * {@link System#setProperty propriétés système} si la variable d'environnement
     * n'est pas déjà définie. Ainsi {@code MICROFINA_DB_URL} dans {@code .env}
     * aligne le backend sur la même base qu'Adminer sans exporter manuellement
     * les variables sous Windows.
     */
    static void loadDotEnvFromRepositoryRoot() {
        Path envPath = resolveEnvFile(Paths.get("").toAbsolutePath());
        if (envPath == null || !Files.isRegularFile(envPath)) {
            return;
        }
        try {
            for (String raw : Files.readAllLines(envPath, StandardCharsets.UTF_8)) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int hash = line.indexOf('#');
                if (hash >= 0) {
                    line = line.substring(0, hash).trim();
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String val = line.substring(eq + 1).trim();
                if ((val.startsWith("\"") && val.endsWith("\""))
                        || (val.startsWith("'") && val.endsWith("'"))) {
                    val = val.substring(1, val.length() - 1);
                }
                if (!key.isEmpty() && System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, val);
                }
            }
        } catch (IOException e) {
            System.err.println(">>> Impossible de lire .env (" + envPath + "): " + e.getMessage());
        }
    }

    private static Path resolveEnvFile(Path cwd) {
        Path a = cwd.resolve(".env");
        if (Files.isRegularFile(a)) {
            return a;
        }
        if (cwd.getFileName() != null && "backend".equalsIgnoreCase(cwd.getFileName().toString())) {
            Path parent = cwd.getParent();
            if (parent != null) {
                Path b = parent.resolve(".env");
                if (Files.isRegularFile(b)) {
                    return b;
                }
            }
        }
        return null;
    }

    @Bean
    public CommandLineRunner checkDb(EntityManager em, Environment env) {
        return args -> {
            String jdbcUrl = env.getProperty("spring.datasource.url", "");
            System.out.println(">>> BACKEND JDBC URL (doit correspondre au serveur vu dans Adminer) : " + jdbcUrl);
            try {
                Number agents = (Number) em.createNativeQuery("SELECT COUNT(*) FROM AGENT_CREDIT").getSingleResult();
                System.out.println(">>> STARTUP CHECK: AGENT_CREDIT COUNT = " + agents.longValue());
            } catch (Exception e) {
                System.err.println(">>> STARTUP CHECK AGENT_CREDIT FAILED: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            try {
                Number membres = (Number) em.createNativeQuery("SELECT COUNT(*) FROM membres").getSingleResult();
                System.out.println(">>> STARTUP CHECK: membres COUNT = " + membres.longValue());
            } catch (Exception e) {
                System.err.println(">>> STARTUP CHECK membres FAILED: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        };
    }
}

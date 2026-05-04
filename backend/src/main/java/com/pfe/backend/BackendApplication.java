package com.pfe.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.domain.EntityScan;

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
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner checkDb(EntityManager em) {
        return args -> {
            try {
                Number count = (Number) em.createNativeQuery("SELECT COUNT(*) FROM AGENT_CREDIT").getSingleResult();
                System.out.println(">>> STARTUP CHECK: AGENT_CREDIT COUNT = " + count.longValue());
            } catch (Exception e) {
                System.err.println(">>> STARTUP CHECK FAILED: " + e.getClass().getName() + ": " + e.getMessage());
            }
        };
    }
}

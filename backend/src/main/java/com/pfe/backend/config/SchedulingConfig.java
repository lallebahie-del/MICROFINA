package com.pfe.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SchedulingConfig — active le scheduler Spring pour les traitements planifiés.
 *
 * <p>Séparé de BackendApplication pour respecter le principe de séparation des
 * responsabilités. Peut être exclu en test via
 * {@code @SpringBootTest(excludeAutoConfiguration = SchedulingConfig.class)}.</p>
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}

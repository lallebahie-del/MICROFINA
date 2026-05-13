package com.pfe.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminMonitoringController — tableau de bord technique de l'application.
 *
 * <h2>Endpoints</h2>
 * <pre>
 *  GET /api/v1/admin/monitoring/metrics   → métriques JVM (mémoire, threads)
 *  GET /api/v1/admin/monitoring/database  → état de la connexion base de données
 *  GET /api/v1/admin/monitoring/jobs      → dernier statut des jobs planifiés
 *  GET /api/v1/admin/monitoring/sessions  → utilisateurs connectés
 * </pre>
 *
 * <h2>Sécurité</h2>
 * <p>Accès réservé aux utilisateurs portant le privilège {@code PRIV_ADMIN}.</p>
 */
@Tag(name = "Admin — Monitoring", description = "Métriques applicatives et système")
@RestController
@RequestMapping("/api/v1/admin/monitoring")
@PreAuthorize("hasAuthority('PRIV_ADMIN')")
public class AdminMonitoringController {

    private static final Instant START_TIME = Instant.now();

    private final JdbcTemplate jdbc;

    public AdminMonitoringController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // =========================================================================
    //  Métriques JVM
    // =========================================================================

    /**
     * Retourne les métriques mémoire et threads de la JVM.
     *
     * <pre>GET /api/v1/admin/monitoring/metrics</pre>
     */
    @Operation(summary = "Métriques JVM (mémoire, threads, uptime)")
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        MemoryMXBean mem     = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();

        long usedMb  = mem.getHeapMemoryUsage().getUsed()  / (1024 * 1024);
        long maxMb   = mem.getHeapMemoryUsage().getMax()   / (1024 * 1024);
        Duration up  = Duration.between(START_TIME, Instant.now());

        Map<String, Object> result = new HashMap<>();
        result.put("memoryUsedMb",        usedMb);
        result.put("memoryMaxMb",         maxMb);
        result.put("uptime",              formatDuration(up));
        result.put("activeThreads",       threads.getThreadCount());
        result.put("requestsPerMinute",   0);   // placeholder — brancher Micrometer si disponible
        return result;
    }

    // =========================================================================
    //  Base de données
    // =========================================================================

    /**
     * Vérifie la disponibilité de la base de données et retourne des métriques
     * de connexion.
     *
     * <pre>GET /api/v1/admin/monitoring/database</pre>
     */
    @Operation(summary = "État de la connexion base de données")
    @GetMapping("/database")
    public Map<String, Object> getDatabase() {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer activeConns = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys.dm_exec_sessions WHERE is_user_process = 1",
                Integer.class);
            Long dbSizeMb = jdbc.queryForObject(
                "SELECT ISNULL(SUM(CAST(size AS BIGINT)) * 8 / 1024, 0) FROM sys.database_files",
                Long.class);

            result.put("status",            "UP");
            result.put("activeConnections", activeConns != null ? activeConns : 0);
            result.put("maxConnections",    32767);
            result.put("pendingQueries",    0);
            result.put("dbSizeMb",          dbSizeMb != null ? dbSizeMb : 0);
        } catch (Exception e) {
            result.put("status",            "DOWN");
            result.put("error",             e.getMessage());
            result.put("activeConnections", 0);
            result.put("maxConnections",    0);
            result.put("pendingQueries",    0);
            result.put("dbSizeMb",          0);
        }
        return result;
    }

    // =========================================================================
    //  Jobs planifiés
    // =========================================================================

    /**
     * Retourne le dernier statut connu de chaque job planifié.
     *
     * <pre>GET /api/v1/admin/monitoring/jobs</pre>
     */
    @Operation(summary = "Dernier statut des jobs planifiés")
    @GetMapping("/jobs")
    public List<Map<String, Object>> getJobs() {
        return jdbc.queryForList(
            """
            SELECT nom_job          AS nomJob,
                   date_debut       AS dernierExecution,
                   statut,
                   nb_traites       AS nbTraites
            FROM (
                SELECT nom_job, date_debut, statut, nb_traites,
                       ROW_NUMBER() OVER (PARTITION BY nom_job ORDER BY date_debut DESC) AS rn
                FROM   job_execution
            ) ranked
            WHERE  rn = 1
            ORDER BY nom_job
            """);
    }

    // =========================================================================
    //  Sessions actives
    // =========================================================================

    /**
     * Retourne la liste des utilisateurs actuellement connectés (sessions JWT
     * actives enregistrées dans le {@link SessionRegistry} Spring Security).
     *
     * <pre>GET /api/v1/admin/monitoring/sessions</pre>
     */
    @Operation(summary = "Utilisateurs connectés (sessions actives)")
    @GetMapping("/sessions")
    public Map<String, Object> getSessions() {
        // JWT is stateless — derive active sessions from audit log:
        // users who logged in within the last 8 hours (token validity) with no logout after that login.
        List<Map<String, Object>> sessionList = jdbc.queryForList("""
            SELECT l.utilisateur AS login,
                   l.date_action AS lastActivity,
                   0             AS expired
            FROM   JournalAudit l
            WHERE  l.action = 'LOGIN'
              AND  l.date_action >= DATEADD(HOUR, -8, GETDATE())
              AND  NOT EXISTS (
                       SELECT 1 FROM JournalAudit lo
                       WHERE  lo.utilisateur = l.utilisateur
                         AND  lo.action      = 'LOGOUT'
                         AND  lo.date_action > l.date_action
                   )
            ORDER BY l.date_action DESC
            """);

        Map<String, Object> result = new HashMap<>();
        result.put("activeUsers", sessionList.size());
        result.put("sessions",    sessionList);
        return result;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static String formatDuration(Duration d) {
        long days    = d.toDays();
        long hours   = d.toHoursPart();
        long minutes = d.toMinutesPart();
        return String.format("%dd %02dh %02dm", days, hours, minutes);
    }
}

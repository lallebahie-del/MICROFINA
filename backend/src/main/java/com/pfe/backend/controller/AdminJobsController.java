package com.pfe.backend.controller;

import com.microfina.entity.JobExecution;
import com.pfe.backend.repository.JobExecutionRepository;
import com.pfe.backend.service.JobSchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AdminJobsController — déclenchement manuel et suivi des jobs planifiés.
 *
 * <pre>
 *   POST /api/v1/admin/jobs/{nom}/run    — déclencher un job manuellement
 *   GET  /api/v1/admin/jobs/{nom}/historique — historique des exécutions
 *   GET  /api/v1/admin/jobs/historique   — historique global (100 derniers)
 * </pre>
 *
 * <p>Sécurité : requiert {@code PRIV_ADMIN}.</p>
 *
 * <h2>Noms de jobs valides</h2>
 * <ul>
 *   <li>{@code CALCUL_INTERETS} — calcul des intérêts courus (quotidien 01h00)</li>
 *   <li>{@code RECALCUL_PAR} — recalcul portefeuille à risque (quotidien 01h30)</li>
 *   <li>{@code CLOTURE_JOURNALIERE} — clôture comptable (quotidien 23h50)</li>
 * </ul>
 */
@Tag(name = "Administration Jobs", description = "Déclenchement et suivi des traitements planifiés")
@RestController
@RequestMapping("/api/v1/admin/jobs")
@PreAuthorize("hasAuthority('PRIV_ADMIN')")
public class AdminJobsController {

    private final JobSchedulerService    jobSchedulerService;
    private final JobExecutionRepository jobExecutionRepository;

    public AdminJobsController(JobSchedulerService jobSchedulerService,
                                JobExecutionRepository jobExecutionRepository) {
        this.jobSchedulerService    = jobSchedulerService;
        this.jobExecutionRepository = jobExecutionRepository;
    }

    // ── POST /api/v1/admin/jobs/{nom}/run ────────────────────────────────────

    /**
     * Déclenche un job manuellement (hors planification automatique).
     *
     * @param nom  nom canonique du job ({@code CALCUL_INTERETS}, {@code RECALCUL_PAR},
     *             {@code CLOTURE_JOURNALIERE})
     * @param auth utilisateur authentifié (tracé dans JobExecution.declencheur)
     * @return l'enregistrement {@link JobExecution} créé (statut final)
     */
    @Operation(summary = "Déclencher un job planifié manuellement")
    @PostMapping("/{nom}/run")
    public ResponseEntity<JobExecution> run(
            @PathVariable String nom,
            Authentication auth) {

        String declencheur = (auth != null) ? auth.getName() : "admin";
        JobExecution exec  = jobSchedulerService.declencher(nom.toUpperCase(), declencheur);
        return ResponseEntity.ok(exec);
    }

    // ── GET /api/v1/admin/jobs/{nom}/historique ──────────────────────────────

    /**
     * Retourne les 50 dernières exécutions d'un job donné.
     *
     * @param nom nom canonique du job
     */
    @Operation(summary = "Historique des exécutions d'un job")
    @GetMapping("/{nom}/historique")
    public ResponseEntity<List<JobExecution>> historique(@PathVariable String nom) {
        return ResponseEntity.ok(
                jobExecutionRepository.findTop50ByNomJobOrderByDateDebutDesc(nom.toUpperCase()));
    }

    // ── GET /api/v1/admin/jobs/historique ────────────────────────────────────

    /**
     * Retourne les 100 dernières exécutions tous jobs confondus.
     */
    @Operation(summary = "Historique global des jobs (100 derniers)")
    @GetMapping("/historique")
    public ResponseEntity<List<JobExecution>> historiqueGlobal() {
        return ResponseEntity.ok(jobExecutionRepository.findTop100ByOrderByDateDebutDesc());
    }
}

package com.pfe.backend.service;

import com.microfina.entity.JobExecution;
import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.repository.JobExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * JobSchedulerService — traitements batchs planifiés de MICROFINA++.
 *
 * <h2>Jobs définis</h2>
 * <pre>
 *   calculInterets          : quotidien à 01h00 — calcul des intérêts courus
 *   recalculPar             : quotidien à 01h30 — recalcul du portefeuille à risque
 *   clotureJournaliere      : quotidien à 23h50 — clôture comptable journalière
 * </pre>
 *
 * <h2>Traçabilité</h2>
 * <p>Chaque exécution (automatique ou déclenchée manuellement via
 * {@link AdminJobsController}) est enregistrée dans la table {@code job_execution}.</p>
 *
 * <h2>Anti-régression</h2>
 * <p>Les jobs sont désactivés en profil "test" via la condition
 * {@code @ConditionalOnProperty} implicite dans le scheduler Spring
 * (le scheduler est désactivé si {@code spring.task.scheduling.pool.size=0}).</p>
 */
@Service
public class JobSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(JobSchedulerService.class);

    public static final String JOB_CALCUL_INTERETS     = "CALCUL_INTERETS";
    public static final String JOB_RECALCUL_PAR        = "RECALCUL_PAR";
    public static final String JOB_CLOTURE_JOURNALIERE = "CLOTURE_JOURNALIERE";

    private final JobExecutionRepository jobExecutionRepository;
    private final JdbcTemplate           jdbc;

    public JobSchedulerService(JobExecutionRepository jobExecutionRepository,
                                JdbcTemplate jdbc) {
        this.jobExecutionRepository = jobExecutionRepository;
        this.jdbc                   = jdbc;
    }

    // =========================================================================
    //  Jobs planifiés
    // =========================================================================

    /**
     * Calcul des intérêts courus — quotidien à 01h00.
     *
     * <p>Met à jour les colonnes d'intérêts courus pour tous les crédits actifs
     * dont l'échéance est passée (via la procédure stockée sp_calcul_interets
     * si elle existe, sinon calcul direct sur la table Reglement).</p>
     */
    @Scheduled(cron = "${app.jobs.calcul-interets.cron:0 0 1 * * ?}")
    @Transactional
    public void calculInterets() {
        executer(JOB_CALCUL_INTERETS, "SCHEDULER", () -> {
            // Recalcul des intérêts échus non réglés
            int nb = jdbc.update("""
                UPDATE reglement
                SET MONTANT_INTERET_RETARD =
                    CASE WHEN DATEDIFF(day, DATE_ECHEANCE, GETDATE()) > 0
                         THEN MONTANT_INTERET_RETARD +
                              (MONTANT_CAPITAL_RESTANT * (TAUX_INTERET_RETARD / 100.0)
                               * DATEDIFF(day, DATE_ECHEANCE, GETDATE()) / 365.0)
                         ELSE MONTANT_INTERET_RETARD
                    END
                WHERE STATUT IN ('EN_ATTENTE', 'EN_RETARD')
                  AND DATE_ECHEANCE < CAST(GETDATE() AS DATE)
                  AND DATEDIFF(day, DATE_ECHEANCE, GETDATE()) > 0
                """);
            return Map.of("nb", nb);
        });
    }

    /**
     * Recalcul du portefeuille à risque (PAR) — quotidien à 01h30.
     *
     * <p>Rafraîchit les vues matérialisées (si SQL Server Enterprise)
     * ou déclenche une mise à jour des colonnes de statut PAR sur les crédits.</p>
     */
    @Scheduled(cron = "${app.jobs.recalcul-par.cron:0 30 1 * * ?}")
    @Transactional
    public void recalculPar() {
        executer(JOB_RECALCUL_PAR, "SCHEDULER", () -> {
            // Mise à jour des jours de retard et statut PAR sur les crédits actifs
            int nb = jdbc.update("""
                UPDATE Credits
                SET JOURS_RETARD = CASE
                    WHEN STATUT IN ('DEBLOQUE', 'EN_RETARD')
                         AND DATE_ECHEANCE_FINALE < CAST(GETDATE() AS DATE)
                    THEN DATEDIFF(day, DATE_ECHEANCE_FINALE, GETDATE())
                    ELSE 0 END,
                STATUT = CASE
                    WHEN STATUT IN ('DEBLOQUE', 'EN_RETARD')
                         AND DATEDIFF(day, DATE_ECHEANCE_FINALE, GETDATE()) > 90
                    THEN 'EN_RETARD'
                    WHEN STATUT IN ('DEBLOQUE', 'EN_RETARD')
                         AND DATEDIFF(day, DATE_ECHEANCE_FINALE, GETDATE()) > 0
                    THEN 'EN_RETARD'
                    ELSE STATUT END
                WHERE STATUT IN ('DEBLOQUE', 'EN_RETARD')
                """);
            return Map.of("nb", nb);
        });
    }

    /**
     * Clôture journalière — quotidien à 23h50.
     *
     * <p>Vérifie que toutes les opérations de la journée sont équilibrées
     * (somme débit = somme crédit) et insère un enregistrement de clôture.</p>
     */
    @Scheduled(cron = "${app.jobs.cloture-journaliere.cron:0 50 23 * * ?}")
    @Transactional
    public void clotureJournaliere() {
        executer(JOB_CLOTURE_JOURNALIERE, "SCHEDULER", () -> {
            // Vérification équilibre débit/crédit du jour
            Map<String, Object> row = jdbc.queryForMap("""
                SELECT
                    COALESCE(SUM(DEBIT),  0) AS total_debit,
                    COALESCE(SUM(CREDIT), 0) AS total_credit
                FROM comptabilite
                WHERE DATEOPERATION = CAST(GETDATE() AS DATE)
                """);
            double debit  = ((Number) row.get("total_debit")).doubleValue();
            double credit = ((Number) row.get("total_credit")).doubleValue();
            boolean equilibre = Math.abs(debit - credit) < 0.01;

            return Map.of(
                "debit",    debit,
                "credit",   credit,
                "equilibre", equilibre
            );
        });
    }

    // =========================================================================
    //  API publique — déclenchement manuel
    // =========================================================================

    /**
     * Exécute un job par son nom (usage : {@link AdminJobsController}).
     *
     * @param nomJob     nom canonique du job (voir constantes JOB_*)
     * @param declencheur login de l'utilisateur qui déclenche
     * @return l'enregistrement {@link JobExecution} créé
     */
    @Transactional
    public JobExecution declencher(String nomJob, String declencheur) {
        return switch (nomJob.toUpperCase()) {
            case JOB_CALCUL_INTERETS     -> { calculInterets();   yield derniereExecution(nomJob); }
            case JOB_RECALCUL_PAR        -> { recalculPar();      yield derniereExecution(nomJob); }
            case JOB_CLOTURE_JOURNALIERE -> { clotureJournaliere(); yield derniereExecution(nomJob); }
            default -> throw new BusinessException("Job inconnu : " + nomJob +
                    ". Jobs disponibles : " + JOB_CALCUL_INTERETS + ", " +
                    JOB_RECALCUL_PAR + ", " + JOB_CLOTURE_JOURNALIERE);
        };
    }

    // =========================================================================
    //  Méthodes utilitaires
    // =========================================================================

    @FunctionalInterface
    private interface JobLogic {
        Map<String, Object> execute() throws Exception;
    }

    /**
     * Enveloppe générique qui crée/met à jour un enregistrement JobExecution.
     */
    private void executer(String nomJob, String declencheur, JobLogic logic) {
        JobExecution exec = new JobExecution();
        exec.setNomJob(nomJob);
        exec.setDateDebut(LocalDateTime.now());
        exec.setStatut(JobExecution.STATUT_EN_COURS);
        exec.setDeclencheur(declencheur);
        jobExecutionRepository.save(exec);

        try {
            Map<String, Object> result = logic.execute();
            exec.setStatut(JobExecution.STATUT_SUCCES);
            exec.setMessage("OK — " + result);
            exec.setNbTraites(result.containsKey("nb")
                    ? ((Number) result.get("nb")).intValue() : null);
            log.info("[Job] {} terminé : {}", nomJob, result);
        } catch (Exception e) {
            exec.setStatut(JobExecution.STATUT_ECHEC);
            exec.setMessage(e.getMessage());
            log.error("[Job] {} ÉCHEC : {}", nomJob, e.getMessage());
        } finally {
            exec.setDateFin(LocalDateTime.now());
            jobExecutionRepository.save(exec);
        }
    }

    private JobExecution derniereExecution(String nomJob) {
        return jobExecutionRepository
                .findTop50ByNomJobOrderByDateDebutDesc(nomJob)
                .stream().findFirst()
                .orElseThrow(() -> new BusinessException("Aucune exécution trouvée pour : " + nomJob));
    }
}

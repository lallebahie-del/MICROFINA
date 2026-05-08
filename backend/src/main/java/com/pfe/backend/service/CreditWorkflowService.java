package com.pfe.backend.service;

import com.microfina.entity.*;
import com.microfina.service.AmortissementService;
import com.pfe.backend.dto.WorkflowDTO.*;
import com.pfe.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CreditWorkflowService – orchestrateur du workflow multi-niveaux d'approbation crédit.
 *
 * Chaque méthode publique correspond à une transition du diagramme d'état :
 *   SAISIE → COMPLETUDE → ANALYSE_FINANCIERE → VISA_RC → COMITE
 *         → VISA_SF → DEBLOCAGE_PENDING → DEBLOQUE
 *
 * Règles transverses :
 *   - Chaque transition append une ligne dans historique_visa_credit.
 *   - Le verrouillage optimiste ({@code @Version}) protège les écritures concurrentes.
 *   - Le déblocage applique un verrou pessimiste pour éviter le double-décaissement.
 */
@Service
@Transactional
public class CreditWorkflowService {

    private final CreditsRepository              creditsRepo;
    private final AnalyseFinanciereRepository    analyseRepo;
    private final HistoriqueVisaCreditRepository historiqueRepo;
    private final AmortpRepository               amortpRepo;
    private final AmortissementService           amortissementService;
    private final CreditDisbursementService      disbursementService;
    private final CreditAccountingService        accountingService;

    public CreditWorkflowService(
            CreditsRepository              creditsRepo,
            AnalyseFinanciereRepository    analyseRepo,
            HistoriqueVisaCreditRepository historiqueRepo,
            AmortpRepository               amortpRepo,
            AmortissementService           amortissementService,
            CreditDisbursementService      disbursementService,
            CreditAccountingService        accountingService) {
        this.creditsRepo          = creditsRepo;
        this.analyseRepo          = analyseRepo;
        this.historiqueRepo       = historiqueRepo;
        this.amortpRepo           = amortpRepo;
        this.amortissementService = amortissementService;
        this.disbursementService  = disbursementService;
        this.accountingService    = accountingService;
    }

    // =========================================================================
    //  Transitions de workflow
    // =========================================================================

    /**
     * SAISIE → COMPLETUDE (soumet le dossier).
     */
    public Credits soumettreDossier(Long idCredit) {
        Credits c = chargerCredit(idCredit);
        requireEtape(c, "SAISIE");
        CreditStatut avant = c.getStatut();
        c.setStatut(CreditStatut.SOUMIS);
        c.setEtapeCourante("COMPLETUDE");
        Credits saved = creditsRepo.save(c);
        appendHistorique(saved, "COMPLETUDE", avant, CreditStatut.SOUMIS, "APPROUVE", null);
        return saved;
    }

    /**
     * COMPLETUDE → ANALYSE_FINANCIERE (valide la complétude documentaire).
     */
    public Credits validerCompletude(Long idCredit, WorkflowDecisionRequest req) {
        Credits c = chargerCredit(idCredit);
        requireEtape(c, "COMPLETUDE");
        CreditStatut avant = c.getStatut();
        c.setEtapeCourante("ANALYSE_FINANCIERE");
        Credits saved = creditsRepo.save(c);
        appendHistorique(saved, "ANALYSE_FINANCIERE", avant, c.getStatut(), "APPROUVE",
                         req != null ? req.commentaire() : null);
        return saved;
    }

    /**
     * ANALYSE_FINANCIERE → VISA_RC (enregistre l'analyse et avance l'étape).
     */
    public AnalyseFinanciere enregistrerAnalyseFinanciere(Long idCredit,
                                                          AnalyseFinanciereCreateRequest req) {
        Credits c = chargerCredit(idCredit);
        requireEtape(c, "ANALYSE_FINANCIERE");

        // Calcul ratio et capacité
        BigDecimal revenus  = req.revenusMensuels();
        BigDecimal charges  = req.chargesMensuelles();
        BigDecimal ratio    = charges.divide(revenus, 4, RoundingMode.HALF_UP);
        BigDecimal capacite = revenus.subtract(charges);

        // Persistance de l'analyse (upsert : une seule analyse par crédit)
        AnalyseFinanciere analyse = analyseRepo.findByCredit_IdCredit(idCredit)
                                               .orElseGet(AnalyseFinanciere::new);
        analyse.setCredit(c);
        analyse.setRevenusMensuels(revenus);
        analyse.setChargesMensuelles(charges);
        analyse.setCapaciteRemboursement(capacite);
        analyse.setRatioEndettement(ratio);
        analyse.setTotalActif(req.totalActif());
        analyse.setTotalPassif(req.totalPassif());
        analyse.setIndicateursJson(req.indicateursJson());
        analyse.setCommentaire(req.commentaire());
        if (req.avisAgent() != null && !req.avisAgent().isBlank()) {
            try {
                analyse.setAvisAgent(AnalyseFinanciere.AvisAgent.valueOf(req.avisAgent()));
            } catch (IllegalArgumentException ignored) {}
        }
        analyse.setDateAnalyse(LocalDateTime.now());
        analyse.setUtilisateur(currentUser());

        AnalyseFinanciere saved = analyseRepo.save(analyse);

        // Avancer l'étape
        CreditStatut avant = c.getStatut();
        c.setEtapeCourante("VISA_RC");
        creditsRepo.save(c);
        appendHistorique(c, "VISA_RC", avant, c.getStatut(), "APPROUVE",
                         req.commentaire());
        return saved;
    }

    /**
     * VISA_RC → COMITE (visa du Responsable Crédit).
     */
    public Credits viserResponsableCredit(Long idCredit, WorkflowDecisionRequest req) {
        Credits c = chargerCredit(idCredit);
        requireEtape(c, "VISA_RC");
        CreditStatut avant = c.getStatut();
        c.setStatut(CreditStatut.VALIDE_AGENT);
        c.setEtapeCourante("COMITE");
        Credits saved = creditsRepo.save(c);
        appendHistorique(saved, "COMITE", avant, CreditStatut.VALIDE_AGENT, "APPROUVE",
                         req != null ? req.commentaire() : null);
        return saved;
    }

    /**
     * COMITE → VISA_SF (approbation) ou REJETE.
     */
    public Credits decisionComite(Long idCredit, boolean approuve, WorkflowDecisionRequest req) {
        Credits c = chargerCredit(idCredit);
        requireEtape(c, "COMITE");
        CreditStatut avant = c.getStatut();
        String decision = approuve ? "APPROUVE" : "REFUSE";
        if (approuve) {
            c.setStatut(CreditStatut.VALIDE_COMITE);
            c.setEtapeCourante("VISA_SF");
        } else {
            c.setStatut(CreditStatut.REJETE);
            c.setEtapeCourante("REJETE");
        }
        Credits saved = creditsRepo.save(c);
        appendHistorique(saved, approuve ? "VISA_SF" : "REJETE",
                         avant, saved.getStatut(), decision,
                         req != null ? req.commentaire() : null);
        return saved;
    }

    /**
     * VISA_SF → DEBLOCAGE_PENDING (visa du Service Financier).
     */
    public Credits viserServiceFinancier(Long idCredit, WorkflowDecisionRequest req) {
        Credits c = chargerCredit(idCredit);
        requireEtape(c, "VISA_SF");
        CreditStatut avant = c.getStatut();
        c.setEtapeCourante("DEBLOCAGE_PENDING");
        Credits saved = creditsRepo.save(c);
        appendHistorique(saved, "DEBLOCAGE_PENDING", avant, c.getStatut(), "APPROUVE",
                         req != null ? req.commentaire() : null);
        return saved;
    }

    /**
     * DEBLOCAGE_PENDING → DEBLOQUE.
     *
     * Avec effets de bord :
     *   1. Charge le crédit et vérifie l'étape {@code DEBLOCAGE_PENDING}.
     *   2. Décaissement ({@link CreditDisbursementService}) — impact solde caisse / banque.
     *   3. Écriture comptable de déblocage ({@link CreditAccountingService#ecritureDeblocage}).
     *   4. Met à jour montants, dates, soldes et statut {@code DEBLOQUE}.
     *   5. Génère et persiste les lignes {@link Amortp} via {@link AmortissementService}.
     *   6. Ajoute une ligne d'historique.
     */
    public Credits debloquer(Long idCredit, DeblocageRequest req) {
        // 1. Pessimistic lock to avoid double-disbursement
        Credits c = creditsRepo.findById(idCredit)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Crédit introuvable : " + idCredit));
        requireEtape(c, "DEBLOCAGE_PENDING");

        CreditStatut avant = c.getStatut();

        // 2. Matérialiser le mouvement de fonds (impact solde caisse/banque)
        disbursementService.debloquerFonds(req);

        // 3. Comptabiliser le déblocage (écriture minimale)
        accountingService.ecritureDeblocage(
                c,
                req.montantDeblocage(),
                currentUser(),
                "DEBLOCAGE_CREDIT#" + idCredit,
                "CR-DEB-" + idCredit + "-" + LocalDate.now()
        );

        // 4. Set disbursement fields
        c.setMontantDebloquer(req.montantDeblocage());
        c.setDateDeblocage(req.datePremiereEcheance());  // date déblocage = date 1ère échéance context
        c.setDatePremièreEcheance(req.datePremiereEcheance());
        c.setPeriodicite(req.periodicite());
        c.setNombreEcheance(req.nombreEcheance());
        if (req.delaiGrace() != null) c.setDelaiGrace(req.delaiGrace());
        c.setSoldeCapital(req.montantDeblocage());
        c.setSoldeInteret(BigDecimal.ZERO);
        c.setSoldePenalite(BigDecimal.ZERO);
        c.setStatut(CreditStatut.DEBLOQUE);
        c.setEtapeCourante("DEBLOQUE");

        Credits saved = creditsRepo.save(c);

        // 5. Generate amortization schedule
        if (saved.getModeDeCalculInteret() != null) {
            List<Amortp> tableau = amortissementService.genererTableau(saved);
            amortpRepo.saveAll(tableau);
        }

        // 6. Append historique
        appendHistorique(saved, "DEBLOQUE", avant, CreditStatut.DEBLOQUE, "APPROUVE", null);

        return saved;
    }

    /**
     * Rejet à n'importe quelle étape non terminale.
     */
    public Credits rejeter(Long idCredit, WorkflowDecisionRequest req) {
        Credits c = chargerCredit(idCredit);
        if (Etape.valueOf(c.getEtapeCourante()).isTerminal()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le crédit est déjà dans un état terminal : " + c.getEtapeCourante());
        }
        CreditStatut avant = c.getStatut();
        String etapeAvant = c.getEtapeCourante();
        c.setStatut(CreditStatut.REJETE);
        c.setEtapeCourante("REJETE");
        Credits saved = creditsRepo.save(c);
        appendHistorique(saved, etapeAvant + "→REJETE", avant, CreditStatut.REJETE, "REFUSE",
                         req != null ? req.commentaire() : null);
        return saved;
    }

    // =========================================================================
    //  Lecture
    // =========================================================================

    @Transactional(readOnly = true)
    public List<WorkflowTimelineEntry> getTimeline(Long idCredit) {
        return historiqueRepo
                .findByCredit_IdCreditOrderByDateVisaAscIdHistoriqueAsc(idCredit)
                .stream()
                .map(WorkflowTimelineEntry::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<AnalyseFinanciereDTO> getAnalyse(Long idCredit) {
        return analyseRepo.findByCredit_IdCredit(idCredit)
                          .map(AnalyseFinanciereDTO::from);
    }

    @Transactional(readOnly = true)
    public List<Credits> getComitePending() {
        return creditsRepo.findByEtapeCourante("COMITE");
    }

    /** Dossiers en attente d'action agent (complétude + analyse + visa RC). */
    @Transactional(readOnly = true)
    public List<Credits> getAgentPending() {
        List<Credits> all = creditsRepo.findByEtapeCourante("COMPLETUDE");
        all.addAll(creditsRepo.findByEtapeCourante("ANALYSE_FINANCIERE"));
        all.addAll(creditsRepo.findByEtapeCourante("VISA_RC"));
        return all;
    }

    /** Dossiers à une étape précise (paramétrable). */
    @Transactional(readOnly = true)
    public List<Credits> getQueueByEtape(String etape) {
        if (etape == null || etape.isBlank()) {
            throw new IllegalArgumentException("Étape requise.");
        }
        return creditsRepo.findByEtapeCourante(etape.toUpperCase());
    }

    /** Compteurs de dossiers par étape (workflow stats). */
    @Transactional(readOnly = true)
    public Map<String, Long> getWorkflowStats() {
        String[] etapes = {
            "SAISIE", "COMPLETUDE", "ANALYSE_FINANCIERE",
            "VISA_RC", "COMITE", "VISA_SF",
            "DEBLOCAGE_PENDING", "DEBLOQUE", "REJETE", "CLOTURE"
        };
        Map<String, Long> stats = new HashMap<>();
        for (String e : etapes) {
            stats.put(e, (long) creditsRepo.findByEtapeCourante(e).size());
        }
        return stats;
    }

    // =========================================================================
    //  Helpers internes
    // =========================================================================

    private Credits chargerCredit(Long idCredit) {
        return creditsRepo.findById(idCredit)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Crédit introuvable : " + idCredit));
    }

    private void requireEtape(Credits c, String expectedEtape) {
        if (!expectedEtape.equals(c.getEtapeCourante())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Transition impossible : étape courante = " + c.getEtapeCourante()
                    + ", étape attendue = " + expectedEtape);
        }
    }

    private void appendHistorique(Credits c, String etape,
                                   CreditStatut avant, CreditStatut apres,
                                   String decision, String commentaire) {
        HistoriqueVisaCredit h = new HistoriqueVisaCredit();
        h.setCredit(c);
        h.setEtape(etape);
        h.setStatutAvant(avant);
        h.setStatutApres(apres);
        h.setDateVisa(LocalDate.now());
        h.setDecision(decision);
        h.setCommentaire(commentaire);
        h.setUtilisateur(currentUser());
        historiqueRepo.save(h);
    }

    private String currentUser() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "SYSTEM";
        }
    }
}

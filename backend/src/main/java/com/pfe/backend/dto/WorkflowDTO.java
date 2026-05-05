package com.pfe.backend.dto;

import com.microfina.entity.AnalyseFinanciere;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * WorkflowDTO – DTOs du workflow multi-niveaux de validation crédit (Phase 12).
 */
public final class WorkflowDTO {

    private WorkflowDTO() {}

    /**
     * Réponse complète d'une analyse financière.
     */
    public record AnalyseFinanciereDTO(
        Long          idAnalyse,
        Long          creditId,
        String        typeAnalyse,
        BigDecimal    revenusMensuels,
        BigDecimal    chargesMensuelles,
        BigDecimal    capaciteRemboursement,
        BigDecimal    ratioEndettement,
        BigDecimal    totalActif,
        BigDecimal    totalPassif,
        String        indicateursJson,
        String        commentaire,
        String        avisAgent,
        LocalDateTime dateAnalyse,
        String        utilisateur,
        Integer       version
    ) {
        public static AnalyseFinanciereDTO from(AnalyseFinanciere a) {
            return new AnalyseFinanciereDTO(
                a.getIdAnalyse(),
                a.getCredit() != null ? a.getCredit().getIdCredit() : null,
                a.getTypeAnalyse()  != null ? a.getTypeAnalyse().name()  : null,
                a.getRevenusMensuels(),
                a.getChargesMensuelles(),
                a.getCapaciteRemboursement(),
                a.getRatioEndettement(),
                a.getTotalActif(),
                a.getTotalPassif(),
                a.getIndicateursJson(),
                a.getCommentaire(),
                a.getAvisAgent() != null ? a.getAvisAgent().name() : null,
                a.getDateAnalyse(),
                a.getUtilisateur(),
                a.getVersion()
            );
        }
    }

    /**
     * Corps de la requête POST /analyse.
     */
    public record AnalyseFinanciereCreateRequest(
        @NotNull @Positive BigDecimal revenusMensuels,
        @NotNull @Positive BigDecimal chargesMensuelles,
        BigDecimal    totalActif,
        BigDecimal    totalPassif,
        String        indicateursJson,
        @Size(max = 500) String commentaire,
        String        avisAgent
    ) {}

    /**
     * Corps générique pour les décisions de workflow (approbation / rejet).
     */
    public record WorkflowDecisionRequest(
        @Size(max = 500) String commentaire
    ) {}

    /**
     * Entrée de la timeline historique.
     */
    public record WorkflowTimelineEntry(
        String      etape,
        String      statutAvant,
        String      statutApres,
        LocalDate   dateVisa,
        String      decision,
        String      commentaire,
        String      utilisateur
    ) {
        public static WorkflowTimelineEntry from(com.microfina.entity.HistoriqueVisaCredit h) {
            return new WorkflowTimelineEntry(
                h.getEtape(),
                h.getStatutAvant()  != null ? h.getStatutAvant().name()  : null,
                h.getStatutApres()  != null ? h.getStatutApres().name()  : null,
                h.getDateVisa(),
                h.getDecision(),
                h.getCommentaire(),
                h.getUtilisateur()
            );
        }
    }

    /**
     * Corps de la requête POST /debloquer.
     */
    public record DeblocageRequest(
        @NotNull @Positive BigDecimal montantDeblocage,
        @NotNull LocalDate            datePremiereEcheance,
        @NotNull String               periodicite,
        @NotNull @Positive Integer    nombreEcheance,
        Integer                       delaiGrace,

        /**
         * Canal de déblocage : CAISSE | BANQUE | WALLET (optionnel selon intégration).
         */
        @NotNull String               canal,

        /**
         * Si canal = BANQUE : identifiant du CompteBanque source.
         */
        Long                          compteBanqueId,

        /**
         * Si canal = CAISSE : numéro du CompteEps utilisé comme caisse/compte source.
         */
        String                        numCompteCaisse
    ) {}
}

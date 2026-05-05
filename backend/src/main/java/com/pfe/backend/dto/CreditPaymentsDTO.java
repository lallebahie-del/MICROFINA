package com.pfe.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * CreditPaymentsDTO — DTO de suivi des paiements par échéance (Amortp) + agrégats.
 */
public class CreditPaymentsDTO {

    public record EcheanceDTO(
            Long idAmortp,
            Integer numEcheance,
            LocalDate dateEcheance,
            LocalDate dateReglement,
            String statutEcheance,

            BigDecimal capitalDu,
            BigDecimal margeOuInteretDu,
            BigDecimal penaliteDue,
            BigDecimal assuranceDue,
            BigDecimal commissionDue,
            BigDecimal taxeDue,
            BigDecimal totalDu,

            BigDecimal capitalPaye,
            BigDecimal margeOuInteretPaye,
            BigDecimal penalitePayee,
            BigDecimal assurancePayee,
            BigDecimal commissionPayee,
            BigDecimal taxePayee,
            BigDecimal totalPaye,

            BigDecimal totalRestant,
            Integer joursRetard
    ) {}

    public record AgingBucketDTO(
            BigDecimal current,
            BigDecimal d1_30,
            BigDecimal d31_60,
            BigDecimal d61_90,
            BigDecimal d90_plus
    ) {}

    public record SummaryDTO(
            BigDecimal totalDu,
            BigDecimal totalPaye,
            BigDecimal totalRestant,
            long nbEcheances,
            long nbEnRetard,
            AgingBucketDTO balanceAgee
    ) {}

    public record Response(
            Long idCredit,
            LocalDate dateCalcul,
            SummaryDTO summary,
            List<EcheanceDTO> echeances
    ) {}

    /** Lignes d'un échéancier calculé à la volée (post-comité / avant déblocage). */
    public record PreviewEcheanceDTO(
            int numEcheance,
            LocalDate dateEcheance,
            BigDecimal capital,
            BigDecimal margeOuInteret,
            BigDecimal assurance,
            BigDecimal commission,
            BigDecimal taxe,
            BigDecimal totalEcheance,
            BigDecimal soldeCapitalApres
    ) {}

    public record PreviewResponse(
            Long idCredit,
            BigDecimal montantPrincipalUtilise,
            boolean islamique,
            LocalDate dateCalcul,
            List<PreviewEcheanceDTO> echeances
    ) {}
}


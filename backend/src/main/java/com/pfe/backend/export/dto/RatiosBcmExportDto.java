package com.pfe.backend.export.dto;

import java.math.BigDecimal;

/**
 * DTO de projection pour la vue {@code vue_ratios_bcm}.
 *
 * <p>Une ligne par agence active — ratios prudentiels BCM.</p>
 */
public record RatiosBcmExportDto(

        String     codeAgence,
        String     nomAgence,

        // ── Encours ──────────────────────────────────────────────────────
        BigDecimal encoursBrut,
        BigDecimal encoursNet,
        Integer    nbCreditsActifs,

        // ── PAR ───────────────────────────────────────────────────────────
        BigDecimal capitalRisquePar30,
        Double     tauxPar30,
        BigDecimal capitalRisquePar90,
        Double     tauxPar90,
        BigDecimal totalArrieres,
        Double     tauxPortefeuilleRisque,

        // ── Remboursement ─────────────────────────────────────────────────
        BigDecimal totalRembourse,
        BigDecimal totalEchu,
        Double     tauxRemboursement,

        // ── Couverture garanties ──────────────────────────────────────────
        BigDecimal totalGaranties,
        Double     ratioCouvertureGaranties
) {}

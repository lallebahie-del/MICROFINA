package com.pfe.backend.export.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de projection pour la vue {@code vue_liste_clients}.
 *
 * <p>Une ligne par membre — agrège épargne, crédits, retards et garanties.</p>
 */
public record ListeClientExportDto(

        // ── Identité ─────────────────────────────────────────────────────
        String    numMembre,
        String    nom,
        String    prenom,
        String    sexe,
        LocalDate dateNaissance,
        String    statutMembre,
        LocalDate dateAdhesion,

        // ── Agence ───────────────────────────────────────────────────────
        String    codeAgence,
        String    nomAgence,

        // ── Épargne ──────────────────────────────────────────────────────
        Integer   nbComptesEpargne,
        BigDecimal totalEpargne,
        BigDecimal totalEpargneBloquee,

        // ── Crédits ──────────────────────────────────────────────────────
        Integer   nbCreditsTotal,
        Integer   nbCreditsActifs,
        BigDecimal montantTotalAccorde,
        BigDecimal encoursCapital,

        // ── Retards / PAR ────────────────────────────────────────────────
        Integer   nbCreditsRetard,
        String    categoriePar,
        Integer   maxJoursRetard,
        BigDecimal totalArrieres,

        // ── Garanties ─────────────────────────────────────────────────────
        Integer   nbGaranties,
        BigDecimal totalGaranties
) {}

package com.pfe.backend.export.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de projection pour la vue {@code vue_etat_credits}.
 *
 * <p>Utilisé par {@link com.pfe.backend.export.ExportService} pour alimenter
 * les exports Excel et PDF du portefeuille crédit.</p>
 *
 * <p>Toutes les colonnes numériques sont {@code BigDecimal} pour conserver
 * la précision monétaire (DECIMAL 19,4 côté SQL Server).</p>
 */
public record EtatCreditExportDto(

        // ── Identification ────────────────────────────────────────────────
        Long    idCredit,
        String  numCredit,
        String  statutCredit,
        String  objetCredit,

        // ── Membre emprunteur ────────────────────────────────────────────
        String  numMembre,
        String  nomMembre,
        String  prenomMembre,
        String  sexe,

        // ── Agence ───────────────────────────────────────────────────────
        String  codeAgence,
        String  nomAgence,
        String  nomAgent,

        // ── Dates ────────────────────────────────────────────────────────
        LocalDate dateDemande,
        LocalDate dateDeblocage,
        LocalDate dateEcheanceFinale,

        // ── Montants ─────────────────────────────────────────────────────
        BigDecimal montantAccorde,
        BigDecimal montantDebloque,
        BigDecimal soldeCapital,
        BigDecimal soldeTotal,

        // ── PAR ───────────────────────────────────────────────────────────
        Integer   joursRetard,
        BigDecimal totalArrieres,
        String    categoriePar,

        // ── Garanties ─────────────────────────────────────────────────────
        BigDecimal totalGaranties,
        Double     tauxCouverturePct
) {}

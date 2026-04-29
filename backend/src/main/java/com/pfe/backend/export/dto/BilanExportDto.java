package com.pfe.backend.export.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de projection pour la vue {@code vue_bilan}.
 *
 * <p>Une ligne par numéro de compte × agence — bilan simplifié SYSCOHADA.</p>
 */
public record BilanExportDto(

        String     classeCompte,
        String     numCompte,
        String     rubrique,
        String     libelleRubrique,
        String     codeAgence,
        String     nomAgence,
        Long       nbEcritures,
        BigDecimal totalDebit,
        BigDecimal totalCredit,
        BigDecimal soldeNet,
        BigDecimal montantActif,
        BigDecimal montantPassif,
        LocalDate  datePremièreEcriture,
        LocalDate  dateDerniereEcriture
) {}

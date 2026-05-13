package com.pfe.backend.dto;

import com.microfina.entity.Credits;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * WorkflowCreditSummaryDTO — projection enrichie pour les dashboards Agent et Comité.
 *
 * <p>Contient les champs nécessaires pour décider sans ouvrir le détail :
 * identité du membre, montants, agence, date, jours dans l'étape.</p>
 */
public record WorkflowCreditSummaryDTO(
        Long       idCredit,
        String     numCredit,
        String     statut,
        String     etapeCourante,
        // Membre
        String     numMembre,
        String     nomMembre,
        String     prenomMembre,
        // Montants
        BigDecimal montantDemande,
        BigDecimal montantAccorde,
        // Agence
        String     codeAgence,
        String     nomAgence,
        // Agent qui a créé / suit le dossier
        String     codeAgent,
        // Dates
        LocalDate  dateDemande,
        Long       joursDansEtape
) {
    public static WorkflowCreditSummaryDTO from(Credits c) {
        return new WorkflowCreditSummaryDTO(
                c.getIdCredit(),
                c.getNumCredit() != null ? c.getNumCredit() : "",
                c.getStatut() != null ? c.getStatut().name() : null,
                c.getEtapeCourante(),
                c.getMembre()  != null ? c.getMembre().getNumMembre() : null,
                c.getMembre()  != null ? c.getMembre().getNom()       : null,
                c.getMembre()  != null ? c.getMembre().getPrenom()    : null,
                c.getMontantDemande(),
                c.getMontantAccorde(),
                c.getAgence()  != null ? c.getAgence().getCodeAgence() : null,
                c.getAgence()  != null ? c.getAgence().getNomAgence()  : null,
                c.getAgentCredit() != null ? c.getAgentCredit().getCodeAgent() : null,
                c.getDateDemande(),
                c.getDateDemande() != null
                    ? ChronoUnit.DAYS.between(c.getDateDemande(), LocalDate.now())
                    : null
        );
    }
}

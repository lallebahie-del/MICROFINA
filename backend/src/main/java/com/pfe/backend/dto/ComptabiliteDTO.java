package com.pfe.backend.dto;

import com.microfina.entity.Comptabilite;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ComptabiliteDTO {

    private ComptabiliteDTO() { }

    public record Response(
            Long       idComptabilite,
            String     codeAgence,
            LocalDate  dateEcriture,
            String     numPiece,
            String     numCompte,
            String     libelle,
            BigDecimal debit,
            BigDecimal credit,
            String     codeLettrage,
            String     etat
    ) {
        public static Response from(Comptabilite c) {
            String agenceCode = c.getAgence() != null
                    ? c.getAgence().getCodeAgence()
                    : c.getCodeAgence();

            return new Response(
                    c.getIdComptabilite(),
                    agenceCode,
                    c.getDateOperation(),
                    c.getNumPiece(),
                    c.getCompteAuxi(),
                    c.getLibelle(),
                    c.getDebit(),
                    c.getCredit(),
                    c.getLettre(),
                    c.getEtat()
            );
        }
    }
}

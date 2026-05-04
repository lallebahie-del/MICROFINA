package com.microfina.entity;

/**
 * Etape – étapes du workflow multi-niveaux de validation crédit (Phase 12).
 *
 * Valeurs valides pour Credits.etapeCourante (colonne NVARCHAR(30)).
 */
public enum Etape {
    SAISIE,
    COMPLETUDE,
    ANALYSE_FINANCIERE,
    VISA_RC,
    COMITE,
    VISA_SF,
    DEBLOCAGE_PENDING,
    DEBLOQUE,
    CLOTURE,
    REJETE;

    public boolean isTerminal() {
        return this == DEBLOQUE || this == CLOTURE || this == REJETE;
    }

    /**
     * Étape suivante en cas d'approbation (approve=true) ou de rejet (approve=false).
     * Retourne null pour les étapes terminales.
     */
    public static Etape next(Etape current, boolean approve) {
        if (!approve) return REJETE;
        return switch (current) {
            case SAISIE              -> COMPLETUDE;
            case COMPLETUDE          -> ANALYSE_FINANCIERE;
            case ANALYSE_FINANCIERE  -> VISA_RC;
            case VISA_RC             -> COMITE;
            case COMITE              -> VISA_SF;
            case VISA_SF             -> DEBLOCAGE_PENDING;
            case DEBLOCAGE_PENDING   -> DEBLOQUE;
            case DEBLOQUE            -> CLOTURE;
            default                  -> null;
        };
    }
}

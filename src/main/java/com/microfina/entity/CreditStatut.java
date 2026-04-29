package com.microfina.entity;

/**
 * CreditStatut – états du cycle de vie d'un crédit (French naming convention).
 *
 * Diagramme de transition :
 *
 *   BROUILLON ──► SOUMIS ──► VALIDE_AGENT ──► VALIDE_COMITE ──► DEBLOQUE ──► SOLDE
 *                  │               │                 │
 *                  └──► REJETE     └──► REJETE        └──► REJETE
 *
 * BROUILLON    – dossier en cours de saisie par l'agent de crédit.
 * SOUMIS       – dossier soumis pour analyse / validation de premier niveau.
 * VALIDE_AGENT – validé par l'agent ou le superviseur de crédit.
 * VALIDE_COMITE– validé par le comité de crédit (décision finale avant déblocage).
 * DEBLOQUE     – fonds décaissés au membre ; DATE_DEBLOCAGE renseignée ;
 *                tableau d'amortissement généré ; écriture Comptabilité postée.
 * SOLDE        – crédit entièrement remboursé ou passé en perte.
 * REJETE       – refusé à n'importe quelle étape de la chaîne de validation.
 */
public enum CreditStatut {

    BROUILLON,
    SOUMIS,
    VALIDE_AGENT,
    VALIDE_COMITE,
    DEBLOQUE,
    SOLDE,
    REJETE;

    // ── Business-rule helpers ─────────────────────────────────────

    /**
     * Returns {@code true} if the loan officer may still edit the dossier.
     * Only BROUILLON is editable.
     */
    public boolean isEditable() {
        return this == BROUILLON;
    }

    /**
     * Returns {@code true} if the credit has been disbursed and an
     * amortization schedule must exist.
     */
    public boolean isActif() {
        return this == DEBLOQUE;
    }

    /**
     * Returns {@code true} if repayment (Reglement) entries may be posted
     * against this credit.
     */
    public boolean accepteRemboursement() {
        return this == DEBLOQUE;
    }

    /**
     * Returns {@code true} if the credit is in a terminal state
     * (no further transitions are possible).
     */
    public boolean isTerminal() {
        return this == SOLDE || this == REJETE;
    }

    /**
     * Returns {@code true} if the credit has passed at least the first
     * validation step (i.e. agent + committee have both approved).
     * Used by disbursement guards.
     */
    public boolean isEntierementValide() {
        return this == VALIDE_COMITE || this == DEBLOQUE || this == SOLDE;
    }
}

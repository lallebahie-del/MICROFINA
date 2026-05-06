package com.microfina.entity;

/**
 * ModeCalculInteretConstant – technical codes for interest calculation modes.
 *
 * These constants match the MODECALCUL column values in the
 * {@code mode_de_calcul_interet} reference table (Phase 1).
 *
 * Usage:
 * <pre>
 *   if (ModeCalculInteretConstant.DEGRESSIF.equals(produit.getModeCalcul())) {
 *       // use declining-balance schedule
 *   }
 * </pre>
 */
public final class ModeCalculInteretConstant {

    private ModeCalculInteretConstant() {
        // utility class – no instantiation
    }

    // ── Interest calculation modes ────────────────────────────────

    /**
     * Simple interest: I = P × r × t
     * Applied on the original principal for each period.
     */
    public static final String SIMPLE = "SIMPLE";

    /**
     * Compound interest: A = P × (1 + r)^t
     * Interest is capitalised and earns interest in subsequent periods.
     */
    public static final String COMPOSE = "COMPOSE";

    /**
     * Declining-balance (dégressif): interest is applied on the outstanding
     * (reducing) principal balance after each repayment.
     * Most common method for amortising microfinance loans.
     */
    public static final String DEGRESSIF = "DEGRESSIF";

    // NOTE: FIXE mode was removed — it is mathematically identical to SIMPLE
    // (fixed interest on original principal, equal capital instalments).
    // Use SIMPLE for all flat-rate credit products.

    // ── Repayment periodicity codes ───────────────────────────────

    public static final String PERIODICITE_MENSUEL      = "M";
    public static final String PERIODICITE_BIMENSUEL    = "BM";
    public static final String PERIODICITE_TRIMESTRIEL  = "T";
    public static final String PERIODICITE_SEMESTRIEL   = "S";
    public static final String PERIODICITE_ANNUEL       = "A";
    public static final String PERIODICITE_HEBDOMADAIRE = "W";
    public static final String PERIODICITE_QUINZAINE    = "Q";

    // ── Grace period types ────────────────────────────────────────

    /** Grace on principal only – interest continues to accrue. */
    public static final String GRACE_PRINCIPAL    = "P";

    /** Grace on both principal and interest (full moratorium). */
    public static final String GRACE_TOTAL        = "T";

    // ── Credit types ──────────────────────────────────────────────

    public static final String TYPE_CREDIT_INDIVIDUEL = "INDIVIDUEL";
    public static final String TYPE_CREDIT_GROUPE     = "GROUPE";
    public static final String TYPE_CREDIT_SOLIDAIRE  = "SOLIDAIRE";

    // ── Commission collection modes ───────────────────────────────

    /** Commission deducted at disbursement. */
    public static final String PRELEVEMENT_DEBUT    = "DEBUT";

    /** Commission collected at each repayment instalment. */
    public static final String PRELEVEMENT_ECHEANCE = "ECHEANCE";

    /** Commission collected at final repayment. */
    public static final String PRELEVEMENT_FIN      = "FIN";
}

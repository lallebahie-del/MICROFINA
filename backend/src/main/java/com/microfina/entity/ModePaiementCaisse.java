package com.microfina.entity;

/**
 * Mode de paiement accepté aux guichets caisse.
 *
 * <ul>
 *   <li>{@link #ESPECES}      – paiement en espèces (numéraire)</li>
 *   <li>{@link #CHEQUE}       – paiement par chèque</li>
 *   <li>{@link #VIREMENT}     – paiement par virement bancaire</li>
 *   <li>{@link #MOBILE_MONEY} – paiement par mobile money (Orange Money, Wave, etc.)</li>
 * </ul>
 *
 * DDL source of truth: P7-001-CREATE-TABLE-OperationCaisse.xml.
 * Spec: cahier §3.1.1.
 */
public enum ModePaiementCaisse {
    ESPECES,
    CHEQUE,
    VIREMENT,
    MOBILE_MONEY
}

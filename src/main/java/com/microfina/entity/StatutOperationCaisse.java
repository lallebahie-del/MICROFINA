package com.microfina.entity;

/**
 * Statut du cycle de vie d'une {@link OperationCaisse}.
 *
 * <ul>
 *   <li>{@link #EN_ATTENTE} – opération créée, en attente de validation</li>
 *   <li>{@link #VALIDE}     – opération validée et comptabilisée</li>
 *   <li>{@link #ANNULE}     – opération annulée (contrepassée)</li>
 * </ul>
 *
 * DDL source of truth: P7-001-CREATE-TABLE-OperationCaisse.xml.
 * Spec: cahier §3.1.1 (Opérations de caisse).
 */
public enum StatutOperationCaisse {
    VALIDE,
    ANNULE,
    EN_ATTENTE
}

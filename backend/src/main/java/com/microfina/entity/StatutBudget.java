package com.microfina.entity;

/**
 * Cycle de vie d'un Budget.
 * <ul>
 *   <li>{@code BROUILLON} – budget en cours de saisie, non encore validé.</li>
 *   <li>{@code VALIDE}    – budget approuvé et en vigueur.</li>
 *   <li>{@code CLOTURE}   – budget clôturé en fin d'exercice fiscal.</li>
 * </ul>
 *
 * DDL source of truth: P8-001-CREATE-TABLE-Budget.xml.
 * Spec: cahier §3.1.1 (Budget).
 */
public enum StatutBudget {
    BROUILLON,
    VALIDE,
    CLOTURE
}

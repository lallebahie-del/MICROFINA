package com.microfina.entity;

/**
 * StatutCheque – cycle de vie d'un chèque individuel.
 *
 * DDL source of truth: P6-002-CREATE-TABLE-Cheque.xml.
 * Spec: cahier §6 (Module Banque – gestion des chèques).
 */
public enum StatutCheque {
    EMIS,
    ENCAISSE,
    REJETE,
    OPPOSE
}

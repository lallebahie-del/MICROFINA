package com.microfina.entity;

/**
 * TypeVirement – nature du virement bancaire.
 *
 * DDL source of truth: P6-004-CREATE-TABLE-Virement.xml.
 * Spec: cahier §6 (Module Banque – virements).
 */
public enum TypeVirement {
    INTRA_BANQUE,
    INTER_BANQUE
}

package com.microfina.entity;

/**
 * StatutOperationBanque – état d'une opération bancaire.
 *
 * DDL source of truth: P6-003-CREATE-TABLE-OperationBanque.xml.
 * Spec: cahier §6 (Module Banque – opérations bancaires).
 */
public enum StatutOperationBanque {
    EN_ATTENTE,
    VALIDE,
    ANNULE
}

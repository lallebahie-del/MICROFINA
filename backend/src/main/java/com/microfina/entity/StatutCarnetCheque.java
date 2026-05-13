package com.microfina.entity;

/**
 * StatutCarnetCheque – cycle de vie d'un carnet de chèques.
 *
 * DDL source of truth: P6-001-CREATE-TABLE-CarnetCheque.xml.
 * Spec: cahier §6 (Module Banque – gestion des carnets de chèques).
 */
public enum StatutCarnetCheque {
    DEMANDE,
    IMPRIME,
    REMIS,
    EPUISE,
    PERDU,
    OPPOSITION
}

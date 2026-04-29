package com.microfina.entity;

/**
 * Nature d'une ligne budgétaire.
 * <ul>
 *   <li>{@code RECETTE} – ligne de recette prévisionnelle ou réalisée.</li>
 *   <li>{@code DEPENSE} – ligne de dépense prévisionnelle ou réalisée.</li>
 * </ul>
 *
 * DDL source of truth: P8-002-CREATE-TABLE-LigneBudget.xml.
 * Spec: cahier §3.1.1.
 */
public enum TypeLigneBudget {
    RECETTE,
    DEPENSE
}

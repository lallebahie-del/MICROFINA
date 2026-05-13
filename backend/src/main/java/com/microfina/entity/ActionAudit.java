package com.microfina.entity;

/**
 * ActionAudit – énumération des actions traçables dans le journal d'audit.
 *
 * <p>Chaque valeur correspond à une opération métier ou de sécurité
 * enregistrée dans {@link JournalAudit}.</p>
 *
 * <ul>
 *   <li>{@link #CREATE}  – création d'une entité</li>
 *   <li>{@link #UPDATE}  – modification d'une entité</li>
 *   <li>{@link #DELETE}  – suppression d'une entité</li>
 *   <li>{@link #LOGIN}   – connexion d'un utilisateur</li>
 *   <li>{@link #LOGOUT}  – déconnexion d'un utilisateur</li>
 * </ul>
 */
public enum ActionAudit {

    /** Création d'une nouvelle entité dans le système. */
    CREATE,

    /** Modification d'une entité existante. */
    UPDATE,

    /** Suppression d'une entité. */
    DELETE,

    /** Connexion d'un utilisateur au système. */
    LOGIN,

    /** Déconnexion d'un utilisateur du système. */
    LOGOUT
}

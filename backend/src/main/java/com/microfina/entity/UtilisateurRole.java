package com.microfina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * UtilisateurRole – table de jointure entre {@link Utilisateur} et {@link Role}.
 *
 * <p>Chaque ligne associe un utilisateur à un rôle. La clé composite
 * {@link UtilisateurRoleId} garantit l'unicité de chaque association.
 * Cette table de jointure est pure : elle ne contient pas de colonne
 * métier ni de versionnement.</p>
 *
 * <p>Table cible : {@code UtilisateurRole}</p>
 */
@Entity
@Table(name = "UtilisateurRole")
@DynamicUpdate
public class UtilisateurRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Clé primaire composite (id_utilisateur + id_role). */
    @EmbeddedId
    private UtilisateurRoleId id;

    /**
     * Utilisateur concerné par cette association.
     * {@code @MapsId} lie la partie {@code idUtilisateur} de la clé composite.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUtilisateur")
    @JoinColumn(name = "id_utilisateur",
                foreignKey = @ForeignKey(name = "FK_UtilisateurRole_Utilisateur"))
    private Utilisateur utilisateur;

    /**
     * Rôle concerné par cette association.
     * {@code @MapsId} lie la partie {@code idRole} de la clé composite.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idRole")
    @JoinColumn(name = "id_role",
                foreignKey = @ForeignKey(name = "FK_UtilisateurRole_Role"))
    private Role role;

    // ── Constructeurs ──────────────────────────────────────────────────────────

    /** Constructeur sans arguments requis par JPA. */
    public UtilisateurRole() {
    }

    /**
     * Constructeur complet.
     *
     * @param id          clé composite
     * @param utilisateur utilisateur concerné
     * @param role        rôle concerné
     */
    public UtilisateurRole(UtilisateurRoleId id, Utilisateur utilisateur, Role role) {
        this.id = id;
        this.utilisateur = utilisateur;
        this.role = role;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    /** @return clé primaire composite */
    public UtilisateurRoleId getId() {
        return id;
    }

    /** @param id clé primaire composite */
    public void setId(UtilisateurRoleId id) {
        this.id = id;
    }

    /** @return utilisateur de cette association */
    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    /** @param utilisateur utilisateur de cette association */
    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    /** @return rôle de cette association */
    public Role getRole() {
        return role;
    }

    /** @param role rôle de cette association */
    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "UtilisateurRole("
            + "id=" + id
            + ")";
    }
}

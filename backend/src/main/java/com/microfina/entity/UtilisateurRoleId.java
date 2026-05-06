package com.microfina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * UtilisateurRoleId – clé primaire composite de la table {@code UtilisateurRole}.
 *
 * <p>Combine l'identifiant de l'utilisateur ({@code id_utilisateur}) et
 * l'identifiant du rôle ({@code id_role}) pour former une clé composite
 * qui garantit l'unicité de chaque association utilisateur-rôle.</p>
 */
@Embeddable
public class UtilisateurRoleId implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Référence à l'identifiant de l'utilisateur. */
    @Column(name = "id_utilisateur")
    private Long idUtilisateur;

    /** Référence à l'identifiant du rôle. */
    @Column(name = "id_role")
    private Long idRole;

    // ── Constructeurs ──────────────────────────────────────────────────────────

    /** Constructeur sans arguments requis par JPA. */
    public UtilisateurRoleId() {
    }

    /**
     * Constructeur complet.
     *
     * @param idUtilisateur identifiant de l'utilisateur
     * @param idRole        identifiant du rôle
     */
    public UtilisateurRoleId(Long idUtilisateur, Long idRole) {
        this.idUtilisateur = idUtilisateur;
        this.idRole = idRole;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    /** @return identifiant de l'utilisateur */
    public Long getIdUtilisateur() {
        return idUtilisateur;
    }

    /** @param idUtilisateur identifiant de l'utilisateur */
    public void setIdUtilisateur(Long idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    /** @return identifiant du rôle */
    public Long getIdRole() {
        return idRole;
    }

    /** @param idRole identifiant du rôle */
    public void setIdRole(Long idRole) {
        this.idRole = idRole;
    }

    // ── equals / hashCode ──────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UtilisateurRoleId)) return false;
        UtilisateurRoleId other = (UtilisateurRoleId) o;
        return Objects.equals(this.idUtilisateur, other.idUtilisateur)
            && Objects.equals(this.idRole, other.idRole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.idUtilisateur, this.idRole);
    }
}

package com.microfina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * RolePrivilegeId – clé primaire composite de la table {@code RolePrivilege}.
 *
 * <p>Combine l'identifiant du rôle ({@code id_role}) et l'identifiant
 * du privilège ({@code id_privilege}) pour former une clé composite
 * garantissant l'unicité de chaque association rôle-privilège.</p>
 */
@Embeddable
public class RolePrivilegeId implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Référence à l'identifiant du rôle. */
    @Column(name = "id_role")
    private Long idRole;

    /** Référence à l'identifiant du privilège. */
    @Column(name = "id_privilege")
    private Long idPrivilege;

    // ── Constructeurs ──────────────────────────────────────────────────────────

    /** Constructeur sans arguments requis par JPA. */
    public RolePrivilegeId() {
    }

    /**
     * Constructeur complet.
     *
     * @param idRole      identifiant du rôle
     * @param idPrivilege identifiant du privilège
     */
    public RolePrivilegeId(Long idRole, Long idPrivilege) {
        this.idRole = idRole;
        this.idPrivilege = idPrivilege;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    /** @return identifiant du rôle */
    public Long getIdRole() {
        return idRole;
    }

    /** @param idRole identifiant du rôle */
    public void setIdRole(Long idRole) {
        this.idRole = idRole;
    }

    /** @return identifiant du privilège */
    public Long getIdPrivilege() {
        return idPrivilege;
    }

    /** @param idPrivilege identifiant du privilège */
    public void setIdPrivilege(Long idPrivilege) {
        this.idPrivilege = idPrivilege;
    }

    // ── equals / hashCode ──────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolePrivilegeId)) return false;
        RolePrivilegeId other = (RolePrivilegeId) o;
        return Objects.equals(this.idRole, other.idRole)
            && Objects.equals(this.idPrivilege, other.idPrivilege);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.idRole, this.idPrivilege);
    }
}

package com.microfina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * RolePrivilege – table de jointure entre {@link Role} et {@link Privilege}.
 *
 * <p>Chaque ligne associe un rôle à un privilège. La clé composite
 * {@link RolePrivilegeId} garantit l'unicité de chaque association.
 * Cette table de jointure est pure : elle ne contient pas de colonne
 * métier ni de versionnement.</p>
 *
 * <p>Table cible : {@code RolePrivilege}</p>
 */
@Entity
@Table(name = "RolePrivilege")
@DynamicUpdate
public class RolePrivilege implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Clé primaire composite (id_role + id_privilege). */
    @EmbeddedId
    private RolePrivilegeId id;

    /**
     * Rôle concerné par cette association.
     * {@code @MapsId} lie la partie {@code idRole} de la clé composite.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idRole")
    @JoinColumn(name = "id_role",
                foreignKey = @ForeignKey(name = "FK_RolePrivilege_Role"))
    private Role role;

    /**
     * Privilège concerné par cette association.
     * {@code @MapsId} lie la partie {@code idPrivilege} de la clé composite.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idPrivilege")
    @JoinColumn(name = "id_privilege",
                foreignKey = @ForeignKey(name = "FK_RolePrivilege_Privilege"))
    private Privilege privilege;

    // ── Constructeurs ──────────────────────────────────────────────────────────

    /** Constructeur sans arguments requis par JPA. */
    public RolePrivilege() {
    }

    /**
     * Constructeur complet.
     *
     * @param id        clé composite
     * @param role      rôle concerné
     * @param privilege privilège concerné
     */
    public RolePrivilege(RolePrivilegeId id, Role role, Privilege privilege) {
        this.id = id;
        this.role = role;
        this.privilege = privilege;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    /** @return clé primaire composite */
    public RolePrivilegeId getId() {
        return id;
    }

    /** @param id clé primaire composite */
    public void setId(RolePrivilegeId id) {
        this.id = id;
    }

    /** @return rôle de cette association */
    public Role getRole() {
        return role;
    }

    /** @param role rôle de cette association */
    public void setRole(Role role) {
        this.role = role;
    }

    /** @return privilège de cette association */
    public Privilege getPrivilege() {
        return privilege;
    }

    /** @param privilege privilège de cette association */
    public void setPrivilege(Privilege privilege) {
        this.privilege = privilege;
    }

    @Override
    public String toString() {
        return "RolePrivilege("
            + "id=" + id
            + ")";
    }
}

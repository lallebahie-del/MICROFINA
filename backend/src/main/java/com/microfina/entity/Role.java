package com.microfina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * Role – rôle fonctionnel assignable à un utilisateur du système.
 *
 * <p>Un rôle regroupe un ensemble de privilèges (voir {@link Privilege})
 * via la table de jointure {@link RolePrivilege}. Les utilisateurs se voient
 * attribuer des rôles via {@link UtilisateurRole}.</p>
 *
 * <p>Table cible : {@code Role}</p>
 */
@Entity
@Table(name = "Role")
@DynamicUpdate
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identifiant technique généré automatiquement. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** Code unique identifiant le rôle (ex : "ADMIN", "AGENT_CREDIT"). */
    @Column(name = "code_role", length = 50, unique = true)
    private String codeRole;

    /** Libellé lisible du rôle. */
    @Column(name = "libelle", length = 100)
    private String libelle;

    /** Description détaillée du rôle et de ses responsabilités. */
    @Column(name = "description", length = 500)
    private String description;

    /** Colonne de verrouillage optimiste. */
    @Version
    @Column(name = "version")
    private Integer version;

    // ── Constructeurs ──────────────────────────────────────────────────────────

    /** Constructeur sans arguments requis par JPA. */
    public Role() {
    }

    /**
     * Constructeur complet.
     *
     * @param id          identifiant technique
     * @param codeRole    code unique du rôle
     * @param libelle     libellé court
     * @param description description longue
     * @param version     version pour le verrouillage optimiste
     */
    public Role(Long id, String codeRole, String libelle, String description, Integer version) {
        this.id = id;
        this.codeRole = codeRole;
        this.libelle = libelle;
        this.description = description;
        this.version = version;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    /** @return identifiant technique */
    public Long getId() {
        return id;
    }

    /** @param id identifiant technique */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return code unique du rôle */
    public String getCodeRole() {
        return codeRole;
    }

    /** @param codeRole code unique du rôle */
    public void setCodeRole(String codeRole) {
        this.codeRole = codeRole;
    }

    /** @return libellé du rôle */
    public String getLibelle() {
        return libelle;
    }

    /** @param libelle libellé du rôle */
    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    /** @return description détaillée */
    public String getDescription() {
        return description;
    }

    /** @param description description détaillée */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return version pour le verrouillage optimiste */
    public Integer getVersion() {
        return version;
    }

    /** @param version version pour le verrouillage optimiste */
    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Role("
            + "id=" + id
            + ", codeRole=" + codeRole
            + ")";
    }
}

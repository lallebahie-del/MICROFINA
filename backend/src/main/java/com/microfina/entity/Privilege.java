package com.microfina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * Privilege – droit fonctionnel granulaire accordé via un rôle.
 *
 * <p>Un privilège représente une action précise dans le système
 * (ex : "CREATE_CREDIT", "VIEW_REPORTS"). Il est rattaché à un module
 * fonctionnel et regroupé dans des rôles via {@link RolePrivilege}.</p>
 *
 * <p>Les authorities Spring Security sont constituées des
 * {@code codePrivilege} sans préfixe {@code ROLE_}.</p>
 *
 * <p>Table cible : {@code Privilege}</p>
 */
@Entity
@Table(name = "Privilege")
@DynamicUpdate
public class Privilege implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identifiant technique généré automatiquement. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** Code unique du privilège (ex : "CREATE_CREDIT", "VALIDATE_CREDIT"). */
    @Column(name = "code_privilege", length = 100, unique = true)
    private String codePrivilege;

    /** Libellé descriptif du privilège. */
    @Column(name = "libelle", length = 200)
    private String libelle;

    /** Module fonctionnel auquel appartient ce privilège (ex : "CREDIT", "EPARGNE"). */
    @Column(name = "module", length = 50)
    private String module;

    /** Colonne de verrouillage optimiste. */
    @Version
    @Column(name = "version")
    private Integer version;

    // ── Constructeurs ──────────────────────────────────────────────────────────

    /** Constructeur sans arguments requis par JPA. */
    public Privilege() {
    }

    /**
     * Constructeur complet.
     *
     * @param id            identifiant technique
     * @param codePrivilege code unique du privilège
     * @param libelle       libellé descriptif
     * @param module        module fonctionnel
     * @param version       version pour le verrouillage optimiste
     */
    public Privilege(Long id, String codePrivilege, String libelle, String module, Integer version) {
        this.id = id;
        this.codePrivilege = codePrivilege;
        this.libelle = libelle;
        this.module = module;
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

    /** @return code unique du privilège */
    public String getCodePrivilege() {
        return codePrivilege;
    }

    /** @param codePrivilege code unique du privilège */
    public void setCodePrivilege(String codePrivilege) {
        this.codePrivilege = codePrivilege;
    }

    /** @return libellé descriptif */
    public String getLibelle() {
        return libelle;
    }

    /** @param libelle libellé descriptif */
    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    /** @return module fonctionnel */
    public String getModule() {
        return module;
    }

    /** @param module module fonctionnel */
    public void setModule(String module) {
        this.module = module;
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
        return "Privilege("
            + "id=" + id
            + ", codePrivilege=" + codePrivilege
            + ")";
    }
}

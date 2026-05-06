package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * CompteurMembre – sequential counter used to auto-generate member numbers.
 * One row per (institution, agence, categorie) combination, enabling
 * independent number sequences per branch and member category.
 *
 * Spec: DB-FINA-202112-001 – referenced in Parametre table (p.49).
 * DDL source of truth: P1-009-CREATE-TABLE-CompteurMembre.xml.
 *
 * Naming fix: @JoinColumn names now match DDL exactly:
 *   fk_idInstitution, fk_idAgence  (previously institution_CODE_INSTITUTION / AGENCE_CODE_AGENCE).
 * New: fk_idCategorie field + UniqueConstraint on the 3-tuple.
 */
@Entity
@Table(
    name = "CompteurMembre",
    uniqueConstraints = @UniqueConstraint(
        name  = "UQ_CompteurMembre_inst_agence_cat",
        columnNames = {"fk_idInstitution", "fk_idAgence", "fk_idCategorie"}
    )
)
@DynamicUpdate
public class CompteurMembre implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_COMPTEUR_MEMBRE", nullable = false)
    private Long idCompteurMembre;

    /** Current counter value; never null, starts at 0. */
    @NotNull
    @Column(name = "COMPTEUR", nullable = false)
    private Long compteur = 0L;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    /**
     * FK to member-category table (phase 2).
     * Kept as a plain String (AN/25) to avoid a cross-phase dependency.
     * Column name matches DDL: fk_idCategorie.
     */
    @Size(max = 25)
    @Column(name = "fk_idCategorie", length = 25)
    private String fkIdCategorie;

    /**
     * FK to institution.
     * Column name matches DDL: fk_idInstitution.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "fk_idInstitution",
        referencedColumnName = "CODE_INSTITUTION",
        foreignKey = @ForeignKey(name = "FK_CompteurMembre_institution")
    )
    private Institution institution;

    /**
     * FK to AGENCE.
     * Column name matches DDL: fk_idAgence.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "fk_idAgence",
        referencedColumnName = "CODE_AGENCE",
        foreignKey = @ForeignKey(name = "FK_CompteurMembre_AGENCE")
    )
    private Agence agence;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public CompteurMembre() {
    }

    public CompteurMembre(Long idCompteurMembre, Long compteur, Integer version, String fkIdCategorie, Institution institution, Agence agence) {
        this.idCompteurMembre = idCompteurMembre;
        this.compteur = compteur;
        this.version = version;
        this.fkIdCategorie = fkIdCategorie;
        this.institution = institution;
        this.agence = agence;
    }

    public Long getIdCompteurMembre() {
        return idCompteurMembre;
    }

    public void setIdCompteurMembre(Long idCompteurMembre) {
        this.idCompteurMembre = idCompteurMembre;
    }

    public Long getCompteur() {
        return compteur;
    }

    public void setCompteur(Long compteur) {
        this.compteur = compteur;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getFkIdCategorie() {
        return fkIdCategorie;
    }

    public void setFkIdCategorie(String fkIdCategorie) {
        this.fkIdCategorie = fkIdCategorie;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Agence getAgence() {
        return agence;
    }

    public void setAgence(Agence agence) {
        this.agence = agence;
    }

    @Override
    public String toString() {
        return "CompteurMembre("
            + "idCompteurMembre=" + idCompteurMembre
            + ")";
    }
}

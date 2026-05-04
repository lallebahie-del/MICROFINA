package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * ValeurFraisCategorie – adhesion-fee values broken down by member category.
 * Each row ties one member category (String code, AN/25) to one
 * {@link ValeurFraisAdhesion} record.
 *
 * DDL source of truth: P1-016-CREATE-TABLE-ValeurFraisCategorie.xml.
 *
 * Fixes applied:
 *   - UniqueConstraint renamed to UQ_ValeurFraisCategorie_cat_frais (matches P1-016).
 *   - LIEGROUPE column restored (was incorrectly removed in prior pass).
 */
@Entity
@Table(
    name = "ValeurFraisCategorie",
    uniqueConstraints = @UniqueConstraint(
        name        = "UQ_ValeurFraisCategorie_cat_frais",
        columnNames = {"categorie", "valeurFraisAdh"}
    )
)
@DynamicUpdate
public class ValeurFraisCategorie implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDVFRAISADHCATEGORIE", nullable = false)
    private Long idVFraisAdhCategorie;

    /**
     * FK to the member-category table (phase 2).
     * Spec type: AN/25 (NVARCHAR).
     */
    @Size(max = 25)
    @Column(name = "categorie", length = 25)
    private String categorie;

    /**
     * Whether this row is linked to a group member.
     * N-type in spec (BIT in SQL Server).
     */
    @Column(name = "LIEGROUPE")
    private Boolean lieGroupe = false;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    /** FK to ValeurFraisAdhesion (stored in the ValeurFrais SINGLE_TABLE). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "valeurFraisAdh",
        referencedColumnName = "IDVALEURFRAIS",
        foreignKey           = @ForeignKey(name = "FK_ValeurFraisCategorie_ValeurFrais")
    )
    private ValeurFraisAdhesion valeurFraisAdh;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public ValeurFraisCategorie() {
    }

    public ValeurFraisCategorie(Long idVFraisAdhCategorie, String categorie, Boolean lieGroupe, Integer version, ValeurFraisAdhesion valeurFraisAdh) {
        this.idVFraisAdhCategorie = idVFraisAdhCategorie;
        this.categorie = categorie;
        this.lieGroupe = lieGroupe;
        this.version = version;
        this.valeurFraisAdh = valeurFraisAdh;
    }

    public Long getIdVFraisAdhCategorie() {
        return idVFraisAdhCategorie;
    }

    public void setIdVFraisAdhCategorie(Long idVFraisAdhCategorie) {
        this.idVFraisAdhCategorie = idVFraisAdhCategorie;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public Boolean getLieGroupe() {
        return lieGroupe;
    }

    public void setLieGroupe(Boolean lieGroupe) {
        this.lieGroupe = lieGroupe;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public ValeurFraisAdhesion getValeurFraisAdh() {
        return valeurFraisAdh;
    }

    public void setValeurFraisAdh(ValeurFraisAdhesion valeurFraisAdh) {
        this.valeurFraisAdh = valeurFraisAdh;
    }

    @Override
    public String toString() {
        return "ValeurFraisCategorie("
            + "idVFraisAdhCategorie=" + idVFraisAdhCategorie
            + ")";
    }
}

package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * ValeurFrais – fee-value record (base class).
 * Subclassed by {@link ValeurFraisAdhesion} using SINGLE_TABLE inheritance.
 *
 * Decision: SINGLE_TABLE was chosen because the spec shows no unique columns
 * in VALEURFRAISADHESION that would justify a separate physical table.
 * The DTYPE column acts as the discriminator.
 *
 * DDL source of truth: P1-014-CREATE-TABLE-ValeurFrais.xml.
 *
 * Spec corrections applied (final pass):
 *   - Removed invented fields: ACTIF, formeJuridique, ageMin, ageMax.
 *   - compte  typed as String (AN/15) per spec, not Long.
 *   - numproduit typed as String (AN/20) per spec, not Long.
 *   - natureFrais added as Long FK (numeric N type, spec p.69).
 */
@Entity
@Table(name = "ValeurFrais")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name             = "DTYPE",
    discriminatorType = DiscriminatorType.STRING,
    columnDefinition = "NVARCHAR(31)"
)
@DiscriminatorValue("ValeurFrais")
@DynamicUpdate
public class ValeurFrais implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDVALEURFRAIS", nullable = false)
    private Long idValeurFrais;

    /** Fee amount or rate. DECIMAL(19,4). */
    @Column(name = "VALEURFRAIS", precision = 19, scale = 4)
    private BigDecimal valeurFrais;

    /** "M" = fixed amount, "P" = percentage. */
    @Size(max = 1)
    @Column(name = "TYPE_VALEUR", length = 1)
    private String typeValeur;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    /**
     * FK to the fee definition table (Frais – phase 2).
     * Numeric (N type per spec).
     */
    @Column(name = "frais")
    private Long frais;

    /**
     * FK to the nature-of-fee table (natureFrais – phase 2, spec p.69).
     * Numeric (N type per spec).
     */
    @Column(name = "natureFrais")
    private Long natureFrais;

    /**
     * FK to accounting plan account.
     * AN/15 (NVARCHAR) per spec — account number string, NOT a numeric ID.
     */
    @Size(max = 15)
    @Column(name = "compte", length = 15)
    private String compte;

    /**
     * FK to sousTypeMembre (phase 2).
     * Numeric (N type per spec).
     */
    @Column(name = "sousTypeMembre")
    private Long sousTypeMembre;

    /**
     * FK to typeMembre (phase 2).
     * Numeric (N type per spec).
     */
    @Column(name = "typeMembre")
    private Long typeMembre;

    /**
     * FK to credit product (phase 2).
     * AN/20 (NVARCHAR) per spec — product code string, NOT a numeric ID.
     */
    @Size(max = 20)
    @Column(name = "numproduit", length = 20)
    private String numProduit;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public ValeurFrais() {
    }

    public ValeurFrais(Long idValeurFrais, BigDecimal valeurFrais, String typeValeur, Integer version, Long frais, Long natureFrais, String compte, Long sousTypeMembre, Long typeMembre, String numProduit) {
        this.idValeurFrais = idValeurFrais;
        this.valeurFrais = valeurFrais;
        this.typeValeur = typeValeur;
        this.version = version;
        this.frais = frais;
        this.natureFrais = natureFrais;
        this.compte = compte;
        this.sousTypeMembre = sousTypeMembre;
        this.typeMembre = typeMembre;
        this.numProduit = numProduit;
    }

    public Long getIdValeurFrais() {
        return idValeurFrais;
    }

    public void setIdValeurFrais(Long idValeurFrais) {
        this.idValeurFrais = idValeurFrais;
    }

    public BigDecimal getValeurFrais() {
        return valeurFrais;
    }

    public void setValeurFrais(BigDecimal valeurFrais) {
        this.valeurFrais = valeurFrais;
    }

    public String getTypeValeur() {
        return typeValeur;
    }

    public void setTypeValeur(String typeValeur) {
        this.typeValeur = typeValeur;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getFrais() {
        return frais;
    }

    public void setFrais(Long frais) {
        this.frais = frais;
    }

    public Long getNatureFrais() {
        return natureFrais;
    }

    public void setNatureFrais(Long natureFrais) {
        this.natureFrais = natureFrais;
    }

    public String getCompte() {
        return compte;
    }

    public void setCompte(String compte) {
        this.compte = compte;
    }

    public Long getSousTypeMembre() {
        return sousTypeMembre;
    }

    public void setSousTypeMembre(Long sousTypeMembre) {
        this.sousTypeMembre = sousTypeMembre;
    }

    public Long getTypeMembre() {
        return typeMembre;
    }

    public void setTypeMembre(Long typeMembre) {
        this.typeMembre = typeMembre;
    }

    public String getNumProduit() {
        return numProduit;
    }

    public void setNumProduit(String numProduit) {
        this.numProduit = numProduit;
    }

    @Override
    public String toString() {
        return "ValeurFrais("
            + "idValeurFrais=" + idValeurFrais
            + ")";
    }
}

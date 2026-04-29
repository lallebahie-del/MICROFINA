package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * ProduitPartSociale – social-share (capital social) product definition.
 *
 * Defines the parameters governing the subscription, holding, transfer,
 * and dividend distribution of cooperative shares (parts sociales).
 *
 * FK references:
 *   {@link FamillePartSociale} – share family classification (Phase 1)
 *
 * DDL source of truth: P3-009-CREATE-TABLE-PRODUITPARTSOCIALE.xml.
 * Spec: Phase 3, Section 3.
 */
@Entity
@Table(name = "PRODUITPARTSOCIALE")
@DynamicUpdate
public class ProduitPartSociale implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @NotBlank
    @Size(max = 50)
    @Column(name = "NUMPRODUIT", length = 50, nullable = false)
    private String numProduit;

    // ── Identity ──────────────────────────────────────────────────

    @Size(max = 255)
    @Column(name = "NOMPRODUIT", length = 255)
    private String nomProduit;

    @Size(max = 255)
    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    /** 1 = product is active. */
    @Column(name = "ACTIF")
    private Integer actif = 1;

    // ── Share parameters ──────────────────────────────────────────

    /** Nominal (face) value per share, e.g. 5 000 XOF. */
    @Column(name = "VALEUR_NOMINALE", precision = 19, scale = 4)
    private BigDecimal valeurNominale;

    /** Minimum number of shares a member must hold. */
    @Column(name = "NOMBRE_PARTS_MIN")
    private Integer nombrePartsMin;

    /** Maximum number of shares a member may hold. */
    @Column(name = "NOMBRE_PARTS_MAX")
    private Integer nombrePartsMax;

    // ── Dividend ──────────────────────────────────────────────────

    /** Dividend rate (% of nominal value per period). */
    @Column(name = "TAUX_DIVIDENDE", precision = 19, scale = 4)
    private BigDecimal tauxDividende;

    /** ANNUEL, SEMESTRIEL, etc. */
    @Size(max = 50)
    @Column(name = "PERIODICITE_DIVIDENDE", length = 50)
    private String periodiciteDividende;

    // ── Accounting accounts ───────────────────────────────────────

    @Size(max = 30)
    @Column(name = "COMPTE_CAPITAL_SOCIAL", length = 30)
    private String compteCapitalSocial;

    @Size(max = 30)
    @Column(name = "COMPTE_DIVIDENDE", length = 30)
    private String compteDividende;

    @Size(max = 30)
    @Column(name = "COMPTE_RESERVE", length = 30)
    private String compteReserve;

    /** Account for share repurchase / partial withdrawal. */
    @Size(max = 30)
    @Column(name = "COMPTE_REMBOURSEMENT", length = 30)
    private String compteRemboursement;

    // ── Transfer / exit rules ─────────────────────────────────────

    /** 1 = shares may be transferred between members. */
    @Column(name = "TRANSFERT_AUTORISE")
    private Integer transfertAutorise = 0;

    /** Minimum holding period (months) before withdrawal is allowed. */
    @Column(name = "DUREE_INDISPONIBILITE")
    private Integer dureeIndisponibilite;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── FK ────────────────────────────────────────────────────────

    /**
     * Share-product family (Phase 1).
     * FK to FamillePartSociale(CODEFAMILLEPARTSOCIALE).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "codefamillepartsociale",
        referencedColumnName = "IDFAMILLEPARTSOCIALE",
        foreignKey           = @ForeignKey(name = "FK_PRODUITPARTSOCIALE_FamillePartSociale")
    )
    private FamillePartSociale famillePartSociale;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public ProduitPartSociale() {
    }

    public ProduitPartSociale(String numProduit, String nomProduit, String description, Integer actif, BigDecimal valeurNominale, Integer nombrePartsMin, Integer nombrePartsMax, BigDecimal tauxDividende, String periodiciteDividende, String compteCapitalSocial, String compteDividende, String compteReserve, String compteRemboursement, Integer transfertAutorise, Integer dureeIndisponibilite, Integer version, FamillePartSociale famillePartSociale) {
        this.numProduit = numProduit;
        this.nomProduit = nomProduit;
        this.description = description;
        this.actif = actif;
        this.valeurNominale = valeurNominale;
        this.nombrePartsMin = nombrePartsMin;
        this.nombrePartsMax = nombrePartsMax;
        this.tauxDividende = tauxDividende;
        this.periodiciteDividende = periodiciteDividende;
        this.compteCapitalSocial = compteCapitalSocial;
        this.compteDividende = compteDividende;
        this.compteReserve = compteReserve;
        this.compteRemboursement = compteRemboursement;
        this.transfertAutorise = transfertAutorise;
        this.dureeIndisponibilite = dureeIndisponibilite;
        this.version = version;
        this.famillePartSociale = famillePartSociale;
    }

    public String getNumProduit() {
        return numProduit;
    }

    public void setNumProduit(String numProduit) {
        this.numProduit = numProduit;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getActif() {
        return actif;
    }

    public void setActif(Integer actif) {
        this.actif = actif;
    }

    public BigDecimal getValeurNominale() {
        return valeurNominale;
    }

    public void setValeurNominale(BigDecimal valeurNominale) {
        this.valeurNominale = valeurNominale;
    }

    public Integer getNombrePartsMin() {
        return nombrePartsMin;
    }

    public void setNombrePartsMin(Integer nombrePartsMin) {
        this.nombrePartsMin = nombrePartsMin;
    }

    public Integer getNombrePartsMax() {
        return nombrePartsMax;
    }

    public void setNombrePartsMax(Integer nombrePartsMax) {
        this.nombrePartsMax = nombrePartsMax;
    }

    public BigDecimal getTauxDividende() {
        return tauxDividende;
    }

    public void setTauxDividende(BigDecimal tauxDividende) {
        this.tauxDividende = tauxDividende;
    }

    public String getPeriodiciteDividende() {
        return periodiciteDividende;
    }

    public void setPeriodiciteDividende(String periodiciteDividende) {
        this.periodiciteDividende = periodiciteDividende;
    }

    public String getCompteCapitalSocial() {
        return compteCapitalSocial;
    }

    public void setCompteCapitalSocial(String compteCapitalSocial) {
        this.compteCapitalSocial = compteCapitalSocial;
    }

    public String getCompteDividende() {
        return compteDividende;
    }

    public void setCompteDividende(String compteDividende) {
        this.compteDividende = compteDividende;
    }

    public String getCompteReserve() {
        return compteReserve;
    }

    public void setCompteReserve(String compteReserve) {
        this.compteReserve = compteReserve;
    }

    public String getCompteRemboursement() {
        return compteRemboursement;
    }

    public void setCompteRemboursement(String compteRemboursement) {
        this.compteRemboursement = compteRemboursement;
    }

    public Integer getTransfertAutorise() {
        return transfertAutorise;
    }

    public void setTransfertAutorise(Integer transfertAutorise) {
        this.transfertAutorise = transfertAutorise;
    }

    public Integer getDureeIndisponibilite() {
        return dureeIndisponibilite;
    }

    public void setDureeIndisponibilite(Integer dureeIndisponibilite) {
        this.dureeIndisponibilite = dureeIndisponibilite;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public FamillePartSociale getFamillePartSociale() {
        return famillePartSociale;
    }

    public void setFamillePartSociale(FamillePartSociale famillePartSociale) {
        this.famillePartSociale = famillePartSociale;
    }

    @Override
    public String toString() {
        return "ProduitPartSociale("
            + "numProduit=" + numProduit
            + ")";
    }
}

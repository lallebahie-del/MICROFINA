package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * ProduitEpargne – savings product definition.
 *
 * Defines the parameters of a savings product: type (à vue, à terme, bloqué,
 * part sociale), amount constraints, interest rates, periodicity, fees,
 * accounting accounts, and group-savings flags.
 *
 * Referenced by {@link ProduitCredit} via three deferred FK columns
 * (wired in P3-010 changeset):
 *   numproduitPourEpargneOblig, numproduitPourGaranti,
 *   numproduitPourPdrtLieCrdit.
 *
 * DDL source of truth: P3-007-CREATE-TABLE-PRODUITEPARGNE.xml.
 * Spec: Phase 3, Section 2.
 */
@Entity
@Table(name = "PRODUITEPARGNE")
@DynamicUpdate
public class ProduitEpargne implements Serializable {

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

    /** 1 = product is available for new account openings. */
    @Column(name = "ACTIF")
    private Integer actif = 1;

    // ── Savings type ──────────────────────────────────────────────

    /**
     * VUE=à vue (demand), TERME=à terme (fixed-term),
     * BLOC=bloqué (locked), PPS=Part Sociale.
     */
    @Size(max = 50)
    @Column(name = "TYPEEPARGNE", length = 50)
    private String typeEpargne;

    // ── Amount constraints ────────────────────────────────────────

    @Column(name = "SOLDE_MIN", precision = 19, scale = 4)
    private BigDecimal soldeMin;

    @Column(name = "SOLDE_MAX", precision = 19, scale = 4)
    private BigDecimal soldeMax;

    @Column(name = "MONTANT_OUVERTURE_MIN", precision = 19, scale = 4)
    private BigDecimal montantOuvertureMin;

    @Column(name = "MONTANT_OUVERTURE_MAX", precision = 19, scale = 4)
    private BigDecimal montantOuvertureMax;

    @Column(name = "MONTANT_RETRAIT_MIN", precision = 19, scale = 4)
    private BigDecimal montantRetraitMin;

    @Column(name = "MONTANT_RETRAIT_MAX", precision = 19, scale = 4)
    private BigDecimal montantRetraitMax;

    // ── Interest ──────────────────────────────────────────────────

    @Column(name = "TAUXINTERET", precision = 19, scale = 4)
    private BigDecimal tauxInteret;

    @Column(name = "TAUXINTERET_MIN", precision = 19, scale = 4)
    private BigDecimal tauxInteretMin;

    @Column(name = "TAUXINTERET_MAX", precision = 19, scale = 4)
    private BigDecimal tauxInteretMax;

    /** MENSUEL, TRIMESTRIEL, ANNUEL, ECHEANCE. */
    @Size(max = 50)
    @Column(name = "PERIODICITE_INTERET", length = 50)
    private String periodiciteInteret;

    /** 1 = interest is capitalised (added to principal). */
    @Column(name = "CAPITALISATION_INTERET")
    private Integer capitalisationInteret = 0;

    // ── Term savings ──────────────────────────────────────────────

    @Column(name = "DUREE_MIN")
    private Integer dureeMin;

    @Column(name = "DUREE_MAX")
    private Integer dureeMax;

    /** Penalty rate (%) for early withdrawal. */
    @Column(name = "TAUX_PENALITE_RETRAIT_ANTICIPE", precision = 19, scale = 4)
    private BigDecimal tauxPenaliteRetraitAnticipe;

    // ── Fees ──────────────────────────────────────────────────────

    @Column(name = "TAUX_AGIOS", precision = 19, scale = 4)
    private BigDecimal tauxAgios;

    @Column(name = "FRAIS_TENUE_COMPTE", precision = 19, scale = 4)
    private BigDecimal fraisTenutCompte;

    // ── Accounting accounts ───────────────────────────────────────

    @Size(max = 30)
    @Column(name = "COMPTE_EPARGNE", length = 30)
    private String compteEpargne;

    @Size(max = 30)
    @Column(name = "COMPTE_INTERET", length = 30)
    private String compteInteret;

    @Size(max = 30)
    @Column(name = "COMPTE_AGIOS", length = 30)
    private String compteAgios;

    @Size(max = 30)
    @Column(name = "COMPTE_FRAIS", length = 30)
    private String compteFrais;

    // ── SMS notification defaults ─────────────────────────────────

    @Size(max = 10)
    @Column(name = "SMS_DEPOT", length = 10)
    private String smsDepot;

    @Size(max = 10)
    @Column(name = "SMS_RETRAIT", length = 10)
    private String smsRetrait;

    // ── Flags ─────────────────────────────────────────────────────

    /** 1 = accounts under this product can be system-blocked. */
    @Column(name = "BLOCAGE_AUTORISE")
    private Integer blocageAutorise = 0;

    /** 1 = product supports group savings (CompteGroupe). */
    @Column(name = "EPARGNE_GROUPE")
    private Integer epargneGroupe = 0;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public ProduitEpargne() {
    }

    public ProduitEpargne(String numProduit, String nomProduit, String description, Integer actif, String typeEpargne, BigDecimal soldeMin, BigDecimal soldeMax, BigDecimal montantOuvertureMin, BigDecimal montantOuvertureMax, BigDecimal montantRetraitMin, BigDecimal montantRetraitMax, BigDecimal tauxInteret, BigDecimal tauxInteretMin, BigDecimal tauxInteretMax, String periodiciteInteret, Integer capitalisationInteret, Integer dureeMin, Integer dureeMax, BigDecimal tauxPenaliteRetraitAnticipe, BigDecimal tauxAgios, BigDecimal fraisTenutCompte, String compteEpargne, String compteInteret, String compteAgios, String compteFrais, String smsDepot, String smsRetrait, Integer blocageAutorise, Integer epargneGroupe, Integer version) {
        this.numProduit = numProduit;
        this.nomProduit = nomProduit;
        this.description = description;
        this.actif = actif;
        this.typeEpargne = typeEpargne;
        this.soldeMin = soldeMin;
        this.soldeMax = soldeMax;
        this.montantOuvertureMin = montantOuvertureMin;
        this.montantOuvertureMax = montantOuvertureMax;
        this.montantRetraitMin = montantRetraitMin;
        this.montantRetraitMax = montantRetraitMax;
        this.tauxInteret = tauxInteret;
        this.tauxInteretMin = tauxInteretMin;
        this.tauxInteretMax = tauxInteretMax;
        this.periodiciteInteret = periodiciteInteret;
        this.capitalisationInteret = capitalisationInteret;
        this.dureeMin = dureeMin;
        this.dureeMax = dureeMax;
        this.tauxPenaliteRetraitAnticipe = tauxPenaliteRetraitAnticipe;
        this.tauxAgios = tauxAgios;
        this.fraisTenutCompte = fraisTenutCompte;
        this.compteEpargne = compteEpargne;
        this.compteInteret = compteInteret;
        this.compteAgios = compteAgios;
        this.compteFrais = compteFrais;
        this.smsDepot = smsDepot;
        this.smsRetrait = smsRetrait;
        this.blocageAutorise = blocageAutorise;
        this.epargneGroupe = epargneGroupe;
        this.version = version;
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

    public String getTypeEpargne() {
        return typeEpargne;
    }

    public void setTypeEpargne(String typeEpargne) {
        this.typeEpargne = typeEpargne;
    }

    public BigDecimal getSoldeMin() {
        return soldeMin;
    }

    public void setSoldeMin(BigDecimal soldeMin) {
        this.soldeMin = soldeMin;
    }

    public BigDecimal getSoldeMax() {
        return soldeMax;
    }

    public void setSoldeMax(BigDecimal soldeMax) {
        this.soldeMax = soldeMax;
    }

    public BigDecimal getMontantOuvertureMin() {
        return montantOuvertureMin;
    }

    public void setMontantOuvertureMin(BigDecimal montantOuvertureMin) {
        this.montantOuvertureMin = montantOuvertureMin;
    }

    public BigDecimal getMontantOuvertureMax() {
        return montantOuvertureMax;
    }

    public void setMontantOuvertureMax(BigDecimal montantOuvertureMax) {
        this.montantOuvertureMax = montantOuvertureMax;
    }

    public BigDecimal getMontantRetraitMin() {
        return montantRetraitMin;
    }

    public void setMontantRetraitMin(BigDecimal montantRetraitMin) {
        this.montantRetraitMin = montantRetraitMin;
    }

    public BigDecimal getMontantRetraitMax() {
        return montantRetraitMax;
    }

    public void setMontantRetraitMax(BigDecimal montantRetraitMax) {
        this.montantRetraitMax = montantRetraitMax;
    }

    public BigDecimal getTauxInteret() {
        return tauxInteret;
    }

    public void setTauxInteret(BigDecimal tauxInteret) {
        this.tauxInteret = tauxInteret;
    }

    public BigDecimal getTauxInteretMin() {
        return tauxInteretMin;
    }

    public void setTauxInteretMin(BigDecimal tauxInteretMin) {
        this.tauxInteretMin = tauxInteretMin;
    }

    public BigDecimal getTauxInteretMax() {
        return tauxInteretMax;
    }

    public void setTauxInteretMax(BigDecimal tauxInteretMax) {
        this.tauxInteretMax = tauxInteretMax;
    }

    public String getPeriodiciteInteret() {
        return periodiciteInteret;
    }

    public void setPeriodiciteInteret(String periodiciteInteret) {
        this.periodiciteInteret = periodiciteInteret;
    }

    public Integer getCapitalisationInteret() {
        return capitalisationInteret;
    }

    public void setCapitalisationInteret(Integer capitalisationInteret) {
        this.capitalisationInteret = capitalisationInteret;
    }

    public Integer getDureeMin() {
        return dureeMin;
    }

    public void setDureeMin(Integer dureeMin) {
        this.dureeMin = dureeMin;
    }

    public Integer getDureeMax() {
        return dureeMax;
    }

    public void setDureeMax(Integer dureeMax) {
        this.dureeMax = dureeMax;
    }

    public BigDecimal getTauxPenaliteRetraitAnticipe() {
        return tauxPenaliteRetraitAnticipe;
    }

    public void setTauxPenaliteRetraitAnticipe(BigDecimal tauxPenaliteRetraitAnticipe) {
        this.tauxPenaliteRetraitAnticipe = tauxPenaliteRetraitAnticipe;
    }

    public BigDecimal getTauxAgios() {
        return tauxAgios;
    }

    public void setTauxAgios(BigDecimal tauxAgios) {
        this.tauxAgios = tauxAgios;
    }

    public BigDecimal getFraisTenutCompte() {
        return fraisTenutCompte;
    }

    public void setFraisTenutCompte(BigDecimal fraisTenutCompte) {
        this.fraisTenutCompte = fraisTenutCompte;
    }

    public String getCompteEpargne() {
        return compteEpargne;
    }

    public void setCompteEpargne(String compteEpargne) {
        this.compteEpargne = compteEpargne;
    }

    public String getCompteInteret() {
        return compteInteret;
    }

    public void setCompteInteret(String compteInteret) {
        this.compteInteret = compteInteret;
    }

    public String getCompteAgios() {
        return compteAgios;
    }

    public void setCompteAgios(String compteAgios) {
        this.compteAgios = compteAgios;
    }

    public String getCompteFrais() {
        return compteFrais;
    }

    public void setCompteFrais(String compteFrais) {
        this.compteFrais = compteFrais;
    }

    public String getSmsDepot() {
        return smsDepot;
    }

    public void setSmsDepot(String smsDepot) {
        this.smsDepot = smsDepot;
    }

    public String getSmsRetrait() {
        return smsRetrait;
    }

    public void setSmsRetrait(String smsRetrait) {
        this.smsRetrait = smsRetrait;
    }

    public Integer getBlocageAutorise() {
        return blocageAutorise;
    }

    public void setBlocageAutorise(Integer blocageAutorise) {
        this.blocageAutorise = blocageAutorise;
    }

    public Integer getEpargneGroupe() {
        return epargneGroupe;
    }

    public void setEpargneGroupe(Integer epargneGroupe) {
        this.epargneGroupe = epargneGroupe;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ProduitEpargne("
            + "numProduit=" + numProduit
            + ")";
    }
}

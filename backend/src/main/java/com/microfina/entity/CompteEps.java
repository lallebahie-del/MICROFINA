package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CompteEps – savings account (Compte Épargne).
 *
 * Standalone entity; one row per member savings account.
 * PK = NUMCOMPTE (AN/255).
 *
 * DDL source of truth: P2-006-CREATE-TABLE-COMPTEEPS.xml.
 * Spec p.20-21.
 *
 * Phase-2+ FK columns (produitEpargne, produitPartSociale)
 * stored as raw String values.
 */
@Entity
@Table(name = "COMPTEEPS")
@DynamicUpdate
public class CompteEps implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @NotBlank
    @Size(max = 255)
    @Column(name = "NUMCOMPTE", length = 255, nullable = false)
    private String numCompte;

    // ── SMS notification flags (AN/255 = "O"/"N") ─────────────────

    @Size(max = 255)
    @Column(name = "SMSDEPOTCHEQUE", length = 255)
    private String smsDepotCheque;

    @Size(max = 255)
    @Column(name = "SMSPRELEVEMENT", length = 255)
    private String smsPrelevement;

    @Size(max = 255)
    @Column(name = "SMSRETRAIT", length = 255)
    private String smsRetrait;

    @Size(max = 255)
    @Column(name = "SMSVIREMENTSALAIRE", length = 255)
    private String smsVirementSalaire;

    @Size(max = 255)
    @Column(name = "CODESMS", length = 255)
    private String codeSms;

    // ── Account state flags ───────────────────────────────────────

    @Size(max = 255)
    @Column(name = "BLOQUE", length = 255)
    private String bloque;

    @Size(max = 255)
    @Column(name = "FERME", length = 255)
    private String ferme;

    @Column(name = "ENCAISSE")
    private Integer encaisse;

    @Size(max = 255)
    @Column(name = "EXONERE", length = 255)
    private String exonere;

    @Size(max = 255)
    @Column(name = "OPTIONEPARGNE", length = 255)
    private String optionEpargne;

    @Size(max = 255)
    @Column(name = "PRIORITAIRE", length = 255)
    private String prioritaire;

    @Size(max = 255)
    @Column(name = "REMARQUE", length = 255)
    private String remarque;

    @Size(max = 255)
    @Column(name = "TYPEEPARGNE", length = 255)
    private String typeEpargne;

    @Size(max = 255)
    @Column(name = "DETAILDEPOT", length = 255)
    private String detailDepot;

    @Size(max = 255)
    @Column(name = "NUMFRAISSMS", length = 255)
    private String numFraisSms;

    @Size(max = 255)
    @Column(name = "NUMCOMPTABILITE", length = 255)
    private String numComptabilite;

    // ── Financial amounts (DECIMAL 19,4) ──────────────────────────

    @Column(name = "AGIOS", precision = 19, scale = 4)
    private BigDecimal agios;

    @Column(name = "MONTANTOUVERT", precision = 19, scale = 4)
    private BigDecimal montantOuvert;

    @Column(name = "MONTANTBLOQUE", precision = 19, scale = 4)
    private BigDecimal montantBloque;

    @Column(name = "MONTANTDEPOT", precision = 19, scale = 4)
    private BigDecimal montantDepot;

    @Column(name = "MONTANTMG", precision = 19, scale = 4)
    private BigDecimal montantMg;

    @Column(name = "MONTANTINTERET", precision = 19, scale = 4)
    private BigDecimal montantInteret;

    @Column(name = "INTERETS", precision = 19, scale = 4)
    private BigDecimal interets;

    @Column(name = "TAUX", precision = 19, scale = 4)
    private BigDecimal taux;

    @Column(name = "TAUXINTERET", precision = 19, scale = 4)
    private BigDecimal tauxInteret;

    // ── Dates ─────────────────────────────────────────────────────

    @Column(name = "DATEBLOQUE")
    private LocalDate dateBloque;

    @Column(name = "DATECREATION")
    private LocalDate dateCreation;

    @Column(name = "DATEDEBLOQUAGE")
    private LocalDate dateDebloquage;

    @Column(name = "DATEECHEANCE")
    private LocalDate dateEcheance;

    @Column(name = "DATEFERMEE")
    private LocalDate dateFermee;

    @Column(name = "DUREE")
    private Integer duree;

    // ── Extra identifiers ─────────────────────────────────────────

    @Size(max = 50)
    @Column(name = "numcompte_old", length = 50)
    private String numCompteOld;

    @Column(name = "rang_compte")
    private Integer rangCompte;

    @Size(max = 25)
    @Column(name = "code_membre", length = 25)
    private String codeMembre;

    @Size(max = 25)
    @Column(name = "COMPTEEPS_new", length = 25)
    private String compteEpsNew;

    @Size(max = 25)
    @Column(name = "NUMCOMPTE_bis", length = 25)
    private String numCompteBis;

    @Size(max = 30)
    @Column(name = "compte_manuelle", length = 30)
    private String compteManuelle;

    @Size(max = 10)
    @Column(name = "CODE_PROD", length = 10)
    private String codeProd;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── FKs (wired, Phase 2) ──────────────────────────────────────

    /** The member who owns this savings account. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "membre",
        referencedColumnName = "NUM_MEMBRE",
        foreignKey           = @ForeignKey(name = "FK_COMPTEEPS_membres")
    )
    private Membres membre;

    /** Branch that manages this account. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "agence",
        referencedColumnName = "CODE_AGENCE",
        foreignKey           = @ForeignKey(name = "FK_COMPTEEPS_AGENCE")
    )
    private Agence agence;

    // ── Phase-2+ FK columns ───────────────────────────────────────

    /** Savings product code (AN/20). Constraint deferred. */
    @Size(max = 20)
    @Column(name = "produitEpargne", length = 20)
    private String produitEpargne;

    /** Social-share product code (AN/50). Constraint deferred. */
    @Size(max = 50)
    @Column(name = "produitPartSociale", length = 50)
    private String produitPartSociale;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public CompteEps() {
    }

    public CompteEps(String numCompte, String smsDepotCheque, String smsPrelevement, String smsRetrait, String smsVirementSalaire, String codeSms, String bloque, String ferme, Integer encaisse, String exonere, String optionEpargne, String prioritaire, String remarque, String typeEpargne, String detailDepot, String numFraisSms, String numComptabilite, BigDecimal agios, BigDecimal montantOuvert, BigDecimal montantBloque, BigDecimal montantDepot, BigDecimal montantMg, BigDecimal montantInteret, BigDecimal interets, BigDecimal taux, BigDecimal tauxInteret, LocalDate dateBloque, LocalDate dateCreation, LocalDate dateDebloquage, LocalDate dateEcheance, LocalDate dateFermee, Integer duree, String numCompteOld, Integer rangCompte, String codeMembre, String compteEpsNew, String numCompteBis, String compteManuelle, String codeProd, Integer version, Membres membre, Agence agence, String produitEpargne, String produitPartSociale) {
        this.numCompte = numCompte;
        this.smsDepotCheque = smsDepotCheque;
        this.smsPrelevement = smsPrelevement;
        this.smsRetrait = smsRetrait;
        this.smsVirementSalaire = smsVirementSalaire;
        this.codeSms = codeSms;
        this.bloque = bloque;
        this.ferme = ferme;
        this.encaisse = encaisse;
        this.exonere = exonere;
        this.optionEpargne = optionEpargne;
        this.prioritaire = prioritaire;
        this.remarque = remarque;
        this.typeEpargne = typeEpargne;
        this.detailDepot = detailDepot;
        this.numFraisSms = numFraisSms;
        this.numComptabilite = numComptabilite;
        this.agios = agios;
        this.montantOuvert = montantOuvert;
        this.montantBloque = montantBloque;
        this.montantDepot = montantDepot;
        this.montantMg = montantMg;
        this.montantInteret = montantInteret;
        this.interets = interets;
        this.taux = taux;
        this.tauxInteret = tauxInteret;
        this.dateBloque = dateBloque;
        this.dateCreation = dateCreation;
        this.dateDebloquage = dateDebloquage;
        this.dateEcheance = dateEcheance;
        this.dateFermee = dateFermee;
        this.duree = duree;
        this.numCompteOld = numCompteOld;
        this.rangCompte = rangCompte;
        this.codeMembre = codeMembre;
        this.compteEpsNew = compteEpsNew;
        this.numCompteBis = numCompteBis;
        this.compteManuelle = compteManuelle;
        this.codeProd = codeProd;
        this.version = version;
        this.membre = membre;
        this.agence = agence;
        this.produitEpargne = produitEpargne;
        this.produitPartSociale = produitPartSociale;
    }

    public String getNumCompte() {
        return numCompte;
    }

    public void setNumCompte(String numCompte) {
        this.numCompte = numCompte;
    }

    public String getSmsDepotCheque() {
        return smsDepotCheque;
    }

    public void setSmsDepotCheque(String smsDepotCheque) {
        this.smsDepotCheque = smsDepotCheque;
    }

    public String getSmsPrelevement() {
        return smsPrelevement;
    }

    public void setSmsPrelevement(String smsPrelevement) {
        this.smsPrelevement = smsPrelevement;
    }

    public String getSmsRetrait() {
        return smsRetrait;
    }

    public void setSmsRetrait(String smsRetrait) {
        this.smsRetrait = smsRetrait;
    }

    public String getSmsVirementSalaire() {
        return smsVirementSalaire;
    }

    public void setSmsVirementSalaire(String smsVirementSalaire) {
        this.smsVirementSalaire = smsVirementSalaire;
    }

    public String getCodeSms() {
        return codeSms;
    }

    public void setCodeSms(String codeSms) {
        this.codeSms = codeSms;
    }

    public String getBloque() {
        return bloque;
    }

    public void setBloque(String bloque) {
        this.bloque = bloque;
    }

    public String getFerme() {
        return ferme;
    }

    public void setFerme(String ferme) {
        this.ferme = ferme;
    }

    public Integer getEncaisse() {
        return encaisse;
    }

    public void setEncaisse(Integer encaisse) {
        this.encaisse = encaisse;
    }

    public String getExonere() {
        return exonere;
    }

    public void setExonere(String exonere) {
        this.exonere = exonere;
    }

    public String getOptionEpargne() {
        return optionEpargne;
    }

    public void setOptionEpargne(String optionEpargne) {
        this.optionEpargne = optionEpargne;
    }

    public String getPrioritaire() {
        return prioritaire;
    }

    public void setPrioritaire(String prioritaire) {
        this.prioritaire = prioritaire;
    }

    public String getRemarque() {
        return remarque;
    }

    public void setRemarque(String remarque) {
        this.remarque = remarque;
    }

    public String getTypeEpargne() {
        return typeEpargne;
    }

    public void setTypeEpargne(String typeEpargne) {
        this.typeEpargne = typeEpargne;
    }

    public String getDetailDepot() {
        return detailDepot;
    }

    public void setDetailDepot(String detailDepot) {
        this.detailDepot = detailDepot;
    }

    public String getNumFraisSms() {
        return numFraisSms;
    }

    public void setNumFraisSms(String numFraisSms) {
        this.numFraisSms = numFraisSms;
    }

    public String getNumComptabilite() {
        return numComptabilite;
    }

    public void setNumComptabilite(String numComptabilite) {
        this.numComptabilite = numComptabilite;
    }

    public BigDecimal getAgios() {
        return agios;
    }

    public void setAgios(BigDecimal agios) {
        this.agios = agios;
    }

    public BigDecimal getMontantOuvert() {
        return montantOuvert;
    }

    public void setMontantOuvert(BigDecimal montantOuvert) {
        this.montantOuvert = montantOuvert;
    }

    public BigDecimal getMontantBloque() {
        return montantBloque;
    }

    public void setMontantBloque(BigDecimal montantBloque) {
        this.montantBloque = montantBloque;
    }

    public BigDecimal getMontantDepot() {
        return montantDepot;
    }

    public void setMontantDepot(BigDecimal montantDepot) {
        this.montantDepot = montantDepot;
    }

    public BigDecimal getMontantMg() {
        return montantMg;
    }

    public void setMontantMg(BigDecimal montantMg) {
        this.montantMg = montantMg;
    }

    public BigDecimal getMontantInteret() {
        return montantInteret;
    }

    public void setMontantInteret(BigDecimal montantInteret) {
        this.montantInteret = montantInteret;
    }

    public BigDecimal getInterets() {
        return interets;
    }

    public void setInterets(BigDecimal interets) {
        this.interets = interets;
    }

    public BigDecimal getTaux() {
        return taux;
    }

    public void setTaux(BigDecimal taux) {
        this.taux = taux;
    }

    public BigDecimal getTauxInteret() {
        return tauxInteret;
    }

    public void setTauxInteret(BigDecimal tauxInteret) {
        this.tauxInteret = tauxInteret;
    }

    public LocalDate getDateBloque() {
        return dateBloque;
    }

    public void setDateBloque(LocalDate dateBloque) {
        this.dateBloque = dateBloque;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDate getDateDebloquage() {
        return dateDebloquage;
    }

    public void setDateDebloquage(LocalDate dateDebloquage) {
        this.dateDebloquage = dateDebloquage;
    }

    public LocalDate getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    public LocalDate getDateFermee() {
        return dateFermee;
    }

    public void setDateFermee(LocalDate dateFermee) {
        this.dateFermee = dateFermee;
    }

    public Integer getDuree() {
        return duree;
    }

    public void setDuree(Integer duree) {
        this.duree = duree;
    }

    public String getNumCompteOld() {
        return numCompteOld;
    }

    public void setNumCompteOld(String numCompteOld) {
        this.numCompteOld = numCompteOld;
    }

    public Integer getRangCompte() {
        return rangCompte;
    }

    public void setRangCompte(Integer rangCompte) {
        this.rangCompte = rangCompte;
    }

    public String getCodeMembre() {
        return codeMembre;
    }

    public void setCodeMembre(String codeMembre) {
        this.codeMembre = codeMembre;
    }

    public String getCompteEpsNew() {
        return compteEpsNew;
    }

    public void setCompteEpsNew(String compteEpsNew) {
        this.compteEpsNew = compteEpsNew;
    }

    public String getNumCompteBis() {
        return numCompteBis;
    }

    public void setNumCompteBis(String numCompteBis) {
        this.numCompteBis = numCompteBis;
    }

    public String getCompteManuelle() {
        return compteManuelle;
    }

    public void setCompteManuelle(String compteManuelle) {
        this.compteManuelle = compteManuelle;
    }

    public String getCodeProd() {
        return codeProd;
    }

    public void setCodeProd(String codeProd) {
        this.codeProd = codeProd;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Membres getMembre() {
        return membre;
    }

    public void setMembre(Membres membre) {
        this.membre = membre;
    }

    public Agence getAgence() {
        return agence;
    }

    public void setAgence(Agence agence) {
        this.agence = agence;
    }

    public String getProduitEpargne() {
        return produitEpargne;
    }

    public void setProduitEpargne(String produitEpargne) {
        this.produitEpargne = produitEpargne;
    }

    public String getProduitPartSociale() {
        return produitPartSociale;
    }

    public void setProduitPartSociale(String produitPartSociale) {
        this.produitPartSociale = produitPartSociale;
    }

    @Override
    public String toString() {
        return "CompteEps("
            + "numCompte=" + numCompte
            + ")";
    }
}

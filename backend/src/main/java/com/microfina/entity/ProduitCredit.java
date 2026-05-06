package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * ProduitCredit – master credit product definition.
 *
 * Contains all parameters that govern a credit product: interest rates,
 * grace period rules, penalty charges, disbursement configuration,
 * repayment schedule, accounting chart-of-accounts references, and
 * links to mandatory/guarantee savings products.
 *
 * 50+ columns.
 *
 * FK references (wired):
 *   {@link FamilleProduitCredit}     – product family (Phase 1)
 *   {@link ModeDeCalculInteret}      – interest-calculation mode (Phase 1)
 *   {@link ModelCompteProduitCredit} – accounting model template (Phase 3)
 *   {@link ProduitIslamic}           – Islamic finance type (Phase 3, optional)
 *
 * Deferred FK columns (wired in P3-010, entities available after Phase 3):
 *   numproduitPourEpargneOblig   → PRODUITEPARGNE
 *   numproduitPourGaranti        → PRODUITEPARGNE
 *   numproduitPourPdrtLieCrdit  → PRODUITEPARGNE
 *
 * DDL source of truth: P3-003-CREATE-TABLE-produitcredit.xml.
 * Spec: Phase 3, Section 1.
 */
@Entity
@Table(name = "produitcredit")
@DynamicUpdate
public class ProduitCredit implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @NotBlank
    @Size(max = 20)
    @Column(name = "NUMPRODUIT", length = 20, nullable = false)
    private String numProduit;

    // ── Identity / description ────────────────────────────────────

    @Size(max = 255)
    @Column(name = "NOMPRODUIT", length = 255)
    private String nomProduit;

    @Size(max = 255)
    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    /** 1 = product is available for new credit applications. */
    @Column(name = "ACTIF")
    private Integer actif = 1;

    // ── Amount limits ─────────────────────────────────────────────

    @Column(name = "MONTANTMIN", precision = 19, scale = 4)
    private BigDecimal montantMin;

    @Column(name = "MONTANTMAX", precision = 19, scale = 4)
    private BigDecimal montantMax;

    // ── Duration (months) ─────────────────────────────────────────

    @Column(name = "DUREEMIN")
    private Integer dureeMin;

    @Column(name = "DUREEMAX")
    private Integer dureeMax;

    // ── Interest rates ────────────────────────────────────────────

    @Column(name = "TAUXINTERET", precision = 19, scale = 4)
    private BigDecimal tauxInteret;

    @Column(name = "TAUXINTERETMIN", precision = 19, scale = 4)
    private BigDecimal tauxInteretMin;

    @Column(name = "TAUXINTERETMAX", precision = 19, scale = 4)
    private BigDecimal tauxInteretMax;

    // ── Repayment schedule ────────────────────────────────────────

    /**
     * Periodicity code for repayment instalments.
     * See {@link ModeCalculInteretConstant} PERIODICITE_* constants.
     */
    @Size(max = 50)
    @Column(name = "PERIODICITEREMBOURSEMENT", length = 50)
    private String periodiciteRemboursement;

    /** Total number of repayment instalments. */
    @Column(name = "NOMBREECHEANCE")
    private Integer nombreEcheance;

    // ── Grace period ──────────────────────────────────────────────

    /** Grace period length in periods (same unit as periodiciteRemboursement). */
    @Column(name = "DELAIGRACE")
    private Integer delaiGrace;

    /**
     * 1 = interest continues to accrue during the grace period.
     * 0 = no interest during grace.
     */
    @Column(name = "INTERETPENDANTGRACE")
    private Integer interetPendantGrace = 0;

    /**
     * Grace type.
     * P = principal only (interest still due),
     * T = total (principal + interest suspended).
     * See {@link ModeCalculInteretConstant#GRACE_PRINCIPAL},
     *     {@link ModeCalculInteretConstant#GRACE_TOTAL}.
     */
    @Size(max = 10)
    @Column(name = "TYPEGRACE", length = 10)
    private String typeGrace;

    // ── Penalty / late fees ───────────────────────────────────────

    /** Annual penalty rate (%) applied on overdue amounts. */
    @Column(name = "TAUXPENALITE", precision = 19, scale = 4)
    private BigDecimal tauxPenalite;

    /**
     * Base used for penalty calculation.
     * e.g. "CAPITAL", "ECHEANCE", "RETARD".
     */
    @Size(max = 50)
    @Column(name = "BASECALCULPENALITE", length = 50)
    private String baseCalculPenalite;

    /** Number of tolerance days before penalty is triggered. */
    @Column(name = "JOURSTOLERANCEPENALITE")
    private Integer joursTolerancePenalite;

    // ── Commissions ───────────────────────────────────────────────

    /** Commission rate (%). */
    @Column(name = "TAUXCOMMISSION", precision = 19, scale = 4)
    private BigDecimal tauxCommission;

    /**
     * When commission is collected.
     * See {@link ModeCalculInteretConstant#PRELEVEMENT_DEBUT},
     *     {@link ModeCalculInteretConstant#PRELEVEMENT_ECHEANCE},
     *     {@link ModeCalculInteretConstant#PRELEVEMENT_FIN}.
     */
    @Size(max = 50)
    @Column(name = "MODEPRELEVEMENT_COMMISSION", length = 50)
    private String modePrelevementCommission;

    @Column(name = "MONTANTCOMMISSIONMIN", precision = 19, scale = 4)
    private BigDecimal montantCommissionMin;

    @Column(name = "MONTANTCOMMISSIONMAX", precision = 19, scale = 4)
    private BigDecimal montantCommissionMax;

    // ── Insurance (assurance / mutuelle) ─────────────────────────

    @Column(name = "TAUXASSURANCE", precision = 19, scale = 4)
    private BigDecimal tauxAssurance;

    @Column(name = "MONTANTASSURANCEMIN", precision = 19, scale = 4)
    private BigDecimal montantAssuranceMin;

    // ── Mandatory savings (épargne obligatoire) ───────────────────

    /** % of credit amount that must be deposited as compulsory savings. */
    @Column(name = "TAUXEPARGNEOBLIGATOIRE", precision = 19, scale = 4)
    private BigDecimal tauxEpargneObligatoire;

    /** % of capital that must remain in savings as guarantee. */
    @Column(name = "TAUXGARANTIEEPARGNE", precision = 19, scale = 4)
    private BigDecimal tauxGarantieEpargne;

    // ── Disbursement ──────────────────────────────────────────────

    /**
     * 1 = disburse net amount (gross minus fees deducted at source).
     * 0 = disburse gross, collect fees separately.
     */
    @Column(name = "DECAISSEMENT_NET")
    private Integer decaissementNet = 0;

    // ── Accounting chart-of-accounts references ───────────────────

    @Size(max = 30)
    @Column(name = "COMPTE_CAPITAL", length = 30)
    private String compteCapital;

    @Size(max = 30)
    @Column(name = "COMPTE_INTERET", length = 30)
    private String compteInteret;

    @Size(max = 30)
    @Column(name = "COMPTE_INTERET_COURU", length = 30)
    private String compteInteretCouru;

    @Size(max = 30)
    @Column(name = "COMPTE_COMMISSION", length = 30)
    private String compteCommission;

    @Size(max = 30)
    @Column(name = "COMPTE_ASSURANCE", length = 30)
    private String compteAssurance;

    @Size(max = 30)
    @Column(name = "COMPTE_PENALITE", length = 30)
    private String comptePenalite;

    @Size(max = 30)
    @Column(name = "COMPTE_EPARGNE_OBLIG", length = 30)
    private String compteEpargneOblig;

    @Size(max = 30)
    @Column(name = "COMPTE_DEBLOCAGE", length = 30)
    private String compteDeblocage;

    // ── Provisioning / reclassification accounts ──────────────────

    @Size(max = 30)
    @Column(name = "COMPTE_PROVISION", length = 30)
    private String compteProvision;

    @Size(max = 30)
    @Column(name = "COMPTE_CREANCE_DOUTEUSE", length = 30)
    private String compteCreanceDouteuse;

    // ── Renegotiation / early repayment ───────────────────────────

    /** 1 = renegotiation of loan terms is allowed. */
    @Column(name = "AUTORISER_RENEG")
    private Integer autoriserReneg = 0;

    /** 1 = early (anticipated) repayment is allowed. */
    @Column(name = "AUTORISER_REMBOURSEMENT_ANTICIPE")
    private Integer autoriserRemboursementAnticipe = 0;

    /** Penalty rate (%) applied when loan is repaid ahead of schedule. */
    @Column(name = "TAUX_REMB_ANTICIPE", precision = 19, scale = 4)
    private BigDecimal tauxRembAnticipe;

    // ── Client / membership restrictions ─────────────────────────

    /**
     * Eligible client type.
     * PP = Personne Physique, PM = Personne Morale, G = Groupe.
     */
    @Size(max = 10)
    @Column(name = "TYPE_CLIENT", length = 10)
    private String typeClient;

    /**
     * Credit type classification.
     * See {@link ModeCalculInteretConstant#TYPE_CREDIT_INDIVIDUEL},
     *     {@link ModeCalculInteretConstant#TYPE_CREDIT_GROUPE},
     *     {@link ModeCalculInteretConstant#TYPE_CREDIT_SOLIDAIRE}.
     */
    @Size(max = 50)
    @Column(name = "TYPE_CREDIT", length = 50)
    private String typeCredit;

    // ── Group / solidarity lending ────────────────────────────────

    /** Cycle number for progressive group-lending products. */
    @Column(name = "NUMEROCYCLE")
    private Integer numeroCycle;

    // ── Collateral ────────────────────────────────────────────────

    /** 1 = at least one guarantee is required before disbursement. */
    @Column(name = "GARANTIE_REQUISE")
    private Integer garantieRequise = 0;

    // ── Portfolio limits ──────────────────────────────────────────

    /** Maximum number of active loans a member may hold for this product. */
    @Column(name = "NOMBRE_CREDIT_MAX")
    private Integer nombreCreditMax;

    // ── Interest capitalisation ───────────────────────────────────

    /** 1 = unpaid interest is capitalised (added to outstanding principal). */
    @Column(name = "CAPITALISATION_INTERET")
    private Integer capitalisationInteret = 0;

    // ── Portfolio-quality ageing thresholds (days) ────────────────

    /** Days past due to classify into first delinquency bucket. */
    @Column(name = "SEUIL_RETARD_1")
    private Integer seuilRetard1;

    /** Days past due to classify into second delinquency bucket. */
    @Column(name = "SEUIL_RETARD_2")
    private Integer seuilRetard2;

    /** Days past due to classify into third delinquency bucket (write-off). */
    @Column(name = "SEUIL_RETARD_3")
    private Integer seuilRetard3;

    // ── Deferred FK columns → PRODUITEPARGNE ─────────────────────

    /**
     * Savings product that members must fund before this credit is disbursed.
     * Raw FK value; @ManyToOne wired once ProduitEpargne entity is available.
     */
    @Size(max = 50)
    @Column(name = "numproduitPourEpargneOblig", length = 50)
    private String numproduitPourEpargneOblig;

    /**
     * Savings product pledged as collateral/guarantee for this credit.
     * Raw FK value; @ManyToOne deferred.
     */
    @Size(max = 50)
    @Column(name = "numproduitPourGaranti", length = 50)
    private String numproduitPourGaranti;

    /**
     * Additional savings product tied to (sold together with) this credit.
     * Raw FK value; @ManyToOne deferred.
     */
    @Size(max = 50)
    @Column(name = "numproduitPourPdrtLieCrdit", length = 50)
    private String numproduitPourPdrtLieCrdit;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── FKs (wired) ───────────────────────────────────────────────

    /**
     * Product family (Phase 1).
     * FK to FAMILLEPRODUITCREDIT(CODEFAMILLEPRODUITCREDIT).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "codefamilleproduitcredit",
        referencedColumnName = "CODEFAMILLEPRODUITCREDIT",
        foreignKey           = @ForeignKey(name = "FK_produitcredit_FAMILLEPRODUITCREDIT")
    )
    private FamilleProduitCredit familleProduitCredit;

    /**
     * Interest calculation mode (Phase 1).
     * FK to mode_de_calcul_interet(CODEMODE).
     * Use {@link ModeCalculInteretConstant} to compare MODECALCUL values.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "codemode",
        referencedColumnName = "CODEMODE",
        foreignKey           = @ForeignKey(name = "FK_produitcredit_mode_de_calcul_interet")
    )
    private ModeDeCalculInteret modeDeCalculInteret;

    /**
     * Accounting model template (Phase 3).
     * FK to Model_Compte_Produit_Credit(ID_MODEL_PRODUIT_CREDIT).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "id_model_produit_credit",
        referencedColumnName = "ID_MODEL_PRODUIT_CREDIT",
        foreignKey           = @ForeignKey(name = "FK_produitcredit_Model_Compte_Produit_Credit")
    )
    private ModelCompteProduitCredit modelCompteProduitCredit;

    /**
     * Islamic finance product type (Phase 3, optional).
     * FK to produit_islamic(code_produit).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "code_produit_islamic",
        referencedColumnName = "code_produit",
        foreignKey           = @ForeignKey(name = "FK_produitcredit_produit_islamic")
    )
    private ProduitIslamic produitIslamic;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public ProduitCredit() {
    }

    public ProduitCredit(String numProduit, String nomProduit, String description, Integer actif, BigDecimal montantMin, BigDecimal montantMax, Integer dureeMin, Integer dureeMax, BigDecimal tauxInteret, BigDecimal tauxInteretMin, BigDecimal tauxInteretMax, String periodiciteRemboursement, Integer nombreEcheance, Integer delaiGrace, Integer interetPendantGrace, String typeGrace, BigDecimal tauxPenalite, String baseCalculPenalite, Integer joursTolerancePenalite, BigDecimal tauxCommission, String modePrelevementCommission, BigDecimal montantCommissionMin, BigDecimal montantCommissionMax, BigDecimal tauxAssurance, BigDecimal montantAssuranceMin, BigDecimal tauxEpargneObligatoire, BigDecimal tauxGarantieEpargne, Integer decaissementNet, String compteCapital, String compteInteret, String compteInteretCouru, String compteCommission, String compteAssurance, String comptePenalite, String compteEpargneOblig, String compteDeblocage, String compteProvision, String compteCreanceDouteuse, Integer autoriserReneg, Integer autoriserRemboursementAnticipe, BigDecimal tauxRembAnticipe, String typeClient, String typeCredit, Integer numeroCycle, Integer garantieRequise, Integer nombreCreditMax, Integer capitalisationInteret, Integer seuilRetard1, Integer seuilRetard2, Integer seuilRetard3, String numproduitPourEpargneOblig, String numproduitPourGaranti, String numproduitPourPdrtLieCrdit, Integer version, FamilleProduitCredit familleProduitCredit, ModeDeCalculInteret modeDeCalculInteret, ModelCompteProduitCredit modelCompteProduitCredit, ProduitIslamic produitIslamic) {
        this.numProduit = numProduit;
        this.nomProduit = nomProduit;
        this.description = description;
        this.actif = actif;
        this.montantMin = montantMin;
        this.montantMax = montantMax;
        this.dureeMin = dureeMin;
        this.dureeMax = dureeMax;
        this.tauxInteret = tauxInteret;
        this.tauxInteretMin = tauxInteretMin;
        this.tauxInteretMax = tauxInteretMax;
        this.periodiciteRemboursement = periodiciteRemboursement;
        this.nombreEcheance = nombreEcheance;
        this.delaiGrace = delaiGrace;
        this.interetPendantGrace = interetPendantGrace;
        this.typeGrace = typeGrace;
        this.tauxPenalite = tauxPenalite;
        this.baseCalculPenalite = baseCalculPenalite;
        this.joursTolerancePenalite = joursTolerancePenalite;
        this.tauxCommission = tauxCommission;
        this.modePrelevementCommission = modePrelevementCommission;
        this.montantCommissionMin = montantCommissionMin;
        this.montantCommissionMax = montantCommissionMax;
        this.tauxAssurance = tauxAssurance;
        this.montantAssuranceMin = montantAssuranceMin;
        this.tauxEpargneObligatoire = tauxEpargneObligatoire;
        this.tauxGarantieEpargne = tauxGarantieEpargne;
        this.decaissementNet = decaissementNet;
        this.compteCapital = compteCapital;
        this.compteInteret = compteInteret;
        this.compteInteretCouru = compteInteretCouru;
        this.compteCommission = compteCommission;
        this.compteAssurance = compteAssurance;
        this.comptePenalite = comptePenalite;
        this.compteEpargneOblig = compteEpargneOblig;
        this.compteDeblocage = compteDeblocage;
        this.compteProvision = compteProvision;
        this.compteCreanceDouteuse = compteCreanceDouteuse;
        this.autoriserReneg = autoriserReneg;
        this.autoriserRemboursementAnticipe = autoriserRemboursementAnticipe;
        this.tauxRembAnticipe = tauxRembAnticipe;
        this.typeClient = typeClient;
        this.typeCredit = typeCredit;
        this.numeroCycle = numeroCycle;
        this.garantieRequise = garantieRequise;
        this.nombreCreditMax = nombreCreditMax;
        this.capitalisationInteret = capitalisationInteret;
        this.seuilRetard1 = seuilRetard1;
        this.seuilRetard2 = seuilRetard2;
        this.seuilRetard3 = seuilRetard3;
        this.numproduitPourEpargneOblig = numproduitPourEpargneOblig;
        this.numproduitPourGaranti = numproduitPourGaranti;
        this.numproduitPourPdrtLieCrdit = numproduitPourPdrtLieCrdit;
        this.version = version;
        this.familleProduitCredit = familleProduitCredit;
        this.modeDeCalculInteret = modeDeCalculInteret;
        this.modelCompteProduitCredit = modelCompteProduitCredit;
        this.produitIslamic = produitIslamic;
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

    public BigDecimal getMontantMin() {
        return montantMin;
    }

    public void setMontantMin(BigDecimal montantMin) {
        this.montantMin = montantMin;
    }

    public BigDecimal getMontantMax() {
        return montantMax;
    }

    public void setMontantMax(BigDecimal montantMax) {
        this.montantMax = montantMax;
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

    public String getPeriodiciteRemboursement() {
        return periodiciteRemboursement;
    }

    public void setPeriodiciteRemboursement(String periodiciteRemboursement) {
        this.periodiciteRemboursement = periodiciteRemboursement;
    }

    public Integer getNombreEcheance() {
        return nombreEcheance;
    }

    public void setNombreEcheance(Integer nombreEcheance) {
        this.nombreEcheance = nombreEcheance;
    }

    public Integer getDelaiGrace() {
        return delaiGrace;
    }

    public void setDelaiGrace(Integer delaiGrace) {
        this.delaiGrace = delaiGrace;
    }

    public Integer getInteretPendantGrace() {
        return interetPendantGrace;
    }

    public void setInteretPendantGrace(Integer interetPendantGrace) {
        this.interetPendantGrace = interetPendantGrace;
    }

    public String getTypeGrace() {
        return typeGrace;
    }

    public void setTypeGrace(String typeGrace) {
        this.typeGrace = typeGrace;
    }

    public BigDecimal getTauxPenalite() {
        return tauxPenalite;
    }

    public void setTauxPenalite(BigDecimal tauxPenalite) {
        this.tauxPenalite = tauxPenalite;
    }

    public String getBaseCalculPenalite() {
        return baseCalculPenalite;
    }

    public void setBaseCalculPenalite(String baseCalculPenalite) {
        this.baseCalculPenalite = baseCalculPenalite;
    }

    public Integer getJoursTolerancePenalite() {
        return joursTolerancePenalite;
    }

    public void setJoursTolerancePenalite(Integer joursTolerancePenalite) {
        this.joursTolerancePenalite = joursTolerancePenalite;
    }

    public BigDecimal getTauxCommission() {
        return tauxCommission;
    }

    public void setTauxCommission(BigDecimal tauxCommission) {
        this.tauxCommission = tauxCommission;
    }

    public String getModePrelevementCommission() {
        return modePrelevementCommission;
    }

    public void setModePrelevementCommission(String modePrelevementCommission) {
        this.modePrelevementCommission = modePrelevementCommission;
    }

    public BigDecimal getMontantCommissionMin() {
        return montantCommissionMin;
    }

    public void setMontantCommissionMin(BigDecimal montantCommissionMin) {
        this.montantCommissionMin = montantCommissionMin;
    }

    public BigDecimal getMontantCommissionMax() {
        return montantCommissionMax;
    }

    public void setMontantCommissionMax(BigDecimal montantCommissionMax) {
        this.montantCommissionMax = montantCommissionMax;
    }

    public BigDecimal getTauxAssurance() {
        return tauxAssurance;
    }

    public void setTauxAssurance(BigDecimal tauxAssurance) {
        this.tauxAssurance = tauxAssurance;
    }

    public BigDecimal getMontantAssuranceMin() {
        return montantAssuranceMin;
    }

    public void setMontantAssuranceMin(BigDecimal montantAssuranceMin) {
        this.montantAssuranceMin = montantAssuranceMin;
    }

    public BigDecimal getTauxEpargneObligatoire() {
        return tauxEpargneObligatoire;
    }

    public void setTauxEpargneObligatoire(BigDecimal tauxEpargneObligatoire) {
        this.tauxEpargneObligatoire = tauxEpargneObligatoire;
    }

    public BigDecimal getTauxGarantieEpargne() {
        return tauxGarantieEpargne;
    }

    public void setTauxGarantieEpargne(BigDecimal tauxGarantieEpargne) {
        this.tauxGarantieEpargne = tauxGarantieEpargne;
    }

    public Integer getDecaissementNet() {
        return decaissementNet;
    }

    public void setDecaissementNet(Integer decaissementNet) {
        this.decaissementNet = decaissementNet;
    }

    public String getCompteCapital() {
        return compteCapital;
    }

    public void setCompteCapital(String compteCapital) {
        this.compteCapital = compteCapital;
    }

    public String getCompteInteret() {
        return compteInteret;
    }

    public void setCompteInteret(String compteInteret) {
        this.compteInteret = compteInteret;
    }

    public String getCompteInteretCouru() {
        return compteInteretCouru;
    }

    public void setCompteInteretCouru(String compteInteretCouru) {
        this.compteInteretCouru = compteInteretCouru;
    }

    public String getCompteCommission() {
        return compteCommission;
    }

    public void setCompteCommission(String compteCommission) {
        this.compteCommission = compteCommission;
    }

    public String getCompteAssurance() {
        return compteAssurance;
    }

    public void setCompteAssurance(String compteAssurance) {
        this.compteAssurance = compteAssurance;
    }

    public String getComptePenalite() {
        return comptePenalite;
    }

    public void setComptePenalite(String comptePenalite) {
        this.comptePenalite = comptePenalite;
    }

    public String getCompteEpargneOblig() {
        return compteEpargneOblig;
    }

    public void setCompteEpargneOblig(String compteEpargneOblig) {
        this.compteEpargneOblig = compteEpargneOblig;
    }

    public String getCompteDeblocage() {
        return compteDeblocage;
    }

    public void setCompteDeblocage(String compteDeblocage) {
        this.compteDeblocage = compteDeblocage;
    }

    public String getCompteProvision() {
        return compteProvision;
    }

    public void setCompteProvision(String compteProvision) {
        this.compteProvision = compteProvision;
    }

    public String getCompteCreanceDouteuse() {
        return compteCreanceDouteuse;
    }

    public void setCompteCreanceDouteuse(String compteCreanceDouteuse) {
        this.compteCreanceDouteuse = compteCreanceDouteuse;
    }

    public Integer getAutoriserReneg() {
        return autoriserReneg;
    }

    public void setAutoriserReneg(Integer autoriserReneg) {
        this.autoriserReneg = autoriserReneg;
    }

    public Integer getAutoriserRemboursementAnticipe() {
        return autoriserRemboursementAnticipe;
    }

    public void setAutoriserRemboursementAnticipe(Integer autoriserRemboursementAnticipe) {
        this.autoriserRemboursementAnticipe = autoriserRemboursementAnticipe;
    }

    public BigDecimal getTauxRembAnticipe() {
        return tauxRembAnticipe;
    }

    public void setTauxRembAnticipe(BigDecimal tauxRembAnticipe) {
        this.tauxRembAnticipe = tauxRembAnticipe;
    }

    public String getTypeClient() {
        return typeClient;
    }

    public void setTypeClient(String typeClient) {
        this.typeClient = typeClient;
    }

    public String getTypeCredit() {
        return typeCredit;
    }

    public void setTypeCredit(String typeCredit) {
        this.typeCredit = typeCredit;
    }

    public Integer getNumeroCycle() {
        return numeroCycle;
    }

    public void setNumeroCycle(Integer numeroCycle) {
        this.numeroCycle = numeroCycle;
    }

    public Integer getGarantieRequise() {
        return garantieRequise;
    }

    public void setGarantieRequise(Integer garantieRequise) {
        this.garantieRequise = garantieRequise;
    }

    public Integer getNombreCreditMax() {
        return nombreCreditMax;
    }

    public void setNombreCreditMax(Integer nombreCreditMax) {
        this.nombreCreditMax = nombreCreditMax;
    }

    public Integer getCapitalisationInteret() {
        return capitalisationInteret;
    }

    public void setCapitalisationInteret(Integer capitalisationInteret) {
        this.capitalisationInteret = capitalisationInteret;
    }

    public Integer getSeuilRetard1() {
        return seuilRetard1;
    }

    public void setSeuilRetard1(Integer seuilRetard1) {
        this.seuilRetard1 = seuilRetard1;
    }

    public Integer getSeuilRetard2() {
        return seuilRetard2;
    }

    public void setSeuilRetard2(Integer seuilRetard2) {
        this.seuilRetard2 = seuilRetard2;
    }

    public Integer getSeuilRetard3() {
        return seuilRetard3;
    }

    public void setSeuilRetard3(Integer seuilRetard3) {
        this.seuilRetard3 = seuilRetard3;
    }

    public String getNumproduitPourEpargneOblig() {
        return numproduitPourEpargneOblig;
    }

    public void setNumproduitPourEpargneOblig(String numproduitPourEpargneOblig) {
        this.numproduitPourEpargneOblig = numproduitPourEpargneOblig;
    }

    public String getNumproduitPourGaranti() {
        return numproduitPourGaranti;
    }

    public void setNumproduitPourGaranti(String numproduitPourGaranti) {
        this.numproduitPourGaranti = numproduitPourGaranti;
    }

    public String getNumproduitPourPdrtLieCrdit() {
        return numproduitPourPdrtLieCrdit;
    }

    public void setNumproduitPourPdrtLieCrdit(String numproduitPourPdrtLieCrdit) {
        this.numproduitPourPdrtLieCrdit = numproduitPourPdrtLieCrdit;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public FamilleProduitCredit getFamilleProduitCredit() {
        return familleProduitCredit;
    }

    public void setFamilleProduitCredit(FamilleProduitCredit familleProduitCredit) {
        this.familleProduitCredit = familleProduitCredit;
    }

    public ModeDeCalculInteret getModeDeCalculInteret() {
        return modeDeCalculInteret;
    }

    public void setModeDeCalculInteret(ModeDeCalculInteret modeDeCalculInteret) {
        this.modeDeCalculInteret = modeDeCalculInteret;
    }

    public ModelCompteProduitCredit getModelCompteProduitCredit() {
        return modelCompteProduitCredit;
    }

    public void setModelCompteProduitCredit(ModelCompteProduitCredit modelCompteProduitCredit) {
        this.modelCompteProduitCredit = modelCompteProduitCredit;
    }

    public ProduitIslamic getProduitIslamic() {
        return produitIslamic;
    }

    public void setProduitIslamic(ProduitIslamic produitIslamic) {
        this.produitIslamic = produitIslamic;
    }

    @Override
    public String toString() {
        return "ProduitCredit("
            + "numProduit=" + numProduit
            + ")";
    }
}

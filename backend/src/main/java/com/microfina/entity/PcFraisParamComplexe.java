package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * PcFraisParamComplexe – tiered fee parameter grid for a credit product.
 *
 * Each row defines one cell in the fee matrix: the applicable fee value
 * for a given (product, fee type, amount bracket, duration bracket) combination.
 * Multiple rows together express stepped/tiered fee schedules.
 *
 * DDL source of truth: P3-006-CREATE-TABLE-PCFRAISPARAMCOMPLEXE.xml.
 * Spec: Phase 3, Section 1.
 */
@Entity
@Table(name = "PCFRAISPARAMCOMPLEXE")
@DynamicUpdate
public class PcFraisParamComplexe implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDPCFRAISPARAMCOMPLEXE", nullable = false)
    private Long idPcFraisParamComplexe;

    // ── Fee identity ──────────────────────────────────────────────

    /**
     * Fee type code.
     * e.g. COM=Commission, ASS=Assurance, PEN=Pénalité, FRA=Frais divers.
     */
    @Size(max = 50)
    @Column(name = "CODE_TYPE_FRAIS", length = 50)
    private String codeTypeFrais;

    @Size(max = 255)
    @Column(name = "LIBELLE_FRAIS", length = 255)
    private String libelleFrais;

    // ── Amount bracket ────────────────────────────────────────────

    @Column(name = "MONTANT_MIN_TRANCHE", precision = 19, scale = 4)
    private BigDecimal montantMinTranche;

    @Column(name = "MONTANT_MAX_TRANCHE", precision = 19, scale = 4)
    private BigDecimal montantMaxTranche;

    // ── Duration bracket (months) ─────────────────────────────────

    @Column(name = "DUREE_MIN_TRANCHE")
    private Integer dureeMinTranche;

    @Column(name = "DUREE_MAX_TRANCHE")
    private Integer dureeMaxTranche;

    // ── Fee value ─────────────────────────────────────────────────

    /** Rate (%) when MODECALCUL_FRAIS = "TAUX". */
    @Column(name = "TAUX_FRAIS", precision = 19, scale = 4)
    private BigDecimal tauxFrais;

    /** Fixed amount when MODECALCUL_FRAIS = "MONTANT_FIXE". */
    @Column(name = "MONTANT_FRAIS", precision = 19, scale = 4)
    private BigDecimal montantFrais;

    /**
     * Calculation method.
     * TAUX | MONTANT_FIXE | MAX(TAUX,MONTANT_FIXE).
     */
    @Size(max = 30)
    @Column(name = "MODECALCUL_FRAIS", length = 30)
    private String modeCalculFrais;

    /** Floor – minimum fee amount. */
    @Column(name = "MONTANT_MIN_FRAIS", precision = 19, scale = 4)
    private BigDecimal montantMinFrais;

    /** Cap – maximum fee amount. */
    @Column(name = "MONTANT_MAX_FRAIS", precision = 19, scale = 4)
    private BigDecimal montantMaxFrais;

    // ── Collection timing ─────────────────────────────────────────

    /**
     * When the fee is collected.
     * DEBUT=at disbursement, ECHEANCE=per instalment, FIN=at final repayment.
     */
    @Size(max = 30)
    @Column(name = "PERIODICITE_PRELEVEMENT", length = 30)
    private String periodicitePrelevement;

    /** Accounting account for this fee line. */
    @Size(max = 30)
    @Column(name = "COMPTE_COMPTABLE", length = 30)
    private String compteComptable;

    /** 1 = row is active. */
    @Column(name = "ACTIF")
    private Integer actif = 1;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── FK ────────────────────────────────────────────────────────

    /**
     * Credit product this fee grid applies to.
     * FK to produitcredit(NUMPRODUIT).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "numproduit",
        referencedColumnName = "NUMPRODUIT",
        foreignKey           = @ForeignKey(name = "FK_PCFRAISPARAMCOMPLEXE_produitcredit")
    )
    private ProduitCredit produitCredit;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public PcFraisParamComplexe() {
    }

    public PcFraisParamComplexe(Long idPcFraisParamComplexe, String codeTypeFrais, String libelleFrais, BigDecimal montantMinTranche, BigDecimal montantMaxTranche, Integer dureeMinTranche, Integer dureeMaxTranche, BigDecimal tauxFrais, BigDecimal montantFrais, String modeCalculFrais, BigDecimal montantMinFrais, BigDecimal montantMaxFrais, String periodicitePrelevement, String compteComptable, Integer actif, Integer version, ProduitCredit produitCredit) {
        this.idPcFraisParamComplexe = idPcFraisParamComplexe;
        this.codeTypeFrais = codeTypeFrais;
        this.libelleFrais = libelleFrais;
        this.montantMinTranche = montantMinTranche;
        this.montantMaxTranche = montantMaxTranche;
        this.dureeMinTranche = dureeMinTranche;
        this.dureeMaxTranche = dureeMaxTranche;
        this.tauxFrais = tauxFrais;
        this.montantFrais = montantFrais;
        this.modeCalculFrais = modeCalculFrais;
        this.montantMinFrais = montantMinFrais;
        this.montantMaxFrais = montantMaxFrais;
        this.periodicitePrelevement = periodicitePrelevement;
        this.compteComptable = compteComptable;
        this.actif = actif;
        this.version = version;
        this.produitCredit = produitCredit;
    }

    public Long getIdPcFraisParamComplexe() {
        return idPcFraisParamComplexe;
    }

    public void setIdPcFraisParamComplexe(Long idPcFraisParamComplexe) {
        this.idPcFraisParamComplexe = idPcFraisParamComplexe;
    }

    public String getCodeTypeFrais() {
        return codeTypeFrais;
    }

    public void setCodeTypeFrais(String codeTypeFrais) {
        this.codeTypeFrais = codeTypeFrais;
    }

    public String getLibelleFrais() {
        return libelleFrais;
    }

    public void setLibelleFrais(String libelleFrais) {
        this.libelleFrais = libelleFrais;
    }

    public BigDecimal getMontantMinTranche() {
        return montantMinTranche;
    }

    public void setMontantMinTranche(BigDecimal montantMinTranche) {
        this.montantMinTranche = montantMinTranche;
    }

    public BigDecimal getMontantMaxTranche() {
        return montantMaxTranche;
    }

    public void setMontantMaxTranche(BigDecimal montantMaxTranche) {
        this.montantMaxTranche = montantMaxTranche;
    }

    public Integer getDureeMinTranche() {
        return dureeMinTranche;
    }

    public void setDureeMinTranche(Integer dureeMinTranche) {
        this.dureeMinTranche = dureeMinTranche;
    }

    public Integer getDureeMaxTranche() {
        return dureeMaxTranche;
    }

    public void setDureeMaxTranche(Integer dureeMaxTranche) {
        this.dureeMaxTranche = dureeMaxTranche;
    }

    public BigDecimal getTauxFrais() {
        return tauxFrais;
    }

    public void setTauxFrais(BigDecimal tauxFrais) {
        this.tauxFrais = tauxFrais;
    }

    public BigDecimal getMontantFrais() {
        return montantFrais;
    }

    public void setMontantFrais(BigDecimal montantFrais) {
        this.montantFrais = montantFrais;
    }

    public String getModeCalculFrais() {
        return modeCalculFrais;
    }

    public void setModeCalculFrais(String modeCalculFrais) {
        this.modeCalculFrais = modeCalculFrais;
    }

    public BigDecimal getMontantMinFrais() {
        return montantMinFrais;
    }

    public void setMontantMinFrais(BigDecimal montantMinFrais) {
        this.montantMinFrais = montantMinFrais;
    }

    public BigDecimal getMontantMaxFrais() {
        return montantMaxFrais;
    }

    public void setMontantMaxFrais(BigDecimal montantMaxFrais) {
        this.montantMaxFrais = montantMaxFrais;
    }

    public String getPeriodicitePrelevement() {
        return periodicitePrelevement;
    }

    public void setPeriodicitePrelevement(String periodicitePrelevement) {
        this.periodicitePrelevement = periodicitePrelevement;
    }

    public String getCompteComptable() {
        return compteComptable;
    }

    public void setCompteComptable(String compteComptable) {
        this.compteComptable = compteComptable;
    }

    public Integer getActif() {
        return actif;
    }

    public void setActif(Integer actif) {
        this.actif = actif;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public ProduitCredit getProduitCredit() {
        return produitCredit;
    }

    public void setProduitCredit(ProduitCredit produitCredit) {
        this.produitCredit = produitCredit;
    }

    @Override
    public String toString() {
        return "PcFraisParamComplexe("
            + "idPcFraisParamComplexe=" + idPcFraisParamComplexe
            + ")";
    }
}

package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Reglement – header record for a credit repayment transaction.
 *
 * One Reglement represents a single payment event (cash, wire, cheque, etc.)
 * and aggregates the total amount received. The allocation of that amount to
 * specific {@link Amortp} instalments is stored in {@link DetailReglement}.
 *
 * On posting a Reglement:
 *  1. {@link DetailReglement} rows are created (one per Amortp being settled).
 *  2. Each Amortp row's *_REMBOURSE / *_REGLEE columns are incremented.
 *  3. The parent {@link Credits} SOLDE_* balances are decremented.
 *  4. A {@link Comptabilite} journal entry is created for the disbursement /
 *     repayment event (idcomptabilite FK).
 *
 * DDL source of truth: P4-011-CREATE-TABLE-Reglement.xml.
 * Spec: Phase 4, Section 10.
 */
@Entity
@Table(name = "Reglement")
@DynamicUpdate
public class Reglement implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDREGLEMENT", nullable = false)
    private Long idReglement;

    // ── Transaction reference ─────────────────────────────────────

    @Size(max = 50)
    @Column(name = "NUMPIECE", length = 50)
    private String numPiece;

    @Column(name = "DATE_REGLEMENT", nullable = false)
    private LocalDate dateReglement;

    // ── Breakdown of amount received ──────────────────────────────

    @Column(name = "MONTANT_TOTAL", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantTotal;

    @Column(name = "MONTANT_CAPITAL", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantCapital = BigDecimal.ZERO;

    @Column(name = "MONTANT_INTERET", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantInteret = BigDecimal.ZERO;

    @Column(name = "MONTANT_PENALITE", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantPenalite = BigDecimal.ZERO;

    @Column(name = "MONTANT_ASSURANCE", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantAssurance = BigDecimal.ZERO;

    @Column(name = "MONTANT_COMMISSION", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantCommission = BigDecimal.ZERO;

    /** Tax amount (TVA / VAT) collected within this payment. */
    @Column(name = "MONTANT_TAXE", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantTaxe = BigDecimal.ZERO;

    // ── Payment mode ──────────────────────────────────────────────

    /**
     * ESPECES | VIREMENT | CHEQUE | MOBILE_MONEY | PRELEVEMENT
     */
    @Size(max = 30)
    @Column(name = "MODE_PAIEMENT", length = 30)
    private String modePaiement;

    // ── Status ────────────────────────────────────────────────────

    /** VALIDE | ANNULE | EN_ATTENTE. */
    @Size(max = 20)
    @Column(name = "STATUT", length = 20, nullable = false)
    private String statut = "VALIDE";

    // ── Comptabilite linkage ──────────────────────────────────────

    /**
     * ID of the journal entry created for this repayment.
     * NOT NULL: every Reglement must produce a corresponding Comptabilite entry
     * before it can be persisted (Mauritanian financial-integrity requirement).
     */
    @Column(name = "idcomptabilite", nullable = false)
    private Long idComptabilite;

    // ── Audit ─────────────────────────────────────────────────────

    @Size(max = 48)
    @Column(name = "utilisateur", length = 48)
    private String utilisateur;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── FKs ───────────────────────────────────────────────────────

    /**
     * Credit being repaid.
     * FK to Credits(IDCREDIT).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "idcredit",
        referencedColumnName = "IDCREDIT",
        foreignKey           = @ForeignKey(name = "FK_Reglement_Credits")
    )
    private Credits credit;

    /**
     * Branch where the payment was received.
     * FK to AGENCE(CODE_AGENCE).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "agence",
        referencedColumnName = "CODE_AGENCE",
        foreignKey           = @ForeignKey(name = "FK_Reglement_AGENCE")
    )
    private Agence agence;

    /**
     * Journal entry created for this repayment (Comptabilite).
     * FK to comptabilite(IDCOMPTABILITE). Wired via idComptabilite raw column.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "idcomptabilite",
        referencedColumnName = "IDCOMPTABILITE",
        insertable           = false,
        updatable            = false,
        foreignKey           = @ForeignKey(name = "FK_Reglement_comptabilite")
    )
    private Comptabilite comptabilite;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public Reglement() {
    }

    public Reglement(Long idReglement, String numPiece, LocalDate dateReglement, BigDecimal montantTotal, BigDecimal montantCapital, BigDecimal montantInteret, BigDecimal montantPenalite, BigDecimal montantAssurance, BigDecimal montantCommission, BigDecimal montantTaxe, String modePaiement, String statut, Long idComptabilite, String utilisateur, Integer version, Credits credit, Agence agence, Comptabilite comptabilite) {
        this.idReglement = idReglement;
        this.numPiece = numPiece;
        this.dateReglement = dateReglement;
        this.montantTotal = montantTotal;
        this.montantCapital = montantCapital;
        this.montantInteret = montantInteret;
        this.montantPenalite = montantPenalite;
        this.montantAssurance = montantAssurance;
        this.montantCommission = montantCommission;
        this.montantTaxe = montantTaxe;
        this.modePaiement = modePaiement;
        this.statut = statut;
        this.idComptabilite = idComptabilite;
        this.utilisateur = utilisateur;
        this.version = version;
        this.credit = credit;
        this.agence = agence;
        this.comptabilite = comptabilite;
    }

    public Long getIdReglement() {
        return idReglement;
    }

    public void setIdReglement(Long idReglement) {
        this.idReglement = idReglement;
    }

    public String getNumPiece() {
        return numPiece;
    }

    public void setNumPiece(String numPiece) {
        this.numPiece = numPiece;
    }

    public LocalDate getDateReglement() {
        return dateReglement;
    }

    public void setDateReglement(LocalDate dateReglement) {
        this.dateReglement = dateReglement;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public BigDecimal getMontantCapital() {
        return montantCapital;
    }

    public void setMontantCapital(BigDecimal montantCapital) {
        this.montantCapital = montantCapital;
    }

    public BigDecimal getMontantInteret() {
        return montantInteret;
    }

    public void setMontantInteret(BigDecimal montantInteret) {
        this.montantInteret = montantInteret;
    }

    public BigDecimal getMontantPenalite() {
        return montantPenalite;
    }

    public void setMontantPenalite(BigDecimal montantPenalite) {
        this.montantPenalite = montantPenalite;
    }

    public BigDecimal getMontantAssurance() {
        return montantAssurance;
    }

    public void setMontantAssurance(BigDecimal montantAssurance) {
        this.montantAssurance = montantAssurance;
    }

    public BigDecimal getMontantCommission() {
        return montantCommission;
    }

    public void setMontantCommission(BigDecimal montantCommission) {
        this.montantCommission = montantCommission;
    }

    public BigDecimal getMontantTaxe() {
        return montantTaxe;
    }

    public void setMontantTaxe(BigDecimal montantTaxe) {
        this.montantTaxe = montantTaxe;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Long getIdComptabilite() {
        return idComptabilite;
    }

    public void setIdComptabilite(Long idComptabilite) {
        this.idComptabilite = idComptabilite;
    }

    public String getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(String utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Credits getCredit() {
        return credit;
    }

    public void setCredit(Credits credit) {
        this.credit = credit;
    }

    public Agence getAgence() {
        return agence;
    }

    public void setAgence(Agence agence) {
        this.agence = agence;
    }

    public Comptabilite getComptabilite() {
        return comptabilite;
    }

    public void setComptabilite(Comptabilite comptabilite) {
        this.comptabilite = comptabilite;
    }

    @Override
    public String toString() {
        return "Reglement("
            + "idReglement=" + idReglement
            + ")";
    }
}

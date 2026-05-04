package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ClotureJournaliere – enregistrement de clôture de journée comptable.
 *
 * Une ligne par (DATE_CLOTURE, CODE_AGENCE) ; la contrainte unique
 * {@code UQ_cloture_journaliere_date_agence} empêche toute double
 * clôture d'une même journée sur la même agence.
 *
 * Lifecycle STATUT :
 *   CLOTURE  – journée clôturée normalement par {@link ClotureService}
 *   ANNULE   – annulée après clôture (opération superviseur)
 *   REOUVERT – temporairement rouverte pour correction d'écriture
 *
 * Les colonnes MONTANT_* reproduisent les totaux que le ClotureService
 * calcule en agrégeant les {@link Reglement} de la journée.
 * MONTANT_TAXE est inclus conformément à P4-013.
 *
 * DDL source of truth: P5-003-CREATE-TABLE-cloture_journaliere.xml.
 * Spec: Phase 5, Section 4 (Clôture Journalière).
 */
@Entity
@Table(
    name = "cloture_journaliere",
    uniqueConstraints = @UniqueConstraint(
        name        = "UQ_cloture_journaliere_date_agence",
        columnNames = {"DATE_CLOTURE", "CODE_AGENCE"}
    ),
    indexes = {
        @Index(name = "IDX_cloture_journaliere_DATE_CLOTURE", columnList = "DATE_CLOTURE")
    }
)
@DynamicUpdate
public class ClotureJournaliere implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDCLOTURE", nullable = false)
    private Long idCloture;

    // ── Clé métier ────────────────────────────────────────────────

    /** Date de la journée comptable clôturée. */
    @NotNull
    @Column(name = "DATE_CLOTURE", nullable = false)
    private LocalDate dateCloture;

    /** Horodatage précis de l'opération de clôture (heure serveur). */
    @NotNull
    @Column(name = "DATE_HEURE_CLOTURE", nullable = false)
    private LocalDateTime dateHeureCloture;

    // ── Statut ────────────────────────────────────────────────────

    /**
     * État de la journée :
     *   CLOTURE  | ANNULE | REOUVERT
     */
    @NotBlank
    @Size(max = 20)
    @Column(name = "STATUT", length = 20, nullable = false)
    private String statut = "CLOTURE";

    // ── Audit ─────────────────────────────────────────────────────

    /** Identifiant de l'agent ayant déclenché la clôture. */
    @NotBlank
    @Size(max = 48)
    @Column(name = "UTILISATEUR", length = 48, nullable = false)
    private String utilisateur;

    /** Motif de réouverture ou d'annulation (null pour une clôture normale). */
    @Size(max = 500)
    @Column(name = "OBSERVATIONS", length = 500)
    private String observations;

    // ── Synthèse des flux de la journée ──────────────────────────

    /** Nombre de remboursements (Reglement) traités dans la journée. */
    @Column(name = "NB_REGLEMENTS", nullable = false)
    private Integer nbReglements = 0;

    /** Somme de MONTANT_TOTAL de tous les Reglements de la journée. */
    @Column(name = "MONTANT_TOTAL", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantTotal = BigDecimal.ZERO;

    /** Portion capital des remboursements journaliers. */
    @Column(name = "MONTANT_CAPITAL", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantCapital = BigDecimal.ZERO;

    /** Portion intérêts des remboursements journaliers. */
    @Column(name = "MONTANT_INTERET", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantInteret = BigDecimal.ZERO;

    /** Portion pénalités des remboursements journaliers. */
    @Column(name = "MONTANT_PENALITE", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantPenalite = BigDecimal.ZERO;

    /** Portion assurance des remboursements journaliers. */
    @Column(name = "MONTANT_ASSURANCE", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantAssurance = BigDecimal.ZERO;

    /** Portion commissions des remboursements journaliers. */
    @Column(name = "MONTANT_COMMISSION", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantCommission = BigDecimal.ZERO;

    /**
     * Portion taxes (TVA) des remboursements journaliers.
     * Requis par la réglementation fiscale mauritanienne (P4-013).
     */
    @Column(name = "MONTANT_TAXE", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantTaxe = BigDecimal.ZERO;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── FK ────────────────────────────────────────────────────────

    /**
     * Agence concernée par la clôture.
     * NULL = clôture centrale couvrant toutes les agences.
     * FK to AGENCE(CODE_AGENCE) – ON DELETE SET NULL.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "CODE_AGENCE",
        referencedColumnName = "CODE_AGENCE",
        nullable             = true,
        foreignKey           = @ForeignKey(name = "FK_cloture_journaliere_AGENCE")
    )
    private Agence agence;

    // ── Helpers ───────────────────────────────────────────────────

    /** Retourne true si la journée est dans l'état CLOTURE. */
    public boolean isCloture() {
        return "CLOTURE".equals(statut);
    }

    /** Retourne true si la journée a été réouverte ou annulée. */
    public boolean isReouverte() {
        return "REOUVERT".equals(statut) || "ANNULE".equals(statut);
    }

    /**
     * Calcule les revenus totaux (intérêts + commissions + taxes) de la journée.
     * Utilisé par les rapports d'exploitation et les états BCM.
     */
    public BigDecimal getTotalRevenus() {
        return montantInteret
               .add(montantCommission)
               .add(montantTaxe);
    }

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public ClotureJournaliere() {
    }

    public ClotureJournaliere(Long idCloture, LocalDate dateCloture, LocalDateTime dateHeureCloture, String statut, String utilisateur, String observations, Integer nbReglements, BigDecimal montantTotal, BigDecimal montantCapital, BigDecimal montantInteret, BigDecimal montantPenalite, BigDecimal montantAssurance, BigDecimal montantCommission, BigDecimal montantTaxe, Integer version, Agence agence) {
        this.idCloture = idCloture;
        this.dateCloture = dateCloture;
        this.dateHeureCloture = dateHeureCloture;
        this.statut = statut;
        this.utilisateur = utilisateur;
        this.observations = observations;
        this.nbReglements = nbReglements;
        this.montantTotal = montantTotal;
        this.montantCapital = montantCapital;
        this.montantInteret = montantInteret;
        this.montantPenalite = montantPenalite;
        this.montantAssurance = montantAssurance;
        this.montantCommission = montantCommission;
        this.montantTaxe = montantTaxe;
        this.version = version;
        this.agence = agence;
    }

    public Long getIdCloture() {
        return idCloture;
    }

    public void setIdCloture(Long idCloture) {
        this.idCloture = idCloture;
    }

    public LocalDate getDateCloture() {
        return dateCloture;
    }

    public void setDateCloture(LocalDate dateCloture) {
        this.dateCloture = dateCloture;
    }

    public LocalDateTime getDateHeureCloture() {
        return dateHeureCloture;
    }

    public void setDateHeureCloture(LocalDateTime dateHeureCloture) {
        this.dateHeureCloture = dateHeureCloture;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(String utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public Integer getNbReglements() {
        return nbReglements;
    }

    public void setNbReglements(Integer nbReglements) {
        this.nbReglements = nbReglements;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Agence getAgence() {
        return agence;
    }

    public void setAgence(Agence agence) {
        this.agence = agence;
    }

    @Override
    public String toString() {
        return "ClotureJournaliere("
            + "idCloture=" + idCloture
            + ", dateCloture=" + dateCloture
            + ")";
    }
}

package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * HistoriqueVisaCredit – immutable audit trail of credit workflow transitions.
 *
 * One row is appended every time a credit moves from one {@link CreditStatut}
 * to another (SAISIE, VALIDATION_AGENT, VALIDATION_COMITE, DEBLOCAGE, etc.).
 * Rows are never updated after insertion.
 *
 * DDL source of truth: P4-004-CREATE-TABLE-historique_visa_credit.xml.
 * Spec: Phase 4, Section 9 (Workflow / Visa).
 */
@Entity
@Table(name = "historique_visa_credit")
@DynamicUpdate
public class HistoriqueVisaCredit implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDHISTORIQUE", nullable = false)
    private Long idHistorique;

    // ── Workflow step ─────────────────────────────────────────────

    /**
     * Label of the workflow step.
     * SAISIE | ANALYSE | VALIDATION_AGENT | VALIDATION_COMITE |
     * DEBLOCAGE | REMBOURSEMENT | CLOTURE
     */
    @Size(max = 50)
    @Column(name = "ETAPE", length = 50)
    private String etape;

    /** Credit status before this transition. */
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUT_AVANT", length = 20)
    private CreditStatut statutAvant;

    /** Credit status after this transition. */
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUT_APRES", length = 20)
    private CreditStatut statutApres;

    /** Date and time the visa / approval action was recorded. */
    @Column(name = "DATE_VISA")
    private LocalDate dateVisa;

    /**
     * Committee or individual decision.
     * APPROUVE | REFUSE | EN_ATTENTE
     */
    @Size(max = 20)
    @Column(name = "DECISION", length = 20)
    private String decision;

    /** Free-text comment or rejection reason. */
    @Size(max = 500)
    @Column(name = "COMMENTAIRE", length = 500)
    private String commentaire;

    /** Username or committee ID that issued the visa. */
    @Size(max = 48)
    @Column(name = "UTILISATEUR", length = 48)
    private String utilisateur;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── FK ────────────────────────────────────────────────────────

    /**
     * The credit this visa entry belongs to.
     * FK to Credits(IDCREDIT).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "idcredit",
        referencedColumnName = "IDCREDIT",
        foreignKey           = @ForeignKey(name = "FK_historique_visa_credit_Credits")
    )
    private Credits credit;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public HistoriqueVisaCredit() {
    }

    public HistoriqueVisaCredit(Long idHistorique, String etape, CreditStatut statutAvant, CreditStatut statutApres, LocalDate dateVisa, String decision, String commentaire, String utilisateur, Integer version, Credits credit) {
        this.idHistorique = idHistorique;
        this.etape = etape;
        this.statutAvant = statutAvant;
        this.statutApres = statutApres;
        this.dateVisa = dateVisa;
        this.decision = decision;
        this.commentaire = commentaire;
        this.utilisateur = utilisateur;
        this.version = version;
        this.credit = credit;
    }

    public Long getIdHistorique() {
        return idHistorique;
    }

    public void setIdHistorique(Long idHistorique) {
        this.idHistorique = idHistorique;
    }

    public String getEtape() {
        return etape;
    }

    public void setEtape(String etape) {
        this.etape = etape;
    }

    public CreditStatut getStatutAvant() {
        return statutAvant;
    }

    public void setStatutAvant(CreditStatut statutAvant) {
        this.statutAvant = statutAvant;
    }

    public CreditStatut getStatutApres() {
        return statutApres;
    }

    public void setStatutApres(CreditStatut statutApres) {
        this.statutApres = statutApres;
    }

    public LocalDate getDateVisa() {
        return dateVisa;
    }

    public void setDateVisa(LocalDate dateVisa) {
        this.dateVisa = dateVisa;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
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

    @Override
    public String toString() {
        return "HistoriqueVisaCredit("
            + "idHistorique=" + idHistorique
            + ")";
    }
}

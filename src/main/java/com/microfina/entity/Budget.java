package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Budget – enveloppe budgétaire annuelle d'une agence.
 * <p>
 * Un budget est créé pour un exercice fiscal (année) et une agence donnée.
 * Il regroupe un ensemble de {@link LigneBudget} ventilées en recettes et dépenses.
 * Le cycle de vie est piloté par {@link StatutBudget} :
 * BROUILLON → VALIDE → CLOTURE.
 * </p>
 * <p>
 * Les champs {@code montantTotalRecettes} et {@code montantTotalDepenses}
 * sont des agrégats dénormalisés mis à jour à chaque validation de ligne.
 * </p>
 *
 * DDL source of truth: P8-001-CREATE-TABLE-Budget.xml.
 * Spec: cahier §3.1.1 (Budget).
 */
@Entity
@Table(
    name = "Budget",
    indexes = {
        @Index(name = "IDX_Budget_exercice_agence", columnList = "exercice_fiscal, code_agence")
    }
)
@DynamicUpdate
public class Budget implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // ── Champs métier ─────────────────────────────────────────────

    /**
     * Année de l'exercice fiscal (ex. 2024).
     * INT NOT NULL.
     */
    @NotNull
    @Column(name = "exercice_fiscal", nullable = false)
    private Integer exerciceFiscal;

    /**
     * Date de création du budget.
     * DATE NOT NULL.
     */
    @NotNull
    @Column(name = "date_creation", nullable = false)
    private LocalDate dateCreation;

    /**
     * Date de validation du budget. NULL tant que le statut est BROUILLON.
     * DATE NULL.
     */
    @Column(name = "date_validation")
    private LocalDate dateValidation;

    /**
     * Statut du budget dans son cycle de vie.
     * NVARCHAR(20) NOT NULL DEFAULT 'BROUILLON'.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private StatutBudget statut = StatutBudget.BROUILLON;

    /**
     * Somme des montants prévisionnels de toutes les lignes de type RECETTE.
     * DECIMAL(19,4) NOT NULL DEFAULT 0.
     */
    @NotNull
    @Column(name = "montant_total_recettes", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantTotalRecettes = BigDecimal.ZERO;

    /**
     * Somme des montants prévisionnels de toutes les lignes de type DEPENSE.
     * DECIMAL(19,4) NOT NULL DEFAULT 0.
     */
    @NotNull
    @Column(name = "montant_total_depenses", precision = 19, scale = 4, nullable = false)
    private BigDecimal montantTotalDepenses = BigDecimal.ZERO;

    /**
     * Identifiant de l'utilisateur ayant créé ou modifié le budget.
     * NVARCHAR(100) NULL.
     */
    @Size(max = 100)
    @Column(name = "utilisateur", length = 100)
    private String utilisateur;

    // ── Optimistic locking ────────────────────────────────────────

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    // ── Relations ─────────────────────────────────────────────────

    /**
     * Agence propriétaire de ce budget.
     * FK_Budget_Agence → AGENCE(CODE_AGENCE). NULLABLE (budget inter-agences possible).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "code_agence",
        referencedColumnName = "CODE_AGENCE",
        foreignKey = @ForeignKey(name = "FK_Budget_Agence")
    )
    private Agence agence;

    // ── Constructeurs ─────────────────────────────────────────────

    public Budget() {
    }

    // ── Getters / Setters ─────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getExerciceFiscal() {
        return exerciceFiscal;
    }

    public void setExerciceFiscal(Integer exerciceFiscal) {
        this.exerciceFiscal = exerciceFiscal;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDate getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(LocalDate dateValidation) {
        this.dateValidation = dateValidation;
    }

    public StatutBudget getStatut() {
        return statut;
    }

    public void setStatut(StatutBudget statut) {
        this.statut = statut;
    }

    public BigDecimal getMontantTotalRecettes() {
        return montantTotalRecettes;
    }

    public void setMontantTotalRecettes(BigDecimal montantTotalRecettes) {
        this.montantTotalRecettes = montantTotalRecettes;
    }

    public BigDecimal getMontantTotalDepenses() {
        return montantTotalDepenses;
    }

    public void setMontantTotalDepenses(BigDecimal montantTotalDepenses) {
        this.montantTotalDepenses = montantTotalDepenses;
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

    public Agence getAgence() {
        return agence;
    }

    public void setAgence(Agence agence) {
        this.agence = agence;
    }
}

package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * OperationBanque – entité racine (JOINED) de toutes les opérations bancaires.
 *
 * Sous-types :
 * <ul>
 *   <li>{@link Virement}      – virement intra ou inter-bancaire</li>
 *   <li>{@link RemiseCheque}  – remise d'un chèque à l'encaissement</li>
 *   <li>{@link RetraitBanque} – retrait sur compte bancaire</li>
 *   <li>{@link DepotBanque}   – dépôt sur compte bancaire</li>
 * </ul>
 *
 * Chaque opération génère une écriture comptable ({@link Comptabilite}).
 * Le statut suit : EN_ATTENTE → VALIDE ou ANNULE.
 *
 * DDL source of truth: P6-009-CREATE-TABLE-OperationBanque.xml.
 * Spec: cahier §6 (Module Banque – opérations bancaires).
 */
@Entity
@Table(name = "OperationBanque")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(
    name              = "DTYPE",
    discriminatorType = DiscriminatorType.STRING,
    columnDefinition  = "NVARCHAR(31)"
)
@DiscriminatorValue("OperationBanque")
@DynamicUpdate
public class OperationBanque implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // ── Champs métier ─────────────────────────────────────────────

    /** Date de l'opération. */
    @Column(name = "date_operation", nullable = false)
    private LocalDate dateOperation;

    /** Montant de l'opération. */
    @Column(name = "montant", precision = 19, scale = 4, nullable = false)
    private BigDecimal montant;

    /** État courant de l'opération. */
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 15, nullable = false)
    private StatutOperationBanque statut = StatutOperationBanque.EN_ATTENTE;

    /** Identifiant de l'utilisateur ayant saisi l'opération. */
    @Size(max = 48)
    @Column(name = "utilisateur", length = 48)
    private String utilisateur;

    /**
     * Code banque choisi à la saisie (référentiel {@code Banque.code_banque}),
     * conservé pour l'affichage même sans {@link CompteBanque} résolu.
     */
    @Size(max = 20)
    @Column(name = "code_banque_saisie", length = 20)
    private String codeBanqueSaisie;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "version")
    private Integer version = 0;

    // ── Associations ──────────────────────────────────────────────

    /**
     * Compte bancaire concerné par l'opération.
     * FK vers CompteBanque(id).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name       = "compte_banque_id",
        foreignKey = @ForeignKey(name = "FK_OperationBanque_CompteBanque")
    )
    private CompteBanque compteBanque;

    /**
     * Agence qui a initié l'opération.
     * FK vers AGENCE(CODE_AGENCE).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name       = "code_agence",
        foreignKey = @ForeignKey(name = "FK_OperationBanque_Agence")
    )
    private Agence agence;

    /**
     * Écriture comptable générée par l'opération.
     * FK vers comptabilite(IDCOMPTABILITE).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name       = "idcomptabilite",
        nullable   = false,
        foreignKey = @ForeignKey(name = "FK_OperationBanque_Comptabilite")
    )
    private Comptabilite comptabilite;

    // ── Constructeurs ─────────────────────────────────────────────

    public OperationBanque() {
    }

    public OperationBanque(Long id, LocalDate dateOperation, BigDecimal montant,
                           StatutOperationBanque statut, String utilisateur,
                           Integer version, CompteBanque compteBanque,
                           Agence agence, Comptabilite comptabilite) {
        this.id = id;
        this.dateOperation = dateOperation;
        this.montant = montant;
        this.statut = statut;
        this.utilisateur = utilisateur;
        this.version = version;
        this.compteBanque = compteBanque;
        this.agence = agence;
        this.comptabilite = comptabilite;
    }

    // ── Accesseurs ────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDateOperation() { return dateOperation; }
    public void setDateOperation(LocalDate dateOperation) { this.dateOperation = dateOperation; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public StatutOperationBanque getStatut() { return statut; }
    public void setStatut(StatutOperationBanque statut) { this.statut = statut; }

    public String getUtilisateur() { return utilisateur; }
    public void setUtilisateur(String utilisateur) { this.utilisateur = utilisateur; }

    public String getCodeBanqueSaisie() { return codeBanqueSaisie; }
    public void setCodeBanqueSaisie(String codeBanqueSaisie) { this.codeBanqueSaisie = codeBanqueSaisie; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public CompteBanque getCompteBanque() { return compteBanque; }
    public void setCompteBanque(CompteBanque compteBanque) { this.compteBanque = compteBanque; }

    public Agence getAgence() { return agence; }
    public void setAgence(Agence agence) { this.agence = agence; }

    public Comptabilite getComptabilite() { return comptabilite; }
    public void setComptabilite(Comptabilite comptabilite) { this.comptabilite = comptabilite; }

    @Override
    public String toString() {
        return "OperationBanque(id=" + id + ", dateOperation=" + dateOperation
            + ", montant=" + montant + ")";
    }
}

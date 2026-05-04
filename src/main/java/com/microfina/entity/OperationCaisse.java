package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * OperationCaisse – entité racine (JOINED) de toutes les opérations de caisse.
 *
 * Sous-types :
 * <ul>
 *   <li>{@link DepotEpargne}   – versement d'épargne au guichet</li>
 *   <li>{@link RetraitEpargne} – retrait d'épargne au guichet</li>
 *   <li>{@link FraisAdhesion}  – encaissement des frais d'adhésion membre</li>
 * </ul>
 *
 * Chaque opération génère une écriture comptable ({@link Comptabilite}).
 * Le statut suit le cycle : EN_ATTENTE → VALIDE ou ANNULE.
 * La relation vers {@link CompteEps} est NULLABLE car les frais d'adhésion
 * peuvent ne pas être rattachés à un compte épargne existant.
 *
 * DDL source of truth: P7-001-CREATE-TABLE-OperationCaisse.xml.
 * Spec: cahier §3.1.1 (Opérations de caisse).
 */
@Entity
@Table(name = "OperationCaisse")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(
    name             = "dtype",
    discriminatorType = DiscriminatorType.STRING,
    columnDefinition = "NVARCHAR(31)"
)
@DiscriminatorValue("OperationCaisse")
@DynamicUpdate
public class OperationCaisse implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // ── Champs métier ─────────────────────────────────────────────

    /**
     * Numéro de pièce comptable unique pour cette opération.
     * NVARCHAR(30) NOT NULL.
     */
    @NotBlank
    @Size(max = 30)
    @Column(name = "num_piece", length = 30, nullable = false)
    private String numPiece;

    /**
     * Date de l'opération de caisse.
     * DATE NOT NULL.
     */
    @NotNull
    @Column(name = "date_operation", nullable = false)
    private LocalDate dateOperation;

    /**
     * Montant de l'opération (DECIMAL 19,4). Toujours positif.
     */
    @NotNull
    @Column(name = "montant", precision = 19, scale = 4, nullable = false)
    private BigDecimal montant;

    /**
     * Mode de paiement utilisé au guichet.
     * Valeurs : ESPECES / CHEQUE / VIREMENT / MOBILE_MONEY.
     * Défaut : ESPECES.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", length = 20, nullable = false)
    private ModePaiementCaisse modePaiement = ModePaiementCaisse.ESPECES;

    /**
     * Motif ou libellé libre de l'opération.
     * NVARCHAR(500) NULLABLE.
     */
    @Size(max = 500)
    @Column(name = "motif", length = 500)
    private String motif;

    /**
     * Identifiant de l'utilisateur ayant saisi l'opération.
     * NVARCHAR(100) NULLABLE.
     */
    @Size(max = 100)
    @Column(name = "utilisateur", length = 100)
    private String utilisateur;

    /**
     * Statut courant de l'opération dans son cycle de vie.
     * Valeurs : EN_ATTENTE / VALIDE / ANNULE.
     * Défaut : EN_ATTENTE.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private StatutOperationCaisse statut = StatutOperationCaisse.EN_ATTENTE;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    // ── Associations ──────────────────────────────────────────────

    /**
     * Compte épargne concerné par l'opération.
     * FK vers COMPTEEPS(NUMCOMPTE). NULLABLE (les frais d'adhésion peuvent
     * ne pas encore avoir de compte épargne associé).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name       = "num_compte",
        foreignKey = @ForeignKey(name = "FK_OperationCaisse_CompteEps")
    )
    private CompteEps compteEps;

    /**
     * Agence guichet où a été réalisée l'opération.
     * FK vers AGENCE(CODE_AGENCE).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name       = "code_agence",
        foreignKey = @ForeignKey(name = "FK_OperationCaisse_Agence")
    )
    private Agence agence;

    /**
     * Écriture comptable générée par l'opération.
     * FK vers comptabilite(IDCOMPTABILITE). NOT NULL.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name       = "idcomptabilite",
        nullable   = false,
        foreignKey = @ForeignKey(name = "FK_OperationCaisse_Comptabilite")
    )
    private Comptabilite comptabilite;

    // ── Constructeurs ─────────────────────────────────────────────

    public OperationCaisse() {
    }

    public OperationCaisse(Long id, String numPiece, LocalDate dateOperation,
                           BigDecimal montant, ModePaiementCaisse modePaiement,
                           String motif, String utilisateur,
                           StatutOperationCaisse statut, Integer version,
                           CompteEps compteEps, Agence agence,
                           Comptabilite comptabilite) {
        this.id = id;
        this.numPiece = numPiece;
        this.dateOperation = dateOperation;
        this.montant = montant;
        this.modePaiement = modePaiement;
        this.motif = motif;
        this.utilisateur = utilisateur;
        this.statut = statut;
        this.version = version;
        this.compteEps = compteEps;
        this.agence = agence;
        this.comptabilite = comptabilite;
    }

    // ── Accesseurs ────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumPiece() { return numPiece; }
    public void setNumPiece(String numPiece) { this.numPiece = numPiece; }

    public LocalDate getDateOperation() { return dateOperation; }
    public void setDateOperation(LocalDate dateOperation) { this.dateOperation = dateOperation; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public ModePaiementCaisse getModePaiement() { return modePaiement; }
    public void setModePaiement(ModePaiementCaisse modePaiement) { this.modePaiement = modePaiement; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public String getUtilisateur() { return utilisateur; }
    public void setUtilisateur(String utilisateur) { this.utilisateur = utilisateur; }

    public StatutOperationCaisse getStatut() { return statut; }
    public void setStatut(StatutOperationCaisse statut) { this.statut = statut; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public CompteEps getCompteEps() { return compteEps; }
    public void setCompteEps(CompteEps compteEps) { this.compteEps = compteEps; }

    public Agence getAgence() { return agence; }
    public void setAgence(Agence agence) { this.agence = agence; }

    public Comptabilite getComptabilite() { return comptabilite; }
    public void setComptabilite(Comptabilite comptabilite) { this.comptabilite = comptabilite; }

    @Override
    public String toString() {
        return "OperationCaisse(id=" + id
            + ", numPiece=" + numPiece
            + ", dateOperation=" + dateOperation
            + ", montant=" + montant
            + ", statut=" + statut + ")";
    }
}

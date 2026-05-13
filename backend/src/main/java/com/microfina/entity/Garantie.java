package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Garantie – sûreté générique associée à un crédit.
 *
 * <h2>Positionnement architectural</h2>
 *
 * <p>La Phase 4 avait introduit des tables spécifiques par type de sûreté
 * ({@code HYPOTHEQUE_CREDIT}, {@code gageCredit}, {@code Caution}, {@code Nantissement}).
 * Cette entité fournit un <em>point d'entrée unifié</em> permettant :</p>
 * <ul>
 *   <li>de requêter toutes les garanties d'un crédit sans jointures multiples ;</li>
 *   <li>d'enregistrer des types futurs sans migration de schéma ;</li>
 *   <li>d'alimenter les états réglementaires BCM (ratio de couverture).</li>
 * </ul>
 * <p>Les tables Phase 4 restent actives pour les données existantes et les détails
 * supplémentaires spécifiques à chaque type.</p>
 *
 * <h2>Cycle de vie ({@code STATUT})</h2>
 * <pre>
 *   ACTIF ──→ LIBERE   (mainlevée lors du solde du crédit)
 *         ──→ SAISI    (exécution judiciaire suite au défaut)
 *         ──→ EXPIRE   (garantie arrivée à échéance)
 * </pre>
 *
 * <p>Table cible : {@code Garantie} — DDL : P10-001c-CREATE-TABLE-Garantie.xml</p>
 */
@Entity
@Table(name = "Garantie")
@DynamicUpdate
public class Garantie implements Serializable {

    private static final long serialVersionUID = 1L;

    // ─────────────────────────────────────────────────────────────────────────
    // Identifiant
    // ─────────────────────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDGARANTIE", nullable = false)
    private Long idGarantie;

    // ─────────────────────────────────────────────────────────────────────────
    // Associations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Type de garantie (référentiel).
     * Chargement différé pour éviter les requêtes inutiles sur la table de référence.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "CODE_TYPE_GARANTIE",
        referencedColumnName = "CODE",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_Garantie_type_garantie")
    )
    private TypeGarantie typeGarantie;

    /**
     * Crédit couvert par cette garantie.
     * Suppression en cascade : quand le crédit est supprimé, ses garanties le sont aussi.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "IDCREDIT",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_Garantie_Credits")
    )
    private Credits credit;

    /**
     * Membre garant (optionnel).
     * Renseigné uniquement pour les types CAUTION_PERSONNELLE et GARANTIE_GROUPE
     * quand le garant est un membre enregistré.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "NUM_MEMBRE_GARANT",
        referencedColumnName = "NUM_MEMBRE",
        foreignKey = @ForeignKey(name = "FK_Garantie_membres")
    )
    private Membres membreGarant;

    // ─────────────────────────────────────────────────────────────────────────
    // Valorisation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Valeur de marché estimée au moment de l'évaluation, exprimée en MRU.
     * Ne peut pas être négative ; toujours {@code precision=19, scale=4}.
     */
    @NotNull
    @DecimalMin(value = "0.0000", inclusive = true)
    @Column(name = "VALEUR_ESTIMEE", precision = 19, scale = 4, nullable = false)
    private BigDecimal valeurEstimee;

    /**
     * Taux de couverture calculé = {@code VALEUR_ESTIMEE / MONTANT_CREDIT × 100}.
     * Enregistré pour éviter une recalculation répétée lors des états BCM.
     * Nullable : calculé a posteriori si non fourni à la saisie.
     */
    @Column(name = "TAUX_COUVERTURE", precision = 10, scale = 4)
    private BigDecimal tauxCouverture;

    /** Date à laquelle la valorisation a été établie (expertise, cotation, etc.). */
    @Column(name = "DATE_EVALUATION")
    private LocalDate dateEvaluation;

    // ─────────────────────────────────────────────────────────────────────────
    // Suivi de vie
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Statut courant de la garantie.
     * Valeurs : {@code ACTIF} | {@code LIBERE} | {@code SAISI} | {@code EXPIRE}.
     */
    @Size(max = 20)
    @Column(name = "STATUT", length = 20, nullable = false)
    private String statut = "ACTIF";

    /**
     * Date de mainlevée ou d'expiration.
     * Renseignée quand {@code statut = LIBERE} ou {@code EXPIRE}.
     */
    @Column(name = "DATE_MAINLEVEE")
    private LocalDate dateMainlevee;

    /** Référence documentaire (numéro acte notarié, police assurance, etc.). */
    @Size(max = 100)
    @Column(name = "REFERENCE_DOCUMENT", length = 100)
    private String referenceDocument;

    /**
     * Observations libres : adresse du bien, immatriculation véhicule,
     * coordonnées du garant externe, etc.
     */
    @Size(max = 1000)
    @Column(name = "OBSERVATIONS", length = 1000)
    private String observations;

    // ─────────────────────────────────────────────────────────────────────────
    // Traçabilité
    // ─────────────────────────────────────────────────────────────────────────

    /** Login de l'utilisateur ayant enregistré la garantie. */
    @Size(max = 100)
    @Column(name = "UTILISATEUR", length = 100)
    private String utilisateur;

    /** Date de saisie dans le système. */
    @Column(name = "DATE_SAISIE")
    private LocalDate dateSaisie;

    // ─────────────────────────────────────────────────────────────────────────
    // Contrôle de concurrence
    // ─────────────────────────────────────────────────────────────────────────

    /** Version pour le verrouillage optimiste JPA. */
    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version = 0;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructeurs
    // ─────────────────────────────────────────────────────────────────────────

    /** Constructeur sans arguments requis par JPA. */
    public Garantie() {
    }

    /**
     * Constructeur de confort pour la création programmatique (cas nominal).
     *
     * @param typeGarantie  type de garantie (non nul)
     * @param credit        crédit couvert (non nul)
     * @param valeurEstimee valeur estimée en MRU (≥ 0)
     * @param utilisateur   login du saisissant
     */
    public Garantie(TypeGarantie typeGarantie,
                    Credits credit,
                    BigDecimal valeurEstimee,
                    String utilisateur) {
        this.typeGarantie = typeGarantie;
        this.credit = credit;
        this.valeurEstimee = valeurEstimee;
        this.utilisateur = utilisateur;
        this.dateSaisie = LocalDate.now();
        this.statut = "ACTIF";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters / Setters
    // ─────────────────────────────────────────────────────────────────────────

    /** @return identifiant technique */
    public Long getIdGarantie() { return idGarantie; }

    /** @param idGarantie identifiant technique */
    public void setIdGarantie(Long idGarantie) { this.idGarantie = idGarantie; }

    /** @return type de garantie */
    public TypeGarantie getTypeGarantie() { return typeGarantie; }

    /** @param typeGarantie type de garantie */
    public void setTypeGarantie(TypeGarantie typeGarantie) { this.typeGarantie = typeGarantie; }

    /** @return crédit couvert */
    public Credits getCredit() { return credit; }

    /** @param credit crédit couvert */
    public void setCredit(Credits credit) { this.credit = credit; }

    /** @return membre garant (nullable) */
    public Membres getMembreGarant() { return membreGarant; }

    /** @param membreGarant membre garant (nullable) */
    public void setMembreGarant(Membres membreGarant) { this.membreGarant = membreGarant; }

    /** @return valeur estimée en MRU */
    public BigDecimal getValeurEstimee() { return valeurEstimee; }

    /** @param valeurEstimee valeur estimée en MRU (≥ 0) */
    public void setValeurEstimee(BigDecimal valeurEstimee) { this.valeurEstimee = valeurEstimee; }

    /** @return taux de couverture calculé (nullable) */
    public BigDecimal getTauxCouverture() { return tauxCouverture; }

    /** @param tauxCouverture taux de couverture */
    public void setTauxCouverture(BigDecimal tauxCouverture) { this.tauxCouverture = tauxCouverture; }

    /** @return date d'évaluation */
    public LocalDate getDateEvaluation() { return dateEvaluation; }

    /** @param dateEvaluation date d'évaluation */
    public void setDateEvaluation(LocalDate dateEvaluation) { this.dateEvaluation = dateEvaluation; }

    /** @return statut courant (ACTIF/LIBERE/SAISI/EXPIRE) */
    public String getStatut() { return statut; }

    /** @param statut nouveau statut */
    public void setStatut(String statut) { this.statut = statut; }

    /** @return date de mainlevée (nullable) */
    public LocalDate getDateMainlevee() { return dateMainlevee; }

    /** @param dateMainlevee date de mainlevée */
    public void setDateMainlevee(LocalDate dateMainlevee) { this.dateMainlevee = dateMainlevee; }

    /** @return référence documentaire (nullable) */
    public String getReferenceDocument() { return referenceDocument; }

    /** @param referenceDocument référence documentaire */
    public void setReferenceDocument(String referenceDocument) { this.referenceDocument = referenceDocument; }

    /** @return observations libres (nullable) */
    public String getObservations() { return observations; }

    /** @param observations observations libres */
    public void setObservations(String observations) { this.observations = observations; }

    /** @return login du saisissant */
    public String getUtilisateur() { return utilisateur; }

    /** @param utilisateur login du saisissant */
    public void setUtilisateur(String utilisateur) { this.utilisateur = utilisateur; }

    /** @return date de saisie */
    public LocalDate getDateSaisie() { return dateSaisie; }

    /** @param dateSaisie date de saisie */
    public void setDateSaisie(LocalDate dateSaisie) { this.dateSaisie = dateSaisie; }

    /** @return version optimistic lock */
    public Integer getVersion() { return version; }

    /** @param version version optimistic lock */
    public void setVersion(Integer version) { this.version = version; }

    // ─────────────────────────────────────────────────────────────────────────
    // Object overrides
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Garantie(id=" + idGarantie
            + ", type=" + (typeGarantie != null ? typeGarantie.getCode() : null)
            + ", statut=" + statut
            + ", valeur=" + valeurEstimee + ")";
    }
}

package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * TypeGarantie – table de référence des types de sûretés acceptées par Microfina++.
 *
 * <p>Chaque type représente une catégorie de garantie reconnue par la Banque Centrale
 * de Mauritanie (BCM) ou pratiquée en microfinance :</p>
 *
 * <ul>
 *   <li>{@code HYPOTHEQUE}         – bien immobilier inscrit au registre foncier</li>
 *   <li>{@code GAGE}               – bien meuble corporel (véhicule, équipement)</li>
 *   <li>{@code NANTISSEMENT}       – actifs incorporels (compte, parts, créances)</li>
 *   <li>{@code CAUTION_PERSONNELLE}– personne physique caution simple ou solidaire</li>
 *   <li>{@code AVAL}               – aval bancaire ou cambiaire sur effet de commerce</li>
 *   <li>{@code DEPOT_GARANTIE}     – cash collateral bloqué sur compte dédié</li>
 *   <li>{@code ASSURANCE_CREDIT}   – police assurance vie/invalidité au profit de l'IMF</li>
 *   <li>{@code GARANTIE_GROUPE}    – caution solidaire d'un groupe de solidarité</li>
 *   <li>{@code EPARGNE_OBLIGATOIRE}– fraction de l'épargne nantie (TAUXGARANTIEEPARGNE)</li>
 *   <li>{@code AUTRE}              – tout autre type, détaillé en OBSERVATIONS</li>
 * </ul>
 *
 * <p>La PK est un code métier stable ({@code NVARCHAR(20)}) sans auto-incrément,
 * ce qui permet des JOIN sans jointure transitive et rend le SQL lisible.</p>
 *
 * <p>Table cible : {@code type_garantie} — DDL : P10-001-CREATE-TABLE-type_garantie.xml</p>
 */
@Entity
@Table(name = "type_garantie")
@DynamicUpdate
public class TypeGarantie implements Serializable {

    private static final long serialVersionUID = 1L;

    // ─────────────────────────────────────────────────────────────────────────
    // Identifiant
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Code métier unique servant de clé primaire.
     * Exemples : {@code "HYPOTHEQUE"}, {@code "GAGE"}, {@code "CAUTION_PERSONNELLE"}.
     */
    @Id
    @NotBlank
    @Size(max = 20)
    @Column(name = "CODE", length = 20, nullable = false)
    private String code;

    // ─────────────────────────────────────────────────────────────────────────
    // Attributs descriptifs
    // ─────────────────────────────────────────────────────────────────────────

    /** Libellé court affiché dans les formulaires et listes déroulantes. */
    @NotBlank
    @Size(max = 100)
    @Column(name = "LIBELLE", length = 100, nullable = false)
    private String libelle;

    /** Description longue à usage documentaire et d'aide en ligne. */
    @Size(max = 500)
    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    /**
     * Indicateur de disponibilité : {@code true} = actif (visible dans les formulaires) ;
     * {@code false} = archivé (soft-delete, invisible mais données historiques préservées).
     */
    @Column(name = "ACTIF", nullable = false)
    private Boolean actif = true;

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
    public TypeGarantie() {
    }

    /**
     * Constructeur de confort pour la création programmatique.
     *
     * @param code        code métier unique (max 20 car.)
     * @param libelle     libellé court (max 100 car.)
     * @param description description longue (nullable)
     */
    public TypeGarantie(String code, String libelle, String description) {
        this.code = code;
        this.libelle = libelle;
        this.description = description;
        this.actif = true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters / Setters
    // ─────────────────────────────────────────────────────────────────────────

    /** @return code métier PK */
    public String getCode() { return code; }

    /** @param code code métier PK */
    public void setCode(String code) { this.code = code; }

    /** @return libellé court */
    public String getLibelle() { return libelle; }

    /** @param libelle libellé court */
    public void setLibelle(String libelle) { this.libelle = libelle; }

    /** @return description longue (nullable) */
    public String getDescription() { return description; }

    /** @param description description longue */
    public void setDescription(String description) { this.description = description; }

    /** @return {@code true} si actif */
    public Boolean getActif() { return actif; }

    /** @param actif {@code true} pour activer, {@code false} pour archiver */
    public void setActif(Boolean actif) { this.actif = actif; }

    /** @return version optimistic lock */
    public Integer getVersion() { return version; }

    /** @param version version optimistic lock */
    public void setVersion(Integer version) { this.version = version; }

    // ─────────────────────────────────────────────────────────────────────────
    // Object overrides
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "TypeGarantie(code=" + code + ", libelle=" + libelle + ", actif=" + actif + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeGarantie other)) return false;
        return code != null && code.equals(other.code);
    }

    @Override
    public int hashCode() {
        return code == null ? 0 : code.hashCode();
    }
}

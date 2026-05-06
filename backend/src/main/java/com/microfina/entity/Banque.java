package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * Banque – référentiel des banques partenaires ou correspondantes.
 *
 * Utilisé pour identifier la banque domiciliataire d'un compte bancaire
 * ({@link CompteBanque}) ainsi que les banques présentatrices dans les
 * remises de chèques ({@link RemiseCheque}).
 *
 * DDL source of truth: P6-005-CREATE-TABLE-Banque.xml.
 * Spec: cahier §6 (Module Banque – référentiel bancaire).
 */
@Entity
@Table(name = "Banque")
@DynamicUpdate
public class Banque implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @NotBlank
    @Size(max = 20)
    @Column(name = "code_banque", length = 20, nullable = false)
    private String codeBanque;

    // ── Identification ────────────────────────────────────────────

    /** Raison sociale / dénomination de la banque. */
    @Size(max = 255)
    @Column(name = "nom", length = 255, nullable = false)
    private String nom;

    /** Code SWIFT / BIC international (8 ou 11 caractères). */
    @Size(max = 11)
    @Column(name = "swift_bic", length = 11)
    private String swiftBic;

    /** Adresse du siège ou de l'agence principale. */
    @Size(max = 500)
    @Column(name = "adresse", length = 500)
    private String adresse;

    /** Pays d'implantation (code ISO 3166-1 alpha-2 recommandé). */
    @Size(max = 100)
    @Column(name = "pays", length = 100)
    private String pays;

    /** Indicateur d'activité : {@code true} = banque active. */
    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "version")
    private Integer version = 0;

    // ── Constructeurs ─────────────────────────────────────────────

    public Banque() {
    }

    public Banque(String codeBanque, String nom, String swiftBic, String adresse,
                  String pays, Boolean actif, Integer version) {
        this.codeBanque = codeBanque;
        this.nom = nom;
        this.swiftBic = swiftBic;
        this.adresse = adresse;
        this.pays = pays;
        this.actif = actif;
        this.version = version;
    }

    // ── Accesseurs ────────────────────────────────────────────────

    public String getCodeBanque() { return codeBanque; }
    public void setCodeBanque(String codeBanque) { this.codeBanque = codeBanque; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getSwiftBic() { return swiftBic; }
    public void setSwiftBic(String swiftBic) { this.swiftBic = swiftBic; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @Override
    public String toString() {
        return "Banque(codeBanque=" + codeBanque + ", nom=" + nom + ")";
    }
}

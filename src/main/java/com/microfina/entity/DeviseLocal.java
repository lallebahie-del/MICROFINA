package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * devise_local – local currencies used by institutions and agencies.
 * XOF/FCFA is the default for West-African MFIs (UEMOA zone).
 */
@Entity
@Table(name = "devise_local")
@DynamicUpdate
public class DeviseLocal implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ISO 4217 currency code, e.g. "XOF", "EUR". */
    @Id
    @NotBlank
    @Size(max = 10)
    @Column(name = "CODE_DEVISE", length = 10, nullable = false)
    private String codeDevise;

    /** Full currency name in French. */
    @NotBlank
    @Size(max = 255)
    @Column(name = "LIBELLE", length = 255, nullable = false)
    private String libelle;

    /** Currency symbol, e.g. "FCFA", "€". */
    @Size(max = 10)
    @Column(name = "SYMBOLE", length = 10)
    private String symbole;

    /** Whether this currency is currently active. */
    @Column(name = "ACTIF", nullable = false)
    private Boolean actif = true;

    /** Exchange rate relative to base currency (default 1.0000 = local currency). */
    @NotNull
    @Column(name = "TAUX_CHANGE", precision = 19, scale = 4, nullable = false)
    private BigDecimal tauxChange = BigDecimal.ONE;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    @Column(name = "DATE_CREATION")
    private LocalDateTime dateCreation;

    @PrePersist
    private void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public DeviseLocal() {
    }

    public DeviseLocal(String codeDevise, String libelle, String symbole, Boolean actif, BigDecimal tauxChange, Integer version, LocalDateTime dateCreation) {
        this.codeDevise = codeDevise;
        this.libelle = libelle;
        this.symbole = symbole;
        this.actif = actif;
        this.tauxChange = tauxChange;
        this.version = version;
        this.dateCreation = dateCreation;
    }

    public String getCodeDevise() {
        return codeDevise;
    }

    public void setCodeDevise(String codeDevise) {
        this.codeDevise = codeDevise;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getSymbole() {
        return symbole;
    }

    public void setSymbole(String symbole) {
        this.symbole = symbole;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public BigDecimal getTauxChange() {
        return tauxChange;
    }

    public void setTauxChange(BigDecimal tauxChange) {
        this.tauxChange = tauxChange;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return "DeviseLocal("
            + "codeDevise=" + codeDevise
            + ")";
    }
}

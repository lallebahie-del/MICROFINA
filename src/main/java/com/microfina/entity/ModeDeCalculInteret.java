package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * mode_de_calcul_interet – interest calculation modes applied to credit products
 * (SIMPLE, COMPOSE, DEGRESSIF, FIXE, etc.).
 */
@Entity
@Table(name = "mode_de_calcul_interet")
@DynamicUpdate
public class ModeDeCalculInteret implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CODEMODE", nullable = false)
    private Long codeMode;

    /** Human-readable description of the mode. */
    @NotBlank
    @Size(max = 255)
    @Column(name = "DESCRIPTION", length = 255, nullable = false)
    private String description;

    /** Technical mode code, e.g. "SIMPLE", "COMPOSE", "DEGRESSIF". */
    @NotBlank
    @Size(max = 50)
    @Column(name = "MODECALCUL", length = 50, nullable = false)
    private String modeCalcul;

    @Column(name = "ACTIF", nullable = false)
    private Boolean actif = true;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public ModeDeCalculInteret() {
    }

    public ModeDeCalculInteret(Long codeMode, String description, String modeCalcul, Boolean actif, Integer version) {
        this.codeMode = codeMode;
        this.description = description;
        this.modeCalcul = modeCalcul;
        this.actif = actif;
        this.version = version;
    }

    public Long getCodeMode() {
        return codeMode;
    }

    public void setCodeMode(Long codeMode) {
        this.codeMode = codeMode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModeCalcul() {
        return modeCalcul;
    }

    public void setModeCalcul(String modeCalcul) {
        this.modeCalcul = modeCalcul;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ModeDeCalculInteret("
            + "codeMode=" + codeMode
            + ")";
    }
}

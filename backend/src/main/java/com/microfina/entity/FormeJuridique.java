package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * FormeJuridique – legal entity forms (SARL, SA, GIE, Personne Physique, etc.)
 * used to classify members and institutions.
 */
@Entity
@Table(name = "FormeJuridique")
@DynamicUpdate
public class FormeJuridique implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CODE", nullable = false)
    private Long code;

    @NotBlank
    @Size(max = 255)
    @Column(name = "LIBELLE", length = 255, nullable = false)
    private String libelle;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public FormeJuridique() {
    }

    public FormeJuridique(Long code, String libelle, Integer version) {
        this.code = code;
        this.libelle = libelle;
        this.version = version;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "FormeJuridique("
            + "code=" + code
            + ")";
    }
}

package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * ADRESSE – stores physical/contact addresses for institutions, agencies, and members.
 */
@Entity
@Table(name = "ADRESSE")
@DynamicUpdate
public class Adresse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique address code (UUID or custom key). */
    @Id
    @Column(name = "IDADRESSE", length = 48, nullable = false)
    private String idAdresse;

    /** Secondary address line. */
    @Column(name = "adresse", length = 255)
    private String adresse;

    /** Primary address line. */
    @Column(name = "adresse1", length = 255)
    private String adresse1;

    /** P.O. Box. */
    @Column(name = "BP", length = 255)
    private String bp;

    @Column(name = "descriptionAdresse", length = 255)
    private String descriptionAdresse;

    /** Discriminator for address type (used in joined-table hierarchies). */
    @Column(name = "DTYPE", length = 255)
    private String dtype;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "fax", length = 255)
    private String fax;

    @Column(name = "rueMaison", length = 255)
    private String rueMaison;

    @Column(name = "telephone", length = 255)
    private String telephone;

    @Column(name = "telephoneFixe", length = 255)
    private String telephoneFixe;

    @Column(name = "telephoneMobile", length = 255)
    private String telephoneMobile;

    /** Optimistic-lock version counter. */
    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    @Column(name = "LATITUDE", length = 255)
    private String latitude;

    @Column(name = "LONGITUDE", length = 255)
    private String longitude;

    /** Country phone indicator (FK to pays.indicateurTelMobile). */
    @Column(name = "indicateurTelMobile")
    private Integer indicateurTelMobile;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public Adresse() {
    }

    public Adresse(String idAdresse, String adresse, String adresse1, String bp, String descriptionAdresse, String dtype, String email, String fax, String rueMaison, String telephone, String telephoneFixe, String telephoneMobile, Integer version, String latitude, String longitude, Integer indicateurTelMobile) {
        this.idAdresse = idAdresse;
        this.adresse = adresse;
        this.adresse1 = adresse1;
        this.bp = bp;
        this.descriptionAdresse = descriptionAdresse;
        this.dtype = dtype;
        this.email = email;
        this.fax = fax;
        this.rueMaison = rueMaison;
        this.telephone = telephone;
        this.telephoneFixe = telephoneFixe;
        this.telephoneMobile = telephoneMobile;
        this.version = version;
        this.latitude = latitude;
        this.longitude = longitude;
        this.indicateurTelMobile = indicateurTelMobile;
    }

    public String getIdAdresse() {
        return idAdresse;
    }

    public void setIdAdresse(String idAdresse) {
        this.idAdresse = idAdresse;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getAdresse1() {
        return adresse1;
    }

    public void setAdresse1(String adresse1) {
        this.adresse1 = adresse1;
    }

    public String getBp() {
        return bp;
    }

    public void setBp(String bp) {
        this.bp = bp;
    }

    public String getDescriptionAdresse() {
        return descriptionAdresse;
    }

    public void setDescriptionAdresse(String descriptionAdresse) {
        this.descriptionAdresse = descriptionAdresse;
    }

    public String getDtype() {
        return dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getRueMaison() {
        return rueMaison;
    }

    public void setRueMaison(String rueMaison) {
        this.rueMaison = rueMaison;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getTelephoneFixe() {
        return telephoneFixe;
    }

    public void setTelephoneFixe(String telephoneFixe) {
        this.telephoneFixe = telephoneFixe;
    }

    public String getTelephoneMobile() {
        return telephoneMobile;
    }

    public void setTelephoneMobile(String telephoneMobile) {
        this.telephoneMobile = telephoneMobile;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Integer getIndicateurTelMobile() {
        return indicateurTelMobile;
    }

    public void setIndicateurTelMobile(Integer indicateurTelMobile) {
        this.indicateurTelMobile = indicateurTelMobile;
    }

    @Override
    public String toString() {
        return "Adresse("
            + "idAdresse=" + idAdresse
            + ")";
    }
}

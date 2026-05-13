package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Notification — message in-app poussé vers un utilisateur (mobile ou web).
 *
 * <p>DDL source of truth : P13-001-CREATE-TABLE-Notification.xml.</p>
 */
@Entity
@Table(name = "Notification")
@DynamicUpdate
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** Login (Utilisateur.login) du destinataire. */
    @NotBlank
    @Size(max = 100)
    @Column(name = "user_login", length = 100, nullable = false)
    private String userLogin;

    @NotBlank
    @Size(max = 200)
    @Column(name = "titre", length = 200, nullable = false)
    private String titre;

    @Size(max = 1000)
    @Column(name = "message", length = 1000)
    private String message;

    /** Type libre : INFO, ALERTE, OPERATION, COMITE, etc. */
    @Size(max = 50)
    @Column(name = "type", length = 50, nullable = false)
    private String type = "INFO";

    @Column(name = "lu", nullable = false)
    private Boolean lu = false;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_lecture")
    private LocalDateTime dateLecture;

    @Size(max = 500)
    @Column(name = "lien", length = 500)
    private String lien;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @PrePersist
    void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (lu == null) lu = false;
        if (type == null) type = "INFO";
    }

    public Notification() {}

    public Notification(String userLogin, String titre, String message, String type) {
        this.userLogin = userLogin;
        this.titre = titre;
        this.message = message;
        this.type = type != null ? type : "INFO";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserLogin() { return userLogin; }
    public void setUserLogin(String userLogin) { this.userLogin = userLogin; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Boolean getLu() { return lu; }
    public void setLu(Boolean lu) { this.lu = lu; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateLecture() { return dateLecture; }
    public void setDateLecture(LocalDateTime dateLecture) { this.dateLecture = dateLecture; }

    public String getLien() { return lien; }
    public void setLien(String lien) { this.lien = lien; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}

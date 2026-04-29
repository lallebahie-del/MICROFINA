package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * MembreGroupe – JOINED subtype of {@link Tiers}.
 *
 * Represents a member's participation record within a solidarity group.
 * PK {@code CODE_TIERS} is shared with the parent TIERS table.
 *
 * DDL source of truth: P2-004-CREATE-TABLE-MembreGroupe.xml.
 * Spec p.39-40.
 *
 * Phase-2+ FK columns (idGroupement, idSolidarite, typeActivite,
 * CODE_BRANCHE_ACTIVITE, CODE_TYPE_DE_PIECE) stored as raw values.
 */
@Entity
@Table(name = "MembreGroupe")
@PrimaryKeyJoinColumn(
    name                 = "CODE_TIERS",
    referencedColumnName = "CODE_TIERS",
    foreignKey           = @ForeignKey(name = "FK_MembreGroupe_TIERS")
)
@DiscriminatorValue("MembreGroupe")
@DynamicUpdate
public class MembreGroupe extends Tiers implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Group member flags (AN/1) ─────────────────────────────────

    @Size(max = 1)
    @Column(name = "ACTIFGROUPE", length = 1)
    private String actifGroupe;

    @Size(max = 1)
    @Column(name = "ANALPHABETE", length = 1)
    private String analphabete;

    @Size(max = 1)
    @Column(name = "CHEF", length = 1)
    private String chef;

    @Size(max = 1)
    @Column(name = "DROITSIGNATURE", length = 1)
    private String droitSignature;

    // ── Codes & labels ────────────────────────────────────────────

    @Size(max = 255)
    @Column(name = "CODEMEMBREGROUPP", length = 255)
    private String codeMembreGroupp;

    @Size(max = 50)
    @Column(name = "fonction", length = 50)
    private String fonction;

    @Column(name = "SECTIONGROUPE")
    private Integer sectionGroupe;

    // ── Financial ─────────────────────────────────────────────────

    @Column(name = "MONTANTGROUPE", precision = 19, scale = 4)
    private BigDecimal montantGroupe;

    // ── Contact & role ────────────────────────────────────────────

    @Size(max = 255)
    @Column(name = "PERSONNECONTACT", length = 255)
    private String personneContact;

    @Size(max = 255)
    @Column(name = "POSTEOCCUPE", length = 255)
    private String posteOccupe;

    // ── Document reference ────────────────────────────────────────

    @Size(max = 100)
    @Column(name = "REFERENCE_PIECE", length = 100)
    private String referencePiece;

    @Size(max = 25)
    @Column(name = "num_membre_old", length = 25)
    private String numMembreOld;

    // ── Blobs (stored as numeric reference) ───────────────────────

    @Column(name = "photo")
    private Long photo;

    @Column(name = "signature")
    private Long signature;

    // ── FK to membres (wired, Phase 2) ───────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "NUMMEMBRE",
        referencedColumnName = "NUM_MEMBRE",
        foreignKey           = @ForeignKey(name = "FK_MembreGroupe_membres")
    )
    private Membres membre;

    // ── Phase-2+ FK columns (no @ManyToOne yet) ──────────────────

    @Column(name = "idgroupeinterne")
    private Long idGroupeInterne;

    @Size(max = 25)
    @Column(name = "idGroupement", length = 25)
    private String idGroupement;

    @Column(name = "idSolidarite")
    private Long idSolidarite;

    @Column(name = "typeActivite")
    private Long typeActivite;

    @Size(max = 25)
    @Column(name = "CODE_BRANCHE_ACTIVITE", length = 25)
    private String codeBrancheActivite;

    @Column(name = "CODE_TYPE_DE_PIECE")
    private Long codeTypeDePiece;

    @Column(name = "Activite")
    private Long activite;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public MembreGroupe() {
    }

    public MembreGroupe(String actifGroupe, String analphabete, String chef, String droitSignature, String codeMembreGroupp, String fonction, Integer sectionGroupe, BigDecimal montantGroupe, String personneContact, String posteOccupe, String referencePiece, String numMembreOld, Long photo, Long signature, Membres membre, Long idGroupeInterne, String idGroupement, Long idSolidarite, Long typeActivite, String codeBrancheActivite, Long codeTypeDePiece, Long activite) {
        this.actifGroupe = actifGroupe;
        this.analphabete = analphabete;
        this.chef = chef;
        this.droitSignature = droitSignature;
        this.codeMembreGroupp = codeMembreGroupp;
        this.fonction = fonction;
        this.sectionGroupe = sectionGroupe;
        this.montantGroupe = montantGroupe;
        this.personneContact = personneContact;
        this.posteOccupe = posteOccupe;
        this.referencePiece = referencePiece;
        this.numMembreOld = numMembreOld;
        this.photo = photo;
        this.signature = signature;
        this.membre = membre;
        this.idGroupeInterne = idGroupeInterne;
        this.idGroupement = idGroupement;
        this.idSolidarite = idSolidarite;
        this.typeActivite = typeActivite;
        this.codeBrancheActivite = codeBrancheActivite;
        this.codeTypeDePiece = codeTypeDePiece;
        this.activite = activite;
    }

    public String getActifGroupe() {
        return actifGroupe;
    }

    public void setActifGroupe(String actifGroupe) {
        this.actifGroupe = actifGroupe;
    }

    public String getAnalphabete() {
        return analphabete;
    }

    public void setAnalphabete(String analphabete) {
        this.analphabete = analphabete;
    }

    public String getChef() {
        return chef;
    }

    public void setChef(String chef) {
        this.chef = chef;
    }

    public String getDroitSignature() {
        return droitSignature;
    }

    public void setDroitSignature(String droitSignature) {
        this.droitSignature = droitSignature;
    }

    public String getCodeMembreGroupp() {
        return codeMembreGroupp;
    }

    public void setCodeMembreGroupp(String codeMembreGroupp) {
        this.codeMembreGroupp = codeMembreGroupp;
    }

    public String getFonction() {
        return fonction;
    }

    public void setFonction(String fonction) {
        this.fonction = fonction;
    }

    public Integer getSectionGroupe() {
        return sectionGroupe;
    }

    public void setSectionGroupe(Integer sectionGroupe) {
        this.sectionGroupe = sectionGroupe;
    }

    public BigDecimal getMontantGroupe() {
        return montantGroupe;
    }

    public void setMontantGroupe(BigDecimal montantGroupe) {
        this.montantGroupe = montantGroupe;
    }

    public String getPersonneContact() {
        return personneContact;
    }

    public void setPersonneContact(String personneContact) {
        this.personneContact = personneContact;
    }

    public String getPosteOccupe() {
        return posteOccupe;
    }

    public void setPosteOccupe(String posteOccupe) {
        this.posteOccupe = posteOccupe;
    }

    public String getReferencePiece() {
        return referencePiece;
    }

    public void setReferencePiece(String referencePiece) {
        this.referencePiece = referencePiece;
    }

    public String getNumMembreOld() {
        return numMembreOld;
    }

    public void setNumMembreOld(String numMembreOld) {
        this.numMembreOld = numMembreOld;
    }

    public Long getPhoto() {
        return photo;
    }

    public void setPhoto(Long photo) {
        this.photo = photo;
    }

    public Long getSignature() {
        return signature;
    }

    public void setSignature(Long signature) {
        this.signature = signature;
    }

    public Membres getMembre() {
        return membre;
    }

    public void setMembre(Membres membre) {
        this.membre = membre;
    }

    public Long getIdGroupeInterne() {
        return idGroupeInterne;
    }

    public void setIdGroupeInterne(Long idGroupeInterne) {
        this.idGroupeInterne = idGroupeInterne;
    }

    public String getIdGroupement() {
        return idGroupement;
    }

    public void setIdGroupement(String idGroupement) {
        this.idGroupement = idGroupement;
    }

    public Long getIdSolidarite() {
        return idSolidarite;
    }

    public void setIdSolidarite(Long idSolidarite) {
        this.idSolidarite = idSolidarite;
    }

    public Long getTypeActivite() {
        return typeActivite;
    }

    public void setTypeActivite(Long typeActivite) {
        this.typeActivite = typeActivite;
    }

    public String getCodeBrancheActivite() {
        return codeBrancheActivite;
    }

    public void setCodeBrancheActivite(String codeBrancheActivite) {
        this.codeBrancheActivite = codeBrancheActivite;
    }

    public Long getCodeTypeDePiece() {
        return codeTypeDePiece;
    }

    public void setCodeTypeDePiece(Long codeTypeDePiece) {
        this.codeTypeDePiece = codeTypeDePiece;
    }

    public Long getActivite() {
        return activite;
    }

    public void setActivite(Long activite) {
        this.activite = activite;
    }

    @Override
    public String toString() {
        return "MembreGroupe(" + super.toString() + ")";
    }
}

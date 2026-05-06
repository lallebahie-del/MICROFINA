package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * Signataire – JOINED subtype of {@link Tiers}.
 *
 * Represents a signatory person authorised to sign on behalf of a
 * corporate (enterprise) member.  The PK {@code CODE_TIERS} is shared
 * with the parent TIERS table.
 *
 * DDL source of truth: P2-005-CREATE-TABLE-SIGNATAIRE.xml.
 * Spec p.63.
 *
 * Phase-2+ FK column: {@code CODE_ORGANE} – stored as plain String.
 */
@Entity
@Table(name = "SIGNATAIRE")
@PrimaryKeyJoinColumn(
    name                 = "CODE_TIERS",
    referencedColumnName = "CODE_TIERS",
    foreignKey           = @ForeignKey(name = "FK_SIGNATAIRE_TIERS")
)
@DiscriminatorValue("Signataire")
@DynamicUpdate
public class Signataire extends Tiers implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Flags & codes ────────────────────────────────────────────

    @Size(max = 1)
    @Column(name = "ACTIFGROUPE", length = 1)
    private String actifGroupe;

    @Size(max = 255)
    @Column(name = "CODEMEMBRE", length = 255)
    private String codeMembre;

    @Size(max = 255)
    @Column(name = "NUMERO", length = 255)
    private String numero;

    @Size(max = 25)
    @Column(name = "TITRE", length = 25)
    private String titre;

    @Size(max = 25)
    @Column(name = "num_membre_old", length = 25)
    private String numMembreOld;

    // ── Identity references ───────────────────────────────────────

    @Size(max = 50)
    @Column(name = "IDPERSONNE", length = 50)
    private String idPersonne;

    @Size(max = 50)
    @Column(name = "NUMMEMBRE_GROUPEMENT", length = 50)
    private String numMembreGroupement;

    /** Whether the signatory's presence is mandatory. */
    @Column(name = "OBLIGATOIRE")
    private Integer obligatoire;

    // ── Blobs (stored as numeric reference) ───────────────────────

    @Column(name = "photo")
    private Long photo;

    @Column(name = "signature")
    private Long signature;

    // ── FKs to membres (wired, Phase 2) ──────────────────────────

    /** The enterprise member this signatory represents. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "NUMMEMBRE_ENTREPRISE",
        referencedColumnName = "NUM_MEMBRE",
        foreignKey           = @ForeignKey(name = "FK_SIGNATAIRE_membres_entreprise")
    )
    private Membres membreEntreprise;

    /** The physical person who is the signatory. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "NUMMEMBRE_PHYSIQUE",
        referencedColumnName = "NUM_MEMBRE",
        foreignKey           = @ForeignKey(name = "FK_SIGNATAIRE_membres_physique")
    )
    private Membres membrePhysique;

    // ── Phase-2+ FK column ────────────────────────────────────────

    /** Code de l'organe (AN/25). Constraint deferred. */
    @Size(max = 25)
    @Column(name = "CODE_ORGANE", length = 25)
    private String codeOrgane;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public Signataire() {
    }

    public Signataire(String actifGroupe, String codeMembre, String numero, String titre, String numMembreOld, String idPersonne, String numMembreGroupement, Integer obligatoire, Long photo, Long signature, Membres membreEntreprise, Membres membrePhysique, String codeOrgane) {
        this.actifGroupe = actifGroupe;
        this.codeMembre = codeMembre;
        this.numero = numero;
        this.titre = titre;
        this.numMembreOld = numMembreOld;
        this.idPersonne = idPersonne;
        this.numMembreGroupement = numMembreGroupement;
        this.obligatoire = obligatoire;
        this.photo = photo;
        this.signature = signature;
        this.membreEntreprise = membreEntreprise;
        this.membrePhysique = membrePhysique;
        this.codeOrgane = codeOrgane;
    }

    public String getActifGroupe() {
        return actifGroupe;
    }

    public void setActifGroupe(String actifGroupe) {
        this.actifGroupe = actifGroupe;
    }

    public String getCodeMembre() {
        return codeMembre;
    }

    public void setCodeMembre(String codeMembre) {
        this.codeMembre = codeMembre;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getNumMembreOld() {
        return numMembreOld;
    }

    public void setNumMembreOld(String numMembreOld) {
        this.numMembreOld = numMembreOld;
    }

    public String getIdPersonne() {
        return idPersonne;
    }

    public void setIdPersonne(String idPersonne) {
        this.idPersonne = idPersonne;
    }

    public String getNumMembreGroupement() {
        return numMembreGroupement;
    }

    public void setNumMembreGroupement(String numMembreGroupement) {
        this.numMembreGroupement = numMembreGroupement;
    }

    public Integer getObligatoire() {
        return obligatoire;
    }

    public void setObligatoire(Integer obligatoire) {
        this.obligatoire = obligatoire;
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

    public Membres getMembreEntreprise() {
        return membreEntreprise;
    }

    public void setMembreEntreprise(Membres membreEntreprise) {
        this.membreEntreprise = membreEntreprise;
    }

    public Membres getMembrePhysique() {
        return membrePhysique;
    }

    public void setMembrePhysique(Membres membrePhysique) {
        this.membrePhysique = membrePhysique;
    }

    public String getCodeOrgane() {
        return codeOrgane;
    }

    public void setCodeOrgane(String codeOrgane) {
        this.codeOrgane = codeOrgane;
    }

    @Override
    public String toString() {
        return "Signataire(" + super.toString() + ")";
    }
}

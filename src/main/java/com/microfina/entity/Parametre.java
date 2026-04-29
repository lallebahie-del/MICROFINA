package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * Parametre – full 6-segment member-number generation parameters.
 * Each row controls how the system builds member numbers for a given
 * branch/institution/category combination.
 * Spec: DB-FINA-202112-001 p.49-50.
 */
@Entity
@Table(name = "Parametre")
@DynamicUpdate
public class Parametre implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDPARAMETRE", nullable = false)
    private Long idParametre;

    /** Maximum number of open days allowed. */
    @Column(name = "MAXIJOUROUVERT")
    private Integer maxiJourOuvert;

    // ── 6-segment numbering: ORDER of each segment ──────────────

    /** Ordering position of the agency segment. */
    @Column(name = "ordreAgence")
    private Integer ordreAgence;

    /** Ordering position of the member-category segment. */
    @Column(name = "ordreCategorie")
    private Integer ordreCategorie;

    /** Ordering position of the counter segment. */
    @Column(name = "ordreCompteur")
    private Integer ordreCompteur;

    /** Ordering position of the institution segment. */
    @Column(name = "ordreInstitution")
    private Integer ordreInstitution;

    /** Ordering position of the prefix segment. */
    @Column(name = "ordrePrefixe")
    private Integer ordrePrefixe;

    /** Ordering position of the suffix segment. */
    @Column(name = "ordreSuffixe")
    private Integer ordreSuffixe;

    // ── 6-segment numbering: POSITION (width/format) of each segment ──

    /** Display position / column width for the agency segment. */
    @Column(name = "positionAgence")
    private Integer positionAgence;

    /** Display position / column width for the category segment. */
    @Column(name = "positionCategorie")
    private Integer positionCategorie;

    /** Display position / column width for the counter segment. */
    @Column(name = "positionCompteur")
    private Integer positionCompteur;

    /** Display position / column width for the institution segment. */
    @Column(name = "positionInstitution")
    private Integer positionInstitution;

    /** Prefix string prepended to member numbers. */
    @Size(max = 255)
    @Column(name = "prefixe", length = 255)
    private String prefixe;

    /** Suffix string appended to member numbers. */
    @Size(max = 255)
    @Column(name = "suffixe", length = 255)
    private String suffixe;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    /** "O" = multi-currency enabled, "N" = single-currency. */
    @Size(max = 1)
    @Column(name = "use_multidevise", length = 1)
    private String useMultidevise = "N";

    // ── Absence note templates ───────────────────────────────────

    @Size(max = 255)
    @Column(name = "note_absence_deductible", length = 255)
    private String noteAbsenceDeductible;

    @Size(max = 255)
    @Column(name = "note_absence_non_deductible", length = 255)
    private String noteAbsenceNonDeductible;

    @Size(max = 255)
    @Column(name = "note_absence_non_deductible_complement", length = 255)
    private String noteAbsenceNonDeductibleComplement;

    @Size(max = 255)
    @Column(name = "note_interruption_absence", length = 255)
    private String noteInterruptionAbsence;

    /** Collector-level sequential counter. */
    @Column(name = "compteurCollecteur")
    private Long compteurCollecteur;

    /** FK (account number) for PEL savings account. */
    @Size(max = 15)
    @Column(name = "comptePEL", length = 15)
    private String comptePEL;

    /** FK (account number) for PEL interest account. */
    @Size(max = 15)
    @Column(name = "compteInteretPEL", length = 15)
    private String compteInteretPEL;

    // ── Cross-reference strings (kept as raw keys to avoid phase-2 deps) ──

    /** Member-category identifier used for counter look-up. AN/25. */
    @Size(max = 25)
    @Column(name = "fk_categorie", length = 25)
    private String fkCategorie;

    /** Member-counter code scoped to the category. AN/25. */
    @Size(max = 25)
    @Column(name = "fk_compteurMembre_categorie", length = 25)
    private String fkCompteurMembreCategorie;

    /** Member-counter code scoped to the institution. AN/255. */
    @Size(max = 255)
    @Column(name = "fk_compteurMembre_institution", length = 255)
    private String fkCompteurMembreInstitution;

    /** Member-counter code scoped to the agency. AN/5. */
    @Size(max = 5)
    @Column(name = "fk_compteurMembre_agence", length = 5)
    private String fkCompteurMembreAgence;

    // ── FK relationships ─────────────────────────────────────────

    /** FK to AGENCE. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_agence", referencedColumnName = "CODE_AGENCE",
                foreignKey = @ForeignKey(name = "FK_Parametre_AGENCE"))
    private Agence agence;

    /** FK to institution. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_institution", referencedColumnName = "CODE_INSTITUTION",
                foreignKey = @ForeignKey(name = "FK_Parametre_institution"))
    private Institution institution;

    /** FK to CompteurMembre. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COMPTEUR_MEMBRE", referencedColumnName = "ID_COMPTEUR_MEMBRE",
                foreignKey = @ForeignKey(name = "FK_Parametre_CompteurMembre"))
    private CompteurMembre compteurMembre;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public Parametre() {
    }

    public Parametre(Long idParametre, Integer maxiJourOuvert, Integer ordreAgence, Integer ordreCategorie, Integer ordreCompteur, Integer ordreInstitution, Integer ordrePrefixe, Integer ordreSuffixe, Integer positionAgence, Integer positionCategorie, Integer positionCompteur, Integer positionInstitution, String prefixe, String suffixe, Integer version, String useMultidevise, String noteAbsenceDeductible, String noteAbsenceNonDeductible, String noteAbsenceNonDeductibleComplement, String noteInterruptionAbsence, Long compteurCollecteur, String comptePEL, String compteInteretPEL, String fkCategorie, String fkCompteurMembreCategorie, String fkCompteurMembreInstitution, String fkCompteurMembreAgence, Agence agence, Institution institution, CompteurMembre compteurMembre) {
        this.idParametre = idParametre;
        this.maxiJourOuvert = maxiJourOuvert;
        this.ordreAgence = ordreAgence;
        this.ordreCategorie = ordreCategorie;
        this.ordreCompteur = ordreCompteur;
        this.ordreInstitution = ordreInstitution;
        this.ordrePrefixe = ordrePrefixe;
        this.ordreSuffixe = ordreSuffixe;
        this.positionAgence = positionAgence;
        this.positionCategorie = positionCategorie;
        this.positionCompteur = positionCompteur;
        this.positionInstitution = positionInstitution;
        this.prefixe = prefixe;
        this.suffixe = suffixe;
        this.version = version;
        this.useMultidevise = useMultidevise;
        this.noteAbsenceDeductible = noteAbsenceDeductible;
        this.noteAbsenceNonDeductible = noteAbsenceNonDeductible;
        this.noteAbsenceNonDeductibleComplement = noteAbsenceNonDeductibleComplement;
        this.noteInterruptionAbsence = noteInterruptionAbsence;
        this.compteurCollecteur = compteurCollecteur;
        this.comptePEL = comptePEL;
        this.compteInteretPEL = compteInteretPEL;
        this.fkCategorie = fkCategorie;
        this.fkCompteurMembreCategorie = fkCompteurMembreCategorie;
        this.fkCompteurMembreInstitution = fkCompteurMembreInstitution;
        this.fkCompteurMembreAgence = fkCompteurMembreAgence;
        this.agence = agence;
        this.institution = institution;
        this.compteurMembre = compteurMembre;
    }

    public Long getIdParametre() {
        return idParametre;
    }

    public void setIdParametre(Long idParametre) {
        this.idParametre = idParametre;
    }

    public Integer getMaxiJourOuvert() {
        return maxiJourOuvert;
    }

    public void setMaxiJourOuvert(Integer maxiJourOuvert) {
        this.maxiJourOuvert = maxiJourOuvert;
    }

    public Integer getOrdreAgence() {
        return ordreAgence;
    }

    public void setOrdreAgence(Integer ordreAgence) {
        this.ordreAgence = ordreAgence;
    }

    public Integer getOrdreCategorie() {
        return ordreCategorie;
    }

    public void setOrdreCategorie(Integer ordreCategorie) {
        this.ordreCategorie = ordreCategorie;
    }

    public Integer getOrdreCompteur() {
        return ordreCompteur;
    }

    public void setOrdreCompteur(Integer ordreCompteur) {
        this.ordreCompteur = ordreCompteur;
    }

    public Integer getOrdreInstitution() {
        return ordreInstitution;
    }

    public void setOrdreInstitution(Integer ordreInstitution) {
        this.ordreInstitution = ordreInstitution;
    }

    public Integer getOrdrePrefixe() {
        return ordrePrefixe;
    }

    public void setOrdrePrefixe(Integer ordrePrefixe) {
        this.ordrePrefixe = ordrePrefixe;
    }

    public Integer getOrdreSuffixe() {
        return ordreSuffixe;
    }

    public void setOrdreSuffixe(Integer ordreSuffixe) {
        this.ordreSuffixe = ordreSuffixe;
    }

    public Integer getPositionAgence() {
        return positionAgence;
    }

    public void setPositionAgence(Integer positionAgence) {
        this.positionAgence = positionAgence;
    }

    public Integer getPositionCategorie() {
        return positionCategorie;
    }

    public void setPositionCategorie(Integer positionCategorie) {
        this.positionCategorie = positionCategorie;
    }

    public Integer getPositionCompteur() {
        return positionCompteur;
    }

    public void setPositionCompteur(Integer positionCompteur) {
        this.positionCompteur = positionCompteur;
    }

    public Integer getPositionInstitution() {
        return positionInstitution;
    }

    public void setPositionInstitution(Integer positionInstitution) {
        this.positionInstitution = positionInstitution;
    }

    public String getPrefixe() {
        return prefixe;
    }

    public void setPrefixe(String prefixe) {
        this.prefixe = prefixe;
    }

    public String getSuffixe() {
        return suffixe;
    }

    public void setSuffixe(String suffixe) {
        this.suffixe = suffixe;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getUseMultidevise() {
        return useMultidevise;
    }

    public void setUseMultidevise(String useMultidevise) {
        this.useMultidevise = useMultidevise;
    }

    public String getNoteAbsenceDeductible() {
        return noteAbsenceDeductible;
    }

    public void setNoteAbsenceDeductible(String noteAbsenceDeductible) {
        this.noteAbsenceDeductible = noteAbsenceDeductible;
    }

    public String getNoteAbsenceNonDeductible() {
        return noteAbsenceNonDeductible;
    }

    public void setNoteAbsenceNonDeductible(String noteAbsenceNonDeductible) {
        this.noteAbsenceNonDeductible = noteAbsenceNonDeductible;
    }

    public String getNoteAbsenceNonDeductibleComplement() {
        return noteAbsenceNonDeductibleComplement;
    }

    public void setNoteAbsenceNonDeductibleComplement(String noteAbsenceNonDeductibleComplement) {
        this.noteAbsenceNonDeductibleComplement = noteAbsenceNonDeductibleComplement;
    }

    public String getNoteInterruptionAbsence() {
        return noteInterruptionAbsence;
    }

    public void setNoteInterruptionAbsence(String noteInterruptionAbsence) {
        this.noteInterruptionAbsence = noteInterruptionAbsence;
    }

    public Long getCompteurCollecteur() {
        return compteurCollecteur;
    }

    public void setCompteurCollecteur(Long compteurCollecteur) {
        this.compteurCollecteur = compteurCollecteur;
    }

    public String getComptePEL() {
        return comptePEL;
    }

    public void setComptePEL(String comptePEL) {
        this.comptePEL = comptePEL;
    }

    public String getCompteInteretPEL() {
        return compteInteretPEL;
    }

    public void setCompteInteretPEL(String compteInteretPEL) {
        this.compteInteretPEL = compteInteretPEL;
    }

    public String getFkCategorie() {
        return fkCategorie;
    }

    public void setFkCategorie(String fkCategorie) {
        this.fkCategorie = fkCategorie;
    }

    public String getFkCompteurMembreCategorie() {
        return fkCompteurMembreCategorie;
    }

    public void setFkCompteurMembreCategorie(String fkCompteurMembreCategorie) {
        this.fkCompteurMembreCategorie = fkCompteurMembreCategorie;
    }

    public String getFkCompteurMembreInstitution() {
        return fkCompteurMembreInstitution;
    }

    public void setFkCompteurMembreInstitution(String fkCompteurMembreInstitution) {
        this.fkCompteurMembreInstitution = fkCompteurMembreInstitution;
    }

    public String getFkCompteurMembreAgence() {
        return fkCompteurMembreAgence;
    }

    public void setFkCompteurMembreAgence(String fkCompteurMembreAgence) {
        this.fkCompteurMembreAgence = fkCompteurMembreAgence;
    }

    public Agence getAgence() {
        return agence;
    }

    public void setAgence(Agence agence) {
        this.agence = agence;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public CompteurMembre getCompteurMembre() {
        return compteurMembre;
    }

    public void setCompteurMembre(CompteurMembre compteurMembre) {
        this.compteurMembre = compteurMembre;
    }

    @Override
    public String toString() {
        return "Parametre("
            + "idParametre=" + idParametre
            + ")";
    }
}

package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * AGENT_CREDIT – credit agent assigned to a branch.
 * Spec: DB-FINA-202112-001 p.8-9.
 */
@Entity
@Table(name = "AGENT_CREDIT")
@DynamicUpdate
public class AgentCredit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank
    @Size(max = 25)
    @Column(name = "CODE_AGENT", length = 25, nullable = false)
    private String codeAgent;

    /** Internal numeric code. */
    @Column(name = "code")
    private Integer code;

    /** Follow-up tracking code. */
    @Column(name = "codesuivi")
    private Integer codesuivi;

    /** Minimum credit duration (months) this agent may manage. */
    @Column(name = "duree1")
    private Integer duree1;

    /** Maximum credit duration (months) this agent may manage. */
    @Column(name = "duree2")
    private Integer duree2;

    /**
     * Agent active flag — AN/255 holding "O" (oui) or "N" (non).
     * NOTE: spec column type is AN/255, not a Boolean/BIT.
     */
    @Size(max = 255)
    @Column(name = "actif", length = 255)
    private String actif = "O";

    /** Minimum credit amount this agent may manage. */
    @Column(name = "montant1", precision = 19, scale = 4)
    private BigDecimal montant1;

    /** Maximum credit amount this agent may manage. */
    @Column(name = "Montant2", precision = 19, scale = 4)
    private BigDecimal montant2;

    /** Agent identity / full name. */
    @Size(max = 255)
    @Column(name = "DESIGNATION", length = 255)
    private String designation;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    /** FK to ADRESSE – address identifier. */
    @Size(max = 48)
    @Column(name = "ADRESSE_IDADRESSE", length = 48)
    private String adresseIdAdresse;

    /** Linked application user code. */
    @Size(max = 48)
    @Column(name = "UTILISATEUR", length = 48)
    private String utilisateur;

    /** System information string. */
    @Size(max = 200)
    @Column(name = "sysinfo", length = 200)
    private String sysinfo;

    /** UI language preference code (e.g. "FR", "EN"). */
    @Size(max = 5)
    @Column(name = "tlanguage", length = 5)
    private String tlanguage;

    /** Login account name used for remote DB access. */
    @Size(max = 20)
    @Column(name = "account", length = 20)
    private String account;

    /** Password for remote DB access (stored encrypted). */
    @Size(max = 100)
    @Column(name = "password", length = 100)
    private String password;

    /** Agent gender: "M" or "F". */
    @Size(max = 1)
    @Column(name = "sexe_agent", length = 1)
    private String sexeAgent;

    /** Agent function / role label. */
    @Size(max = 100)
    @Column(name = "tfunction", length = 100)
    private String tfunction;

    /** Agent user code in the application. */
    @Size(max = 100)
    @Column(name = "usercode", length = 100)
    private String usercode;

    /** Whether the agent is currently in use / logged in (0 or 1). */
    @Column(name = "inuse")
    private Integer inuse;

    /** Number of working days available for this agent. */
    @Column(name = "vdays")
    private Integer vdays;

    /** Agent validity start date. */
    @Column(name = "vstart")
    private LocalDate vstart;

    /** FK to AGENCE – branch the agent is assigned to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CODE_AGENCE", referencedColumnName = "CODE_AGENCE",
                foreignKey = @ForeignKey(name = "FK_AGENT_CREDIT_AGENCE"))
    private Agence agence;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public AgentCredit() {
    }

    public AgentCredit(String codeAgent, Integer code, Integer codesuivi, Integer duree1, Integer duree2, String actif, BigDecimal montant1, BigDecimal montant2, String designation, Integer version, String adresseIdAdresse, String utilisateur, String sysinfo, String tlanguage, String account, String password, String sexeAgent, String tfunction, String usercode, Integer inuse, Integer vdays, LocalDate vstart, Agence agence) {
        this.codeAgent = codeAgent;
        this.code = code;
        this.codesuivi = codesuivi;
        this.duree1 = duree1;
        this.duree2 = duree2;
        this.actif = actif;
        this.montant1 = montant1;
        this.montant2 = montant2;
        this.designation = designation;
        this.version = version;
        this.adresseIdAdresse = adresseIdAdresse;
        this.utilisateur = utilisateur;
        this.sysinfo = sysinfo;
        this.tlanguage = tlanguage;
        this.account = account;
        this.password = password;
        this.sexeAgent = sexeAgent;
        this.tfunction = tfunction;
        this.usercode = usercode;
        this.inuse = inuse;
        this.vdays = vdays;
        this.vstart = vstart;
        this.agence = agence;
    }

    public String getCodeAgent() {
        return codeAgent;
    }

    public void setCodeAgent(String codeAgent) {
        this.codeAgent = codeAgent;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getCodesuivi() {
        return codesuivi;
    }

    public void setCodesuivi(Integer codesuivi) {
        this.codesuivi = codesuivi;
    }

    public Integer getDuree1() {
        return duree1;
    }

    public void setDuree1(Integer duree1) {
        this.duree1 = duree1;
    }

    public Integer getDuree2() {
        return duree2;
    }

    public void setDuree2(Integer duree2) {
        this.duree2 = duree2;
    }

    public String getActif() {
        return actif;
    }

    public void setActif(String actif) {
        this.actif = actif;
    }

    public BigDecimal getMontant1() {
        return montant1;
    }

    public void setMontant1(BigDecimal montant1) {
        this.montant1 = montant1;
    }

    public BigDecimal getMontant2() {
        return montant2;
    }

    public void setMontant2(BigDecimal montant2) {
        this.montant2 = montant2;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getAdresseIdAdresse() {
        return adresseIdAdresse;
    }

    public void setAdresseIdAdresse(String adresseIdAdresse) {
        this.adresseIdAdresse = adresseIdAdresse;
    }

    public String getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(String utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getSysinfo() {
        return sysinfo;
    }

    public void setSysinfo(String sysinfo) {
        this.sysinfo = sysinfo;
    }

    public String getTlanguage() {
        return tlanguage;
    }

    public void setTlanguage(String tlanguage) {
        this.tlanguage = tlanguage;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSexeAgent() {
        return sexeAgent;
    }

    public void setSexeAgent(String sexeAgent) {
        this.sexeAgent = sexeAgent;
    }

    public String getTfunction() {
        return tfunction;
    }

    public void setTfunction(String tfunction) {
        this.tfunction = tfunction;
    }

    public String getUsercode() {
        return usercode;
    }

    public void setUsercode(String usercode) {
        this.usercode = usercode;
    }

    public Integer getInuse() {
        return inuse;
    }

    public void setInuse(Integer inuse) {
        this.inuse = inuse;
    }

    public Integer getVdays() {
        return vdays;
    }

    public void setVdays(Integer vdays) {
        this.vdays = vdays;
    }

    public LocalDate getVstart() {
        return vstart;
    }

    public void setVstart(LocalDate vstart) {
        this.vstart = vstart;
    }

    public Agence getAgence() {
        return agence;
    }

    public void setAgence(Agence agence) {
        this.agence = agence;
    }

    @Override
    public String toString() {
        return "AgentCredit("
            + "codeAgent=" + codeAgent
            + ")";
    }
}

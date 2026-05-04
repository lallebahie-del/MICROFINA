package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "analyse_financiere")
@DynamicUpdate
public class AnalyseFinanciere implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum TypeAnalyse { INFERIEURE, SUPERIEURE }
    public enum AvisAgent   { FAVORABLE, DEFAVORABLE, RESERVE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ANALYSE", nullable = false)
    private Long idAnalyse;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_id", referencedColumnName = "IDCREDIT", unique = true,
                foreignKey = @ForeignKey(name = "FK_analyse_financiere_Credits"))
    private Credits credit;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE_ANALYSE", length = 20)
    private TypeAnalyse typeAnalyse;

    @Column(name = "REVENUS_MENSUELS", precision = 19, scale = 4)
    private BigDecimal revenusMensuels;

    @Column(name = "CHARGES_MENSUELLES", precision = 19, scale = 4)
    private BigDecimal chargesMensuelles;

    @Column(name = "CAPACITE_REMBOURSEMENT", precision = 19, scale = 4)
    private BigDecimal capaciteRemboursement;

    @Column(name = "RATIO_ENDETTEMENT", precision = 19, scale = 4)
    private BigDecimal ratioEndettement;

    @Column(name = "TOTAL_ACTIF", precision = 19, scale = 4)
    private BigDecimal totalActif;

    @Column(name = "TOTAL_PASSIF", precision = 19, scale = 4)
    private BigDecimal totalPassif;

    @Column(name = "INDICATEURS_JSON", columnDefinition = "NVARCHAR(MAX)")
    private String indicateursJson;

    @Size(max = 500)
    @Column(name = "COMMENTAIRE", length = 500)
    private String commentaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "AVIS_AGENT", length = 20)
    private AvisAgent avisAgent;

    @Column(name = "DATE_ANALYSE")
    private LocalDateTime dateAnalyse;

    @Size(max = 48)
    @Column(name = "UTILISATEUR", length = 48)
    private String utilisateur;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    public AnalyseFinanciere() {}

    // -- Getters / Setters --

    public Long getIdAnalyse() { return idAnalyse; }
    public void setIdAnalyse(Long idAnalyse) { this.idAnalyse = idAnalyse; }
    public Credits getCredit() { return credit; }
    public void setCredit(Credits credit) { this.credit = credit; }
    public TypeAnalyse getTypeAnalyse() { return typeAnalyse; }
    public void setTypeAnalyse(TypeAnalyse typeAnalyse) { this.typeAnalyse = typeAnalyse; }
    public BigDecimal getRevenusMensuels() { return revenusMensuels; }
    public void setRevenusMensuels(BigDecimal revenusMensuels) { this.revenusMensuels = revenusMensuels; }
    public BigDecimal getChargesMensuelles() { return chargesMensuelles; }
    public void setChargesMensuelles(BigDecimal chargesMensuelles) { this.chargesMensuelles = chargesMensuelles; }
    public BigDecimal getCapaciteRemboursement() { return capaciteRemboursement; }
    public void setCapaciteRemboursement(BigDecimal capaciteRemboursement) { this.capaciteRemboursement = capaciteRemboursement; }
    public BigDecimal getRatioEndettement() { return ratioEndettement; }
    public void setRatioEndettement(BigDecimal ratioEndettement) { this.ratioEndettement = ratioEndettement; }
    public BigDecimal getTotalActif() { return totalActif; }
    public void setTotalActif(BigDecimal totalActif) { this.totalActif = totalActif; }
    public BigDecimal getTotalPassif() { return totalPassif; }
    public void setTotalPassif(BigDecimal totalPassif) { this.totalPassif = totalPassif; }
    public String getIndicateursJson() { return indicateursJson; }
    public void setIndicateursJson(String indicateursJson) { this.indicateursJson = indicateursJson; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public AvisAgent getAvisAgent() { return avisAgent; }
    public void setAvisAgent(AvisAgent avisAgent) { this.avisAgent = avisAgent; }
    public LocalDateTime getDateAnalyse() { return dateAnalyse; }
    public void setDateAnalyse(LocalDateTime dateAnalyse) { this.dateAnalyse = dateAnalyse; }
    public String getUtilisateur() { return utilisateur; }
    public void setUtilisateur(String utilisateur) { this.utilisateur = utilisateur; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @Override
    public String toString() {
        return "AnalyseFinanciere(idAnalyse=" + idAnalyse + ")";
    }
}

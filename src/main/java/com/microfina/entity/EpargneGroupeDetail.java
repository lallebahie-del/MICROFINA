package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * EpargneGroupeDetail – per-member breakdown of a group savings movement.
 *
 * Each row specifies the credit/debit share of one {@link MembreGroupe}
 * for a given {@link EpargneGroupe} movement.
 * Unique constraint: one detail row per (EpargneGroupe, membreGroupe).
 *
 * DDL source of truth: P2-008-CREATE-TABLE-EpargneGroupeDetail.xml.
 * Spec p.32.
 */
@Entity
@Table(
    name = "EpargneGroupeDetail",
    uniqueConstraints = @UniqueConstraint(
        name        = "UQ_EpargneGroupeDetail_epargne_membre",
        columnNames = {"EpargneGroupe", "membreGroupe"}
    )
)
@DynamicUpdate
public class EpargneGroupeDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── PK ────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEpargneGD", nullable = false)
    private Long idEpargneGD;

    // ── Group member code (AN/255 per spec) ───────────────────────

    @Size(max = 255)
    @Column(name = "CODEMEMBREGROUPPE", length = 255)
    private String codeMembreGrouppe;

    // ── Credit / debit split ──────────────────────────────────────

    @Column(name = "CREDIT", precision = 19, scale = 4)
    private BigDecimal credit;

    @Column(name = "DEBIT", precision = 19, scale = 4)
    private BigDecimal debit;

    // ── Piece / date reference ────────────────────────────────────

    @Size(max = 50)
    @Column(name = "numpiece", length = 50)
    private String numPiece;

    @Column(name = "dateoperation")
    private LocalDate dateOperation;

    // ── Optimistic lock ───────────────────────────────────────────

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── FKs ───────────────────────────────────────────────────────

    /**
     * The group savings movement this detail belongs to.
     * FK to EPARGNE (EpargneGroupe discriminator row).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "EpargneGroupe",
        referencedColumnName = "IDEPARGNE",
        foreignKey           = @ForeignKey(name = "FK_EpargneGroupeDetail_EPARGNE")
    )
    private EpargneGroupe epargneGroupe;

    /**
     * The group member whose share this detail records.
     * FK to MembreGroupe (PK = CODE_TIERS, NVARCHAR(50)).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name                 = "membreGroupe",
        referencedColumnName = "CODE_TIERS",
        foreignKey           = @ForeignKey(name = "FK_EpargneGroupeDetail_MembreGroupe")
    )
    private MembreGroupe membreGroupe;

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public EpargneGroupeDetail() {
    }

    public EpargneGroupeDetail(Long idEpargneGD, String codeMembreGrouppe, BigDecimal credit, BigDecimal debit, String numPiece, LocalDate dateOperation, Integer version, EpargneGroupe epargneGroupe, MembreGroupe membreGroupe) {
        this.idEpargneGD = idEpargneGD;
        this.codeMembreGrouppe = codeMembreGrouppe;
        this.credit = credit;
        this.debit = debit;
        this.numPiece = numPiece;
        this.dateOperation = dateOperation;
        this.version = version;
        this.epargneGroupe = epargneGroupe;
        this.membreGroupe = membreGroupe;
    }

    public Long getIdEpargneGD() {
        return idEpargneGD;
    }

    public void setIdEpargneGD(Long idEpargneGD) {
        this.idEpargneGD = idEpargneGD;
    }

    public String getCodeMembreGrouppe() {
        return codeMembreGrouppe;
    }

    public void setCodeMembreGrouppe(String codeMembreGrouppe) {
        this.codeMembreGrouppe = codeMembreGrouppe;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public void setDebit(BigDecimal debit) {
        this.debit = debit;
    }

    public String getNumPiece() {
        return numPiece;
    }

    public void setNumPiece(String numPiece) {
        this.numPiece = numPiece;
    }

    public LocalDate getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(LocalDate dateOperation) {
        this.dateOperation = dateOperation;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public EpargneGroupe getEpargneGroupe() {
        return epargneGroupe;
    }

    public void setEpargneGroupe(EpargneGroupe epargneGroupe) {
        this.epargneGroupe = epargneGroupe;
    }

    public MembreGroupe getMembreGroupe() {
        return membreGroupe;
    }

    public void setMembreGroupe(MembreGroupe membreGroupe) {
        this.membreGroupe = membreGroupe;
    }

    @Override
    public String toString() {
        return "EpargneGroupeDetail("
            + "idEpargneGD=" + idEpargneGD
            + ")";
    }
}

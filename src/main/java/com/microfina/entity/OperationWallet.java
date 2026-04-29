package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * OperationWallet — transaction de monnaie mobile effectuée via Bankily.
 *
 * <h2>Types d'opérations</h2>
 * <ul>
 *   <li>{@code DEBLOCAGE}     — décaissement d'un crédit vers le wallet du membre</li>
 *   <li>{@code REMBOURSEMENT} — collecte d'un remboursement depuis le wallet du membre</li>
 *   <li>{@code DEPOT_EPARGNE} — dépôt d'épargne mobile</li>
 * </ul>
 *
 * <h2>Cycle de vie du statut</h2>
 * <pre>
 *   EN_ATTENTE → CONFIRME  (callback Bankily SUCCESS)
 *             → REJETE    (callback Bankily FAILED / INSUFFICIENT_FUNDS)
 *             → EXPIRE    (délai dépassé sans callback)
 *   tout statut → ANNULE  (annulation manuelle avant confirmation)
 * </pre>
 *
 * <p>Table cible : {@code OperationWallet} — DDL : P10-601.</p>
 */
@Entity
@Table(name = "OperationWallet")
public class OperationWallet implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Constantes de statut ──────────────────────────────────────────
    public static final String STATUT_EN_ATTENTE = "EN_ATTENTE";
    public static final String STATUT_CONFIRME   = "CONFIRME";
    public static final String STATUT_REJETE     = "REJETE";
    public static final String STATUT_ANNULE     = "ANNULE";
    public static final String STATUT_EXPIRE     = "EXPIRE";

    // ── Constantes de type ────────────────────────────────────────────
    public static final String TYPE_DEBLOCAGE     = "DEBLOCAGE";
    public static final String TYPE_REMBOURSEMENT = "REMBOURSEMENT";
    public static final String TYPE_DEPOT_EPARGNE = "DEPOT_EPARGNE";

    // ── Identifiant ───────────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // ── Références ────────────────────────────────────────────────────

    /** Référence interne MFI, unique, générée côté applicatif. */
    @NotBlank
    @Size(max = 60)
    @Column(name = "reference_mfi", length = 60, nullable = false, unique = true)
    private String referenceMfi;

    /** Référence renvoyée par Bankily après initiation de la transaction. */
    @Size(max = 100)
    @Column(name = "reference_bankily", length = 100)
    private String referenceBankily;

    // ── Portefeuille ──────────────────────────────────────────────────

    /** Numéro de téléphone du wallet Bankily (format : +222XXXXXXXX). */
    @NotBlank
    @Size(max = 30)
    @Column(name = "numero_telephone", length = 30, nullable = false)
    private String numeroTelephone;

    // ── Montant & type ────────────────────────────────────────────────

    @NotNull
    @DecimalMin(value = "0.01", message = "Le montant doit être positif")
    @Column(name = "montant", nullable = false, precision = 19, scale = 4)
    private BigDecimal montant;

    /** DEBLOCAGE | REMBOURSEMENT | DEPOT_EPARGNE */
    @NotBlank
    @Size(max = 30)
    @Column(name = "type_operation", length = 30, nullable = false)
    private String typeOperation;

    /** EN_ATTENTE | CONFIRME | REJETE | ANNULE | EXPIRE */
    @NotBlank
    @Size(max = 20)
    @Column(name = "statut", length = 20, nullable = false)
    private String statut = STATUT_EN_ATTENTE;

    // ── Chronologie ───────────────────────────────────────────────────

    @NotNull
    @Column(name = "date_operation", nullable = false)
    private LocalDate dateOperation;

    /** Horodatage de la confirmation ou du rejet par Bankily. */
    @Column(name = "date_confirmation")
    private LocalDateTime dateConfirmation;

    /** Délai d'expiration en minutes (défaut 30). */
    @Column(name = "delai_expiration_min", nullable = false)
    private Integer delaiExpirationMin = 30;

    // ── Motif ─────────────────────────────────────────────────────────

    @Size(max = 255)
    @Column(name = "motif", length = 255)
    private String motif;

    // ── Réponse Bankily ───────────────────────────────────────────────

    /** Code retour Bankily (ex. "SUCCESS", "INSUFFICIENT_FUNDS"). */
    @Size(max = 50)
    @Column(name = "code_retour", length = 50)
    private String codeRetour;

    /** Message textuel renvoyé par Bankily. */
    @Size(max = 500)
    @Column(name = "message_retour", length = 500)
    private String messageRetour;

    /** Payload JSON brut du callback (pour audit et rejeu). */
    @Column(name = "payload_callback", columnDefinition = "NVARCHAR(MAX)")
    private String payloadCallback;

    // ── Liens métier ──────────────────────────────────────────────────

    /** FK → membres.NUM_MEMBRE (nullable : transactions sans membre associé). */
    @Size(max = 25)
    @Column(name = "num_membre", length = 25)
    private String numMembre;

    /** FK → Credits.IDCREDIT (nullable : renseigné pour DEBLOCAGE et REMBOURSEMENT). */
    @Column(name = "id_credit")
    private Long idCredit;

    /** FK → AGENCE.CODE_AGENCE. */
    @Size(max = 25)
    @Column(name = "code_agence", length = 25)
    private String codeAgence;

    // ── Audit ─────────────────────────────────────────────────────────

    @Size(max = 100)
    @Column(name = "utilisateur", length = 100)
    private String utilisateur;

    // ── Optimistic lock ───────────────────────────────────────────────
    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version = 0;

    // ── Constructeurs ─────────────────────────────────────────────────
    public OperationWallet() {}

    // ── Getters / Setters ─────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReferenceMfi() { return referenceMfi; }
    public void setReferenceMfi(String v) { this.referenceMfi = v; }

    public String getReferenceBankily() { return referenceBankily; }
    public void setReferenceBankily(String v) { this.referenceBankily = v; }

    public String getNumeroTelephone() { return numeroTelephone; }
    public void setNumeroTelephone(String v) { this.numeroTelephone = v; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal v) { this.montant = v; }

    public String getTypeOperation() { return typeOperation; }
    public void setTypeOperation(String v) { this.typeOperation = v; }

    public String getStatut() { return statut; }
    public void setStatut(String v) { this.statut = v; }

    public LocalDate getDateOperation() { return dateOperation; }
    public void setDateOperation(LocalDate v) { this.dateOperation = v; }

    public LocalDateTime getDateConfirmation() { return dateConfirmation; }
    public void setDateConfirmation(LocalDateTime v) { this.dateConfirmation = v; }

    public Integer getDelaiExpirationMin() { return delaiExpirationMin; }
    public void setDelaiExpirationMin(Integer v) { this.delaiExpirationMin = v; }

    public String getMotif() { return motif; }
    public void setMotif(String v) { this.motif = v; }

    public String getCodeRetour() { return codeRetour; }
    public void setCodeRetour(String v) { this.codeRetour = v; }

    public String getMessageRetour() { return messageRetour; }
    public void setMessageRetour(String v) { this.messageRetour = v; }

    public String getPayloadCallback() { return payloadCallback; }
    public void setPayloadCallback(String v) { this.payloadCallback = v; }

    public String getNumMembre() { return numMembre; }
    public void setNumMembre(String v) { this.numMembre = v; }

    public Long getIdCredit() { return idCredit; }
    public void setIdCredit(Long v) { this.idCredit = v; }

    public String getCodeAgence() { return codeAgence; }
    public void setCodeAgence(String v) { this.codeAgence = v; }

    public String getUtilisateur() { return utilisateur; }
    public void setUtilisateur(String v) { this.utilisateur = v; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer v) { this.version = v; }

    /** @return true si l'opération est dans un état terminal (plus de transitions possibles) */
    public boolean isTerminal() {
        return STATUT_CONFIRME.equals(statut)
            || STATUT_REJETE.equals(statut)
            || STATUT_ANNULE.equals(statut)
            || STATUT_EXPIRE.equals(statut);
    }

    @Override
    public String toString() {
        return "OperationWallet(id=" + id
                + ", ref=" + referenceMfi
                + ", type=" + typeOperation
                + ", montant=" + montant
                + ", statut=" + statut + ")";
    }
}

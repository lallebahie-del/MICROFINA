package com.microfina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * EpargneGroupe – SINGLE_TABLE discriminator subtype of {@link Epargne}.
 *
 * Represents a savings movement issued for a group member account.
 * Adds no physical columns beyond the inherited {@link Epargne} columns;
 * the DTYPE value "EpargneGroupe" distinguishes it at query time.
 *
 * Per-member breakdown of a group movement is stored in
 * {@link EpargneGroupeDetail}.
 *
 * DDL: no separate table (SINGLE_TABLE – shares EPARGNE).
 * Spec p.74 (diagram), p.75 description.
 */
@Entity
@DiscriminatorValue("EpargneGroupe")
@DynamicUpdate
public class EpargneGroupe extends Epargne implements Serializable {

    private static final long serialVersionUID = 1L;

    // Zero additional columns – discriminator only.
    // EpargneGroupeDetail rows link back via FK EpargneGroupe → EPARGNE.IDEPARGNE.

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public EpargneGroupe() {
    }

    @Override
    public String toString() {
        return "EpargneGroupe(" + super.toString() + ")";
    }
}

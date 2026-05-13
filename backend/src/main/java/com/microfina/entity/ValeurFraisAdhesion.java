package com.microfina.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * ValeurFraisAdhesion – membership-fee specialisation of {@link ValeurFrais}.
 *
 * Uses SINGLE_TABLE inheritance: no separate DB table is created.
 * The discriminator value "ValeurFraisAdhesion" stored in the DTYPE column
 * of ValeurFrais identifies rows belonging to this subtype.
 *
 * Adhesion-specific columns (formeJuridique, age_min, age_max) are declared
 * on the parent class and shared in the single table.
 */
@Entity
@DiscriminatorValue("ValeurFraisAdhesion")
@DynamicUpdate
public class ValeurFraisAdhesion extends ValeurFrais implements Serializable {

    private static final long serialVersionUID = 1L;

    // All adhesion-specific columns (formeJuridique, ageMin, ageMax)
    // are declared in the parent ValeurFrais class and stored in the
    // same table. No additional columns needed here.

    // ── Generated (was Lombok) ──────────────────────────────────────────────

    public ValeurFraisAdhesion() {
    }

    @Override
    public String toString() {
        return "ValeurFraisAdhesion(" + super.toString() + ")";
    }
}

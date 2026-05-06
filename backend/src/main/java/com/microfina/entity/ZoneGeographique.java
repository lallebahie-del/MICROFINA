package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * ZoneGeographique — zone géographique opérationnelle de la MFI.
 *
 * <h2>Hiérarchie administrative mauritanienne</h2>
 * <pre>
 *   WILAYA (15) → MOUGHATAA → COMMUNE → QUARTIER
 * </pre>
 *
 * <h2>Enrichissement cartographique (P10-501)</h2>
 * <p>Les colonnes {@code latitude}, {@code longitude}, {@code geojsonPolygon}
 * et {@code typeZone} sont ajoutées en Phase 10.5 pour alimenter
 * l'API GeoJSON du module cartographie.</p>
 *
 * <p>Table cible : {@code zoneGeographique} — DDL : P1-005 + P10-501.</p>
 */
@Entity
@Table(name = "zoneGeographique")
@DynamicUpdate
public class ZoneGeographique implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Identifiant ───────────────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idZoneGeographique", nullable = false)
    private Long idZoneGeographique;

    // ── Dénomination ──────────────────────────────────────────────────────
    @NotBlank
    @Size(max = 255)
    @Column(name = "libelleZoneGeographique", length = 255, nullable = false)
    private String libelleZoneGeographique;

    @Size(max = 16)
    @Column(name = "code", length = 16)
    private String code;

    /** FK vers la table Ville (Phase 1, non contrainte ici). */
    @Column(name = "ville")
    private Long ville;

    // ── Cartographie (P10-501) ────────────────────────────────────────────

    /** Latitude du centroïde WGS-84 (±90°). */
    @Column(name = "latitude", precision = 10, scale = 7)
    private java.math.BigDecimal latitude;

    /** Longitude du centroïde WGS-84 (±180°). */
    @Column(name = "longitude", precision = 10, scale = 7)
    private java.math.BigDecimal longitude;

    /**
     * Géométrie GeoJSON complète (Polygon ou MultiPolygon).
     * Stockée comme chaîne JSON : {@code {"type":"Polygon","coordinates":[[[...]]]}}
     */
    @Column(name = "geojson_polygon", columnDefinition = "NVARCHAR(MAX)")
    private String geojsonPolygon;

    /**
     * Niveau administratif : WILAYA | MOUGHATAA | COMMUNE | QUARTIER.
     */
    @Size(max = 50)
    @Column(name = "type_zone", length = 50)
    private String typeZone;

    /** Zone parente (hiérarchie). */
    @Column(name = "zone_parente")
    private Long zoneParente;

    /** Zone visible sur la carte (défaut : true). */
    @Column(name = "actif")
    private Boolean actif = true;

    // ── Optimistic lock ───────────────────────────────────────────────────
    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    // ── Constructeurs ─────────────────────────────────────────────────────
    public ZoneGeographique() {}

    // ── Getters / Setters ─────────────────────────────────────────────────
    public Long getIdZoneGeographique() { return idZoneGeographique; }
    public void setIdZoneGeographique(Long v) { this.idZoneGeographique = v; }

    public String getLibelleZoneGeographique() { return libelleZoneGeographique; }
    public void setLibelleZoneGeographique(String v) { this.libelleZoneGeographique = v; }

    public String getCode() { return code; }
    public void setCode(String v) { this.code = v; }

    public Long getVille() { return ville; }
    public void setVille(Long v) { this.ville = v; }

    public java.math.BigDecimal getLatitude() { return latitude; }
    public void setLatitude(java.math.BigDecimal v) { this.latitude = v; }

    public java.math.BigDecimal getLongitude() { return longitude; }
    public void setLongitude(java.math.BigDecimal v) { this.longitude = v; }

    public String getGeojsonPolygon() { return geojsonPolygon; }
    public void setGeojsonPolygon(String v) { this.geojsonPolygon = v; }

    public String getTypeZone() { return typeZone; }
    public void setTypeZone(String v) { this.typeZone = v; }

    public Long getZoneParente() { return zoneParente; }
    public void setZoneParente(Long v) { this.zoneParente = v; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean v) { this.actif = v; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer v) { this.version = v; }

    /** @return true si cette zone possède des coordonnées de centroïde */
    public boolean hasCentroid() {
        return latitude != null && longitude != null;
    }

    @Override
    public String toString() {
        return "ZoneGeographique(id=" + idZoneGeographique
                + ", code=" + code
                + ", libelle=" + libelleZoneGeographique
                + ", type=" + typeZone + ")";
    }
}

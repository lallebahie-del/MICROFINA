package com.pfe.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * GeoJsonDto — enveloppes GeoJSON RFC 7946 pour l'API cartographie.
 *
 * <p>Toutes les classes sont des records Java imbriqués dans cette classe
 * conteneur. Jackson sérialise directement chaque record en JSON conforme
 * RFC 7946 sans configuration supplémentaire.</p>
 *
 * <h2>Exemples de réponses</h2>
 * <pre>
 * // FeatureCollection (liste de zones)
 * {
 *   "type": "FeatureCollection",
 *   "features": [ ... ]
 * }
 *
 * // Feature (zone individuelle)
 * {
 *   "type": "Feature",
 *   "geometry": { "type": "Polygon", "coordinates": [[[lng, lat], ...]] },
 *   "properties": { "id": 1, "libelle": "Nouakchott", "typeZone": "WILAYA", ... }
 * }
 * </pre>
 */
public final class GeoJsonDto {

    private GeoJsonDto() {}

    // ── FeatureCollection ─────────────────────────────────────────────────

    /**
     * Conteneur GeoJSON FeatureCollection.
     * Champ {@code type} toujours égal à {@code "FeatureCollection"}.
     */
    public record FeatureCollection(
            String        type,
            List<Feature> features
    ) {
        /** Constructeur de confort — remplit automatiquement {@code type}. */
        public static FeatureCollection of(List<Feature> features) {
            return new FeatureCollection("FeatureCollection", features);
        }
    }

    // ── Feature ───────────────────────────────────────────────────────────

    /**
     * Entité GeoJSON Feature.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Feature(
            String               type,
            Geometry             geometry,
            Map<String, Object>  properties
    ) {
        public static Feature of(Geometry geometry, Map<String, Object> properties) {
            return new Feature("Feature", geometry, properties);
        }
    }

    // ── Geometry ──────────────────────────────────────────────────────────

    /**
     * Géométrie GeoJSON.
     * {@code coordinates} contient :
     * <ul>
     *   <li>Point → {@code [longitude, latitude]}</li>
     *   <li>Polygon → {@code [[[lng, lat], ...]]} (anneau extérieur)</li>
     *   <li>MultiPolygon → {@code [[[[lng, lat], ...]]]} </li>
     * </ul>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Geometry(
            String type,
            Object coordinates
    ) {
        /** Crée une géométrie Point depuis lat/lon. */
        public static Geometry point(double longitude, double latitude) {
            return new Geometry("Point", List.of(longitude, latitude));
        }

        /** Crée une géométrie nulle (zone sans coordonnées). */
        public static Geometry nullGeometry() {
            return null;
        }
    }

    // ── KPIs de zone ─────────────────────────────────────────────────────

    /**
     * Indicateurs de performance agrégés pour une zone géographique.
     * Inclus dans les {@code properties} de chaque Feature.
     */
    public record ZoneKpi(
            Long   idZone,
            String codeZone,
            String libelleZone,
            String typeZone,
            Long   zoneParente,

            // Membres
            int    nbMembres,
            int    nbMembresActifs,

            // Agences
            int    nbAgences,

            // Crédits
            int    nbCreditsActifs,
            double encoursBrut,
            double encoursNet,

            // PAR
            double totalArrieres,
            double tauxPar30,
            String categoriePar
    ) {}
}

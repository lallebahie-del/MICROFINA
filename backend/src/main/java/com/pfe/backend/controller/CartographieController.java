package com.pfe.backend.controller;

import com.pfe.backend.dto.GeoJsonDto.Feature;
import com.pfe.backend.dto.GeoJsonDto.FeatureCollection;
import com.pfe.backend.service.CartographieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * CartographieController — API GeoJSON RFC 7946 du module cartographie.
 *
 * <h2>Endpoints</h2>
 * <pre>
 *  GET /api/v1/cartographie/zones               → FeatureCollection (toutes zones actives)
 *  GET /api/v1/cartographie/zones?type=WILAYA   → FeatureCollection (zones filtrées par niveau)
 *  GET /api/v1/cartographie/zones/{id}          → Feature (zone individuelle + KPIs)
 *  GET /api/v1/cartographie/agences             → FeatureCollection (Points GPS des agences)
 *  GET /api/v1/cartographie/heatmap-par         → FeatureCollection (choroplèthe PAR)
 * </pre>
 *
 * <p>Tous les types de zones acceptés pour {@code ?type} :
 * {@code WILAYA}, {@code MOUGHATAA}, {@code COMMUNE}, {@code QUARTIER}.</p>
 *
 * <p>Tous les endpoints renvoient du {@code application/geo+json} (RFC 7946 §12)
 * avec fallback {@code application/json} pour compatibilité navigateur.</p>
 */
@Tag(name = "Cartographie", description = "Couches GeoJSON des zones géographiques et agences")
@RestController
@RequestMapping("/api/v1/cartographie")
@PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
public class CartographieController {

    /** Type MIME GeoJSON (RFC 7946 §12). */
    private static final String GEO_JSON = "application/geo+json";

    private final CartographieService cartographieService;

    public CartographieController(CartographieService cartographieService) {
        this.cartographieService = cartographieService;
    }

    // =========================================================================
    //  GET /zones — toutes les zones actives (filtrables par niveau)
    // =========================================================================

    /**
     * Retourne la FeatureCollection GeoJSON de toutes les zones actives,
     * enrichies de leurs KPIs opérationnels (membres, crédits, encours, PAR30).
     *
     * @param type filtre optionnel : {@code WILAYA | MOUGHATAA | COMMUNE | QUARTIER}
     * @return 200 + FeatureCollection GeoJSON
     */
    @Operation(summary = "Lister toutes les zones géographiques (GeoJSON)")
    @GetMapping(value = "/zones", produces = {GEO_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<FeatureCollection> getZones(
            @RequestParam(name = "type", required = false) String type) {

        FeatureCollection fc = cartographieService.zonesFeatureCollection(
                type != null ? type.toUpperCase() : null);
        return ResponseEntity.ok(fc);
    }

    // =========================================================================
    //  GET /zones/{id} — zone individuelle
    // =========================================================================

    /**
     * Retourne la Feature GeoJSON d'une zone géographique identifiée par son ID,
     * avec ses KPIs opérationnels complets.
     *
     * @param id identifiant de la zone ({@code idZoneGeographique})
     * @return 200 + Feature GeoJSON, ou 404 si la zone n'existe pas
     */
    @Operation(summary = "Obtenir une zone géographique par identifiant")
    @GetMapping(value = "/zones/{id}", produces = {GEO_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Feature> getZone(@PathVariable Long id) {
        return cartographieService.zoneFeature(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================================
    //  GET /agences — Points GPS des agences actives
    // =========================================================================

    /**
     * Retourne la FeatureCollection GeoJSON des agences actives.
     * Chaque agence est un Point avec ses coordonnées GPS et ses indicateurs
     * opérationnels (nb_crédits_actifs, encours_net, nb_membres).
     *
     * <p>Les agences sans coordonnées GPS sont exclues de la réponse
     * (filtre {@code latitude IS NOT NULL AND longitude IS NOT NULL} côté SQL).</p>
     *
     * @return 200 + FeatureCollection GeoJSON (Points)
     */
    @Operation(summary = "Localisation des agences (GeoJSON)")
    @GetMapping(value = "/agences", produces = {GEO_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<FeatureCollection> getAgences() {
        return ResponseEntity.ok(cartographieService.agencesFeatureCollection());
    }

    // =========================================================================
    //  GET /heatmap-par — choroplèthe PAR par zone
    // =========================================================================

    /**
     * Retourne la FeatureCollection GeoJSON de toutes les zones actives,
     * colorées selon la catégorie PAR (portefeuille à risque) la plus sévère
     * observée dans la zone.
     *
     * <p>Propriétés de chaque Feature :</p>
     * <ul>
     *   <li>{@code categoriePar} : SAIN | PAR30 | PAR90 | PAR180 | PAR180_PLUS</li>
     *   <li>{@code couleurPar}   : suggestion de couleur hex pour le rendu choroplèthe</li>
     *   <li>{@code totalArrieres}, {@code capitalRisque}, {@code maxJoursRetard},
     *       {@code nbCreditsRisque} : métriques de risque agrégées</li>
     * </ul>
     *
     * @return 200 + FeatureCollection GeoJSON (Polygons ou Points, colorés par PAR)
     */
    @Operation(summary = "Carte de chaleur du PAR par zone")
    @GetMapping(value = "/heatmap-par", produces = {GEO_JSON, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<FeatureCollection> getHeatmapPar() {
        return ResponseEntity.ok(cartographieService.heatmapPar());
    }
}

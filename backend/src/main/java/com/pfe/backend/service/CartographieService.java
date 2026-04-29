package com.pfe.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microfina.entity.ZoneGeographique;
import com.pfe.backend.dto.GeoJsonDto;
import com.pfe.backend.dto.GeoJsonDto.*;
import com.pfe.backend.repository.ZoneGeographiqueRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * CartographieService — construction des réponses GeoJSON RFC 7946.
 *
 * <h2>Responsabilités</h2>
 * <ol>
 *   <li>Charger les zones depuis {@code zoneGeographique} via JPA.</li>
 *   <li>Charger les KPIs opérationnels via JDBC (vues Phase 10.4).</li>
 *   <li>Assembler chaque zone en GeoJSON Feature :
 *       <ul>
 *         <li>geometry = Polygon depuis {@code geojson_polygon}, ou Point
 *             depuis {@code latitude/longitude}, ou {@code null}.</li>
 *         <li>properties = identité + KPIs de la zone.</li>
 *       </ul>
 *   </li>
 *   <li>Assembler toutes les agences actives en FeatureCollection de Points.</li>
 * </ol>
 *
 * <h2>Source des KPIs par zone</h2>
 * <p>Jointures SQL directes : {@code membres.zoneGeographique → idZoneGeographique},
 * {@code Credits.agence → AGENCE.CODE_AGENCE → AGENCE.zoneGeographique}.</p>
 */
@Service
@Transactional(readOnly = true)
public class CartographieService {

    private final ZoneGeographiqueRepository zoneRepo;
    private final JdbcTemplate               jdbc;
    private final ObjectMapper               objectMapper;

    public CartographieService(ZoneGeographiqueRepository zoneRepo,
                               JdbcTemplate jdbc,
                               ObjectMapper objectMapper) {
        this.zoneRepo     = zoneRepo;
        this.jdbc         = jdbc;
        this.objectMapper = objectMapper;
    }

    // =========================================================================
    //  FeatureCollection — toutes les zones actives
    // =========================================================================

    /**
     * Construit la FeatureCollection GeoJSON de toutes les zones actives,
     * enrichies de leurs KPIs opérationnels.
     *
     * @param typeZone filtre optionnel : WILAYA | MOUGHATAA | COMMUNE | QUARTIER | null
     * @return GeoJSON FeatureCollection
     */
    public FeatureCollection zonesFeatureCollection(String typeZone) {
        List<ZoneGeographique> zones = typeZone != null
                ? zoneRepo.findByTypeZone(typeZone)
                : zoneRepo.findAllActivesOrdered();

        // Charger les KPIs en une seule requête, indexés par idZone
        Map<Long, Map<String, Object>> kpisParZone = loadKpisParZone();

        List<Feature> features = zones.stream()
                .map(z -> buildZoneFeature(z, kpisParZone.getOrDefault(z.getIdZoneGeographique(), Map.of())))
                .toList();

        return FeatureCollection.of(features);
    }

    // =========================================================================
    //  Feature — zone individuelle
    // =========================================================================

    /**
     * Construit la Feature GeoJSON d'une zone individuelle.
     *
     * @param idZone identifiant de la zone
     * @return Feature GeoJSON ou Optional.empty() si la zone n'existe pas
     */
    public Optional<Feature> zoneFeature(Long idZone) {
        return zoneRepo.findById(idZone).map(zone -> {
            Map<String, Object> kpis = loadKpisPourZone(idZone);
            return buildZoneFeature(zone, kpis);
        });
    }

    // =========================================================================
    //  FeatureCollection — agences (Points)
    // =========================================================================

    /**
     * Construit la FeatureCollection GeoJSON des agences actives.
     * Chaque agence est un Point avec ses coordonnées GPS et ses indicateurs.
     */
    public FeatureCollection agencesFeatureCollection() {
        List<Map<String, Object>> agences = jdbc.queryForList("""
                SELECT a.CODE_AGENCE, a.NOMAGENCE,
                       a.latitude, a.longitude,
                       a.zoneGeographique, a.ISSIEGE,
                       COUNT(DISTINCT c.IDCREDIT)  AS nb_credits_actifs,
                       COALESCE(SUM(CASE WHEN c.STATUT = 'DEBLOQUE'
                                    THEN c.SOLDE_CAPITAL ELSE 0 END), 0) AS encours_net,
                       COUNT(DISTINCT m.NUM_MEMBRE) AS nb_membres
                FROM AGENCE a
                LEFT JOIN Credits c ON c.agence = a.CODE_AGENCE
                LEFT JOIN membres m ON m.agence = a.CODE_AGENCE
                WHERE a.ACTIF = 1
                  AND a.latitude  IS NOT NULL
                  AND a.longitude IS NOT NULL
                GROUP BY a.CODE_AGENCE, a.NOMAGENCE,
                         a.latitude, a.longitude,
                         a.zoneGeographique, a.ISSIEGE
                """);

        List<Feature> features = agences.stream().map(row -> {
            Double lat = toDouble(row.get("latitude"));
            Double lon = toDouble(row.get("longitude"));
            Geometry geom = (lat != null && lon != null)
                    ? Geometry.point(lon, lat)
                    : null;

            Map<String, Object> props = new LinkedHashMap<>();
            props.put("type",             "AGENCE");
            props.put("code",             row.get("CODE_AGENCE"));
            props.put("libelle",          row.get("NOMAGENCE"));
            props.put("isSiege",          "O".equals(row.get("ISSIEGE")));
            props.put("idZone",           row.get("zoneGeographique"));
            props.put("nbCreditsActifs",  row.get("nb_credits_actifs"));
            props.put("encoursNet",       row.get("encours_net"));
            props.put("nbMembres",        row.get("nb_membres"));

            return Feature.of(geom, props);
        }).toList();

        return FeatureCollection.of(features);
    }

    // =========================================================================
    //  Heatmap PAR — zones colorées par intensité de risque
    // =========================================================================

    /**
     * FeatureCollection enrichie du PAR agrégé par zone — utilisée pour
     * le rendu d'une heatmap choroplèthe (ex. Leaflet + Mapbox GL JS).
     *
     * <p>La propriété {@code couleurPar} est une suggestion de couleur hex :
     * <ul>
     *   <li>SAIN      → #2ecc71 (vert)</li>
     *   <li>PAR30     → #f39c12 (orange)</li>
     *   <li>PAR90     → #e67e22 (orange foncé)</li>
     *   <li>PAR180    → #e74c3c (rouge)</li>
     *   <li>PAR180+   → #8e44ad (violet)</li>
     * </ul>
     */
    public FeatureCollection heatmapPar() {
        // PAR agrégé par zone via la table membres et vue_par_bcm
        List<Map<String, Object>> parParZone = jdbc.queryForList("""
                SELECT
                    m.zoneGeographique                          AS idZone,
                    COUNT(DISTINCT par.IDCREDIT)                AS nb_credits_risque,
                    COALESCE(SUM(par.total_arrieres), 0)        AS total_arrieres,
                    COALESCE(SUM(par.SOLDE_CAPITAL), 0)         AS capital_risque,
                    MAX(COALESCE(par.max_jours_retard, 0))      AS max_jours_retard,
                    CASE
                        WHEN MAX(COALESCE(par.max_jours_retard,0)) > 180 THEN 'PAR180_PLUS'
                        WHEN MAX(COALESCE(par.max_jours_retard,0)) >  90 THEN 'PAR180'
                        WHEN MAX(COALESCE(par.max_jours_retard,0)) >  30 THEN 'PAR90'
                        WHEN MAX(COALESCE(par.max_jours_retard,0)) >   0 THEN 'PAR30'
                        ELSE 'SAIN'
                    END                                         AS categorie_par
                FROM vue_par_bcm par
                JOIN Credits c2  ON c2.IDCREDIT = par.IDCREDIT
                JOIN membres m   ON m.NUM_MEMBRE = c2.nummembre
                WHERE m.zoneGeographique IS NOT NULL
                GROUP BY m.zoneGeographique
                """);

        // Indexer par idZone
        Map<Long, Map<String, Object>> parIndex = new HashMap<>();
        for (Map<String, Object> row : parParZone) {
            Object idZone = row.get("idZone");
            if (idZone != null) {
                parIndex.put(toLong(idZone), row);
            }
        }

        List<ZoneGeographique> zones = zoneRepo.findAllActivesOrdered();
        List<Feature> features = zones.stream().map(z -> {
            Geometry geom = buildGeometry(z);
            Map<String, Object> row  = parIndex.getOrDefault(z.getIdZoneGeographique(), Map.of());

            String categorie = (String) row.getOrDefault("categorie_par", "SAIN");
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("id",              z.getIdZoneGeographique());
            props.put("libelle",         z.getLibelleZoneGeographique());
            props.put("code",            z.getCode());
            props.put("typeZone",        z.getTypeZone());
            props.put("categoriePar",    categorie);
            props.put("couleurPar",      couleurPar(categorie));
            props.put("totalArrieres",   row.getOrDefault("total_arrieres", 0));
            props.put("capitalRisque",   row.getOrDefault("capital_risque",  0));
            props.put("maxJoursRetard",  row.getOrDefault("max_jours_retard", 0));
            props.put("nbCreditsRisque", row.getOrDefault("nb_credits_risque", 0));

            return Feature.of(geom, props);
        }).toList();

        return FeatureCollection.of(features);
    }

    // =========================================================================
    //  Méthodes privées
    // =========================================================================

    private Feature buildZoneFeature(ZoneGeographique z, Map<String, Object> kpis) {
        Geometry geom = buildGeometry(z);

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("id",           z.getIdZoneGeographique());
        props.put("code",         z.getCode());
        props.put("libelle",      z.getLibelleZoneGeographique());
        props.put("typeZone",     z.getTypeZone());
        props.put("zoneParente",  z.getZoneParente());
        props.put("latitude",     z.getLatitude());
        props.put("longitude",    z.getLongitude());

        // KPIs opérationnels
        props.put("nbMembres",        kpis.getOrDefault("nb_membres",         0));
        props.put("nbMembresActifs",  kpis.getOrDefault("nb_membres_actifs",  0));
        props.put("nbAgences",        kpis.getOrDefault("nb_agences",          0));
        props.put("nbCreditsActifs",  kpis.getOrDefault("nb_credits_actifs",   0));
        props.put("encoursBrut",      kpis.getOrDefault("encours_brut",        0));
        props.put("totalArrieres",    kpis.getOrDefault("total_arrieres",      0));
        props.put("tauxPar30",        kpis.getOrDefault("taux_par_30",         0));

        return Feature.of(geom, props);
    }

    /**
     * Construit la géométrie GeoJSON d'une zone :
     * 1. Si {@code geojsonPolygon} est renseigné → désérialiser et utiliser
     * 2. Sinon si lat/lon → Point
     * 3. Sinon → null (zone sans coordonnées)
     */
    private Geometry buildGeometry(ZoneGeographique z) {
        if (z.getGeojsonPolygon() != null && !z.getGeojsonPolygon().isBlank()) {
            try {
                // Désérialise {"type":"Polygon","coordinates":[...]}
                Map<String, Object> geomMap = objectMapper.readValue(
                        z.getGeojsonPolygon(),
                        new TypeReference<Map<String, Object>>() {});
                String gType = (String) geomMap.get("type");
                Object coords = geomMap.get("coordinates");
                return new Geometry(gType, coords);
            } catch (Exception e) {
                // Fallback sur centroïde si le JSON est malformé
            }
        }
        if (z.hasCentroid()) {
            return Geometry.point(
                    z.getLongitude().doubleValue(),
                    z.getLatitude().doubleValue());
        }
        return null;
    }

    /** Charge les KPIs de toutes les zones actives en une seule requête SQL. */
    private Map<Long, Map<String, Object>> loadKpisParZone() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT
                    m.zoneGeographique                                  AS idZone,
                    COUNT(DISTINCT m.NUM_MEMBRE)                        AS nb_membres,
                    SUM(CASE WHEN m.STATUT = 'ACTIF' THEN 1 ELSE 0 END) AS nb_membres_actifs,
                    COUNT(DISTINCT ag.CODE_AGENCE)                      AS nb_agences,
                    COUNT(DISTINCT CASE WHEN c.STATUT = 'DEBLOQUE'
                                   THEN c.IDCREDIT END)                 AS nb_credits_actifs,
                    COALESCE(SUM(CASE WHEN c.STATUT = 'DEBLOQUE'
                                 THEN c.MONTANT_DEBLOQUER ELSE 0 END),0) AS encours_brut,
                    COALESCE(SUM(CASE WHEN c.STATUT = 'DEBLOQUE'
                                 THEN c.SOLDE_CAPITAL ELSE 0 END),    0) AS encours_net,
                    COALESCE(SUM(par.total_arrieres),                  0) AS total_arrieres,
                    CAST(0 AS FLOAT)                                       AS taux_par_30
                FROM membres m
                LEFT JOIN AGENCE ag  ON ag.zoneGeographique = m.zoneGeographique
                LEFT JOIN Credits c  ON c.nummembre = m.NUM_MEMBRE
                LEFT JOIN vue_par_bcm par ON par.IDCREDIT = c.IDCREDIT
                WHERE m.zoneGeographique IS NOT NULL
                GROUP BY m.zoneGeographique
                """);

        Map<Long, Map<String, Object>> index = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object id = row.get("idZone");
            if (id != null) index.put(toLong(id), row);
        }
        return index;
    }

    /** Charge les KPIs pour une zone spécifique. */
    private Map<String, Object> loadKpisPourZone(Long idZone) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT
                    COUNT(DISTINCT m.NUM_MEMBRE)                         AS nb_membres,
                    SUM(CASE WHEN m.STATUT = 'ACTIF' THEN 1 ELSE 0 END) AS nb_membres_actifs,
                    COUNT(DISTINCT ag.CODE_AGENCE)                       AS nb_agences,
                    COUNT(DISTINCT CASE WHEN c.STATUT = 'DEBLOQUE'
                                   THEN c.IDCREDIT END)                  AS nb_credits_actifs,
                    COALESCE(SUM(CASE WHEN c.STATUT = 'DEBLOQUE'
                                 THEN c.MONTANT_DEBLOQUER ELSE 0 END), 0) AS encours_brut,
                    COALESCE(SUM(par.total_arrieres), 0)                  AS total_arrieres
                FROM membres m
                LEFT JOIN AGENCE ag  ON ag.zoneGeographique = m.zoneGeographique
                LEFT JOIN Credits c  ON c.nummembre = m.NUM_MEMBRE
                LEFT JOIN vue_par_bcm par ON par.IDCREDIT = c.IDCREDIT
                WHERE m.zoneGeographique = ?
                """, idZone);

        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    // ── Utilitaires ───────────────────────────────────────────────────────

    private static String couleurPar(String categorie) {
        return switch (categorie == null ? "SAIN" : categorie) {
            case "PAR30"      -> "#f39c12";
            case "PAR90"      -> "#e67e22";
            case "PAR180"     -> "#e74c3c";
            case "PAR180_PLUS"-> "#8e44ad";
            default           -> "#2ecc71";
        };
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Long l) return l;
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }

    private static Double toDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Double d) return d;
        if (v instanceof BigDecimal bd) return bd.doubleValue();
        if (v instanceof Number n) return n.doubleValue();
        return null;
    }
}

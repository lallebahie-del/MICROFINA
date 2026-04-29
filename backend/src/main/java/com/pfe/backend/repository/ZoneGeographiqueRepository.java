package com.pfe.backend.repository;

import com.microfina.entity.ZoneGeographique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ZoneGeographiqueRepository — accès Spring Data JPA à la table
 * {@code zoneGeographique}.
 *
 * <p>DDL source of truth : P1-005 + P10-501.</p>
 */
@Repository
public interface ZoneGeographiqueRepository extends JpaRepository<ZoneGeographique, Long> {

    /**
     * Retourne toutes les zones actives, triées par type puis par libellé.
     * Utilisé pour construire la FeatureCollection GeoJSON principale.
     */
    @Query("""
        SELECT z FROM ZoneGeographique z
        WHERE z.actif = true
        ORDER BY z.typeZone ASC, z.libelleZoneGeographique ASC
        """)
    List<ZoneGeographique> findAllActivesOrdered();

    /**
     * Retourne les zones d'un niveau administratif donné.
     *
     * @param typeZone WILAYA | MOUGHATAA | COMMUNE | QUARTIER
     */
    @Query("""
        SELECT z FROM ZoneGeographique z
        WHERE z.actif = true AND z.typeZone = :typeZone
        ORDER BY z.libelleZoneGeographique ASC
        """)
    List<ZoneGeographique> findByTypeZone(@Param("typeZone") String typeZone);

    /**
     * Retourne les enfants directs d'une zone parente.
     */
    @Query("""
        SELECT z FROM ZoneGeographique z
        WHERE z.actif = true AND z.zoneParente = :idParent
        ORDER BY z.libelleZoneGeographique ASC
        """)
    List<ZoneGeographique> findEnfantsDirects(@Param("idParent") Long idParent);

    /**
     * Zones avec coordonnées de centroïde définies (utiles pour la carte des points).
     */
    @Query("""
        SELECT z FROM ZoneGeographique z
        WHERE z.actif = true
          AND z.latitude IS NOT NULL
          AND z.longitude IS NOT NULL
        """)
    List<ZoneGeographique> findWithCentroid();
}

package com.pfe.backend.repository;

import com.microfina.entity.Garantie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * GarantieRepository – accès Spring Data JPA à la table {@code Garantie}.
 *
 * <p>Permet de récupérer toutes les garanties associées à un crédit donné,
 * avec ou sans filtre sur le statut, sans avoir à jongler entre
 * {@code HYPOTHEQUE_CREDIT}, {@code gageCredit}, {@code Caution} et {@code Nantissement}.</p>
 *
 * DDL source of truth : P10-001c-CREATE-TABLE-Garantie.xml
 */
@Repository
public interface GarantieRepository extends JpaRepository<Garantie, Long> {

    /**
     * Retourne toutes les garanties actives d'un crédit, avec le type pré-chargé
     * pour éviter les N+1 lors de l'affichage.
     *
     * @param idCredit identifiant du crédit
     * @return garanties actives du crédit
     */
    @Query("""
        SELECT g FROM Garantie g
        JOIN FETCH g.typeGarantie
        WHERE g.credit.idCredit = :idCredit
          AND g.statut = 'ACTIF'
        ORDER BY g.dateSaisie DESC
        """)
    List<Garantie> findActivesParCredit(@Param("idCredit") Long idCredit);

    /**
     * Somme des valeurs estimées actives pour un crédit — utile pour le calcul
     * du ratio de couverture global du dossier.
     *
     * @param idCredit identifiant du crédit
     * @return somme des valeurs actives, {@code null} si aucune garantie
     */
    @Query("""
        SELECT COALESCE(SUM(g.valeurEstimee), 0)
        FROM Garantie g
        WHERE g.credit.idCredit = :idCredit
          AND g.statut = 'ACTIF'
        """)
    java.math.BigDecimal sommeCouvertureActive(@Param("idCredit") Long idCredit);
}

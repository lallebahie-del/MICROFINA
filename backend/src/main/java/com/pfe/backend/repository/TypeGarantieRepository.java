package com.pfe.backend.repository;

import com.microfina.entity.TypeGarantie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TypeGarantieRepository – accès Spring Data JPA à la table {@code type_garantie}.
 *
 * <p>La PK est le code métier stable ({@code String}) plutôt qu'un identifiant
 * auto-incrémenté, ce qui simplifie les insertions seed et les requêtes métier.</p>
 *
 * <p>Utilisé par :</p>
 * <ul>
 *   <li>L'API REST de référentiel ({@code GET /api/v1/referentiel/types-garantie})</li>
 *   <li>Le formulaire de saisie de garantie côté frontend</li>
 *   <li>Les validateurs Jakarta qui vérifient l'existence d'un code avant persistance</li>
 * </ul>
 *
 * DDL source of truth : P10-001-CREATE-TABLE-type_garantie.xml
 */
@Repository
public interface TypeGarantieRepository extends JpaRepository<TypeGarantie, String> {

    /**
     * Retourne uniquement les types de garantie actifs, triés par libellé.
     * C'est la requête utilisée pour alimenter les listes déroulantes.
     *
     * @return liste des types actifs ordonnée alphabétiquement par libellé
     */
    @Query("SELECT t FROM TypeGarantie t WHERE t.actif = true ORDER BY t.libelle ASC")
    List<TypeGarantie> findAllActifsOrdonnes();

    /**
     * Vérifie l'existence et l'activité d'un code avant de l'utiliser comme FK.
     *
     * @param code code à vérifier
     * @return {@code true} si le code existe et est actif
     */
    boolean existsByCodeAndActifTrue(String code);
}

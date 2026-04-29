package com.pfe.backend.repository;

import com.microfina.entity.Comptabilite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ComptabiliteRepository – accès JPA à la table comptabilite (lecture seule via API).
 */
@Repository
public interface ComptabiliteRepository extends JpaRepository<Comptabilite, Long> {

    /**
     * Retourne toutes les écritures comptables d'une agence donnée.
     * Utilise le champ {@code codeAgence} (colonne CODE_AGENCE) de l'entité Comptabilite,
     * qui est un champ de type String (non une relation JPA).
     *
     * @param codeAgence le code agence
     * @return liste des écritures
     */
    List<Comptabilite> findByCodeAgence(String codeAgence);
}

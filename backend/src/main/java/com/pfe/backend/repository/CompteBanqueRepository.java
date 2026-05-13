package com.pfe.backend.repository;

import com.microfina.entity.CompteBanque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * CompteBanqueRepository – accès JPA à la table CompteBanque.
 *
 * <p>Utilisé principalement pour obtenir des références JPA (proxies lazy)
 * lors de la création d'opérations bancaires.</p>
 */
@Repository
public interface CompteBanqueRepository extends JpaRepository<CompteBanque, Long> {

    /** Premier compte actif pour une banque et une agence (saisie opération depuis l'UI). */
    Optional<CompteBanque> findFirstByBanque_CodeBanqueAndAgence_CodeAgence(
            String codeBanque, String codeAgence);
}

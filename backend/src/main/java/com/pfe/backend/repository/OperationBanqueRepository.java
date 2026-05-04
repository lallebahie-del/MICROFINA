package com.pfe.backend.repository;

import com.microfina.entity.OperationBanque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * OperationBanqueRepository – accès JPA à la table OperationBanque.
 */
@Repository
public interface OperationBanqueRepository extends JpaRepository<OperationBanque, Long> {

    /**
     * Retourne toutes les opérations bancaires d'une agence donnée.
     *
     * @param codeAgence le code de l'agence
     * @return liste des opérations
     */
    List<OperationBanque> findByAgence_CodeAgence(String codeAgence);

    /**
     * Retourne toutes les opérations liées à un compte bancaire.
     *
     * @param compteBanqueId l'identifiant du compte bancaire
     * @return liste des opérations
     */
    List<OperationBanque> findByCompteBanque_Id(Long compteBanqueId);
}

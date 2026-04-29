package com.pfe.backend.repository;

import com.microfina.entity.OperationCaisse;
import com.microfina.entity.StatutOperationCaisse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * OperationCaisseRepository – accès JPA à la table OperationCaisse.
 */
@Repository
public interface OperationCaisseRepository extends JpaRepository<OperationCaisse, Long> {

    /**
     * Retourne toutes les opérations associées à une agence donnée.
     *
     * @param codeAgence le code de l'agence
     * @return liste des opérations
     */
    List<OperationCaisse> findByAgence_CodeAgence(String codeAgence);

    /**
     * Retourne toutes les opérations associées à un compte épargne.
     *
     * @param numCompte le numéro de compte épargne
     * @return liste des opérations
     */
    List<OperationCaisse> findByCompteEps_NumCompte(String numCompte);

    /**
     * Retourne toutes les opérations ayant un statut donné.
     *
     * @param statut le statut recherché
     * @return liste des opérations
     */
    List<OperationCaisse> findByStatut(StatutOperationCaisse statut);
}

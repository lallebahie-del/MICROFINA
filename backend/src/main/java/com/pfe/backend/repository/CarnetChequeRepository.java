package com.pfe.backend.repository;

import com.microfina.entity.CarnetCheque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CarnetChequeRepository — accès JPA à la table CarnetCheque.
 */
@Repository
public interface CarnetChequeRepository extends JpaRepository<CarnetCheque, Long> {

    /** Retourne tous les carnets de chèques d'un membre donné. */
    List<CarnetCheque> findByMembre_NumMembre(String numMembre);

    /** Retourne tous les carnets de chèques liés à un compte bancaire donné. */
    List<CarnetCheque> findByCompteBanque_Id(Long compteBanqueId);
}

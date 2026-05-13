package com.pfe.backend.repository;

import com.microfina.entity.Epargne;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * EpargneRepository — accès JPA aux mouvements épargne (table EPARGNE).
 *
 * <p>Sert principalement à l'historique des opérations affiché dans
 * l'application mobile. Le tri est porté par le {@link Pageable}.</p>
 */
@Repository
public interface EpargneRepository extends JpaRepository<Epargne, Long> {

    /** Mouvements d'un compte épargne donné, paginés. */
    Page<Epargne> findByCompteEps_NumCompte(String numCompte, Pageable pageable);
}

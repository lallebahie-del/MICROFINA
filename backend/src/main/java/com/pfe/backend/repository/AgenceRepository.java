package com.pfe.backend.repository;

import com.microfina.entity.Agence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * AgenceRepository — accès JPA à la table AGENCE.
 *
 * <p>Principalement utilisé pour obtenir des références JPA (lazy proxies)
 * lors de la création d'entités liées à une agence.</p>
 */
@Repository
public interface AgenceRepository extends JpaRepository<Agence, String> {
}

package com.pfe.backend.repository;

import com.microfina.entity.Banque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * BanqueRepository — accès JPA à la table Banque.
 */
@Repository
public interface BanqueRepository extends JpaRepository<Banque, String> {

    /** Retourne uniquement les banques actives (actif = true). */
    List<Banque> findByActifTrue();
}

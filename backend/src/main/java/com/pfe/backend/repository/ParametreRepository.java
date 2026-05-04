package com.pfe.backend.repository;

import com.microfina.entity.Parametre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ParametreRepository — accès JPA à la table Parametre.
 */
@Repository
public interface ParametreRepository extends JpaRepository<Parametre, Long> {

    Optional<Parametre> findByAgence_CodeAgence(String codeAgence);
}

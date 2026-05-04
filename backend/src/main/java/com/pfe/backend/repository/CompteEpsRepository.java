package com.pfe.backend.repository;

import com.microfina.entity.CompteEps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CompteEpsRepository — accès JPA à la table COMPTEEPS.
 */
@Repository
public interface CompteEpsRepository extends JpaRepository<CompteEps, String> {

    /** Retourne tous les comptes épargne d'un membre donné. */
    List<CompteEps> findByMembre_NumMembre(String numMembre);

    /** Retourne tous les comptes épargne gérés par une agence donnée. */
    List<CompteEps> findByAgence_CodeAgence(String codeAgence);
}

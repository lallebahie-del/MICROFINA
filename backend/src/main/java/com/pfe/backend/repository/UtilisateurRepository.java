package com.pfe.backend.repository;

import com.microfina.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UtilisateurRepository — accès JPA à la table Utilisateur.
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByLogin(String login);

    Optional<Utilisateur> findByNumCompteCourant(String numCompteCourant);

    Optional<Utilisateur> findByNumMembre(String numMembre);

    List<Utilisateur> findByActifTrue();

    List<Utilisateur> findByAgence_CodeAgence(String codeAgence);

    boolean existsByLogin(String login);
}

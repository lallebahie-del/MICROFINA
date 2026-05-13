package com.pfe.backend.repository;

import com.microfina.entity.UtilisateurRole;
import com.microfina.entity.UtilisateurRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * UtilisateurRoleRepository — accès JPA à la table de jointure UtilisateurRole.
 */
@Repository
public interface UtilisateurRoleRepository extends JpaRepository<UtilisateurRole, UtilisateurRoleId> {
}

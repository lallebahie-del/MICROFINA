package com.pfe.backend.repository;

import com.microfina.entity.UtilisateurRole;
import com.microfina.entity.UtilisateurRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * UtilisateurRoleRepository — accès JPA à la table de jointure UtilisateurRole.
 */
@Repository
public interface UtilisateurRoleRepository extends JpaRepository<UtilisateurRole, UtilisateurRoleId> {

    @Query("SELECT ur FROM UtilisateurRole ur JOIN FETCH ur.role WHERE ur.id.idUtilisateur = :id")
    List<UtilisateurRole> findWithRoleByUserId(@Param("id") Long id);

    @Modifying
    @Query("DELETE FROM UtilisateurRole ur WHERE ur.id.idUtilisateur = :id")
    void deleteByUserId(@Param("id") Long id);
}

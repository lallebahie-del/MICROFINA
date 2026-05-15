package com.pfe.backend.repository;

import com.microfina.entity.RolePrivilege;
import com.microfina.entity.RolePrivilegeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * RolePrivilegeRepository — accès JPA à la table RolePrivilege.
 */
@Repository
public interface RolePrivilegeRepository extends JpaRepository<RolePrivilege, RolePrivilegeId> {

    @Query("SELECT rp FROM RolePrivilege rp JOIN FETCH rp.privilege WHERE rp.role.id = :roleId")
    List<RolePrivilege> findWithPrivilegeByRoleId(@Param("roleId") Long roleId);

    @Modifying
    @Query("DELETE FROM RolePrivilege rp WHERE rp.role.id = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);
}

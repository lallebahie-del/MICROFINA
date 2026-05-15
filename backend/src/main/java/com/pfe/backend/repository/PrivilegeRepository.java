package com.pfe.backend.repository;

import com.microfina.entity.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PrivilegeRepository — accès JPA à la table Privilege.
 */
@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {

    Optional<Privilege> findByCodePrivilege(String codePrivilege);

    boolean existsByCodePrivilege(String codePrivilege);

    List<Privilege> findByModule(String module);

    List<Privilege> findByCodePrivilegeIn(List<String> codes);
}

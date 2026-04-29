package com.pfe.backend.repository;

import com.microfina.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * RoleRepository — accès JPA à la table Role.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCodeRole(String codeRole);

    boolean existsByCodeRole(String codeRole);
}

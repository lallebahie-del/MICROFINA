package com.pfe.backend.repository;

import com.microfina.entity.Reglement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReglementRepository extends JpaRepository<Reglement, Long> {
}


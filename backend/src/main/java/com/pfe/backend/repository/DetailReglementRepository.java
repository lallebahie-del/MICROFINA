package com.pfe.backend.repository;

import com.microfina.entity.DetailReglement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetailReglementRepository extends JpaRepository<DetailReglement, Long> {
}


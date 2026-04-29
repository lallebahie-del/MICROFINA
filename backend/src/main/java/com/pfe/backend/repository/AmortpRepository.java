package com.pfe.backend.repository;

import com.microfina.entity.Amortp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmortpRepository extends JpaRepository<Amortp, Long> {
}

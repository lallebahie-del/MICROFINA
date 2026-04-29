package com.pfe.backend.repository;

import com.microfina.entity.AnalyseFinanciere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalyseFinanciereRepository extends JpaRepository<AnalyseFinanciere, Long> {
    Optional<AnalyseFinanciere> findByCredit_IdCredit(Long idCredit);
}

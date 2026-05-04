package com.pfe.backend.repository;

import com.microfina.entity.HistoriqueVisaCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueVisaCreditRepository extends JpaRepository<HistoriqueVisaCredit, Long> {
    List<HistoriqueVisaCredit> findByCredit_IdCreditOrderByDateVisaAscIdHistoriqueAsc(Long idCredit);
}

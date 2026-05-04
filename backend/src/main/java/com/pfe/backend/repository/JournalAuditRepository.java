package com.pfe.backend.repository;

import com.microfina.entity.ActionAudit;
import com.microfina.entity.JournalAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JournalAuditRepository — accès JPA à la table JournalAudit (lecture seule).
 */
@Repository
public interface JournalAuditRepository extends JpaRepository<JournalAudit, Long> {

    @Query("SELECT j FROM JournalAudit j WHERE j.utilisateur = :utilisateur ORDER BY j.dateAction DESC")
    Page<JournalAudit> findByUtilisateurPageable(
        @Param("utilisateur") String utilisateur,
        Pageable pageable
    );

    @Query("SELECT j FROM JournalAudit j WHERE j.entite = :entite ORDER BY j.dateAction DESC")
    Page<JournalAudit> findByEntitePageable(
        @Param("entite") String entite,
        Pageable pageable
    );

    @Query("SELECT j FROM JournalAudit j WHERE j.action = :action ORDER BY j.dateAction DESC")
    Page<JournalAudit> findByActionPageable(
        @Param("action") ActionAudit action,
        Pageable pageable
    );

    List<JournalAudit> findByUtilisateur(String utilisateur);

    List<JournalAudit> findByEntite(String entite);

    List<JournalAudit> findByAction(ActionAudit action);
}

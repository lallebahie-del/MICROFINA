package com.pfe.backend.repository;

import com.microfina.entity.Membres;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MembresRepository extends JpaRepository<Membres, String> {

    /**
     * Full-text search across numMembre, nom, prenom, and raisonSociale.
     * Returns a page so the front-end can paginate large tables.
     */
    @Query("""
        SELECT m FROM Membres m
        WHERE (:search IS NULL OR :search = ''
            OR LOWER(m.numMembre)    LIKE LOWER(CONCAT('%',:search,'%'))
            OR LOWER(m.nom)          LIKE LOWER(CONCAT('%',:search,'%'))
            OR LOWER(m.prenom)       LIKE LOWER(CONCAT('%',:search,'%'))
            OR LOWER(m.raisonSociale) LIKE LOWER(CONCAT('%',:search,'%'))
        )
        AND (:statut IS NULL OR :statut = '' OR m.statut = :statut)
        AND (:etat   IS NULL OR :etat   = '' OR m.etat   = :etat)
        ORDER BY m.nom ASC, m.prenom ASC
        """)
    Page<Membres> search(
        @Param("search") String search,
        @Param("statut") String statut,
        @Param("etat")   String etat,
        Pageable pageable
    );
}

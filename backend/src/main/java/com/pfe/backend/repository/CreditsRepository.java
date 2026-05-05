package com.pfe.backend.repository;

import com.microfina.entity.Credits;
import com.microfina.entity.CreditStatut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditsRepository extends JpaRepository<Credits, Long> {

    /**
     * Paginated search across numCredit, member name/id, and product name.
     * Filters by statut, member, and product.
     */
    @Query("""
        SELECT c FROM Credits c
        LEFT JOIN c.membre m
        LEFT JOIN c.produitCredit p
        WHERE (:search IS NULL OR :search = ''
            OR LOWER(c.numCredit)    LIKE LOWER(CONCAT('%',:search,'%'))
            OR LOWER(m.nom)          LIKE LOWER(CONCAT('%',:search,'%'))
            OR LOWER(m.prenom)       LIKE LOWER(CONCAT('%',:search,'%'))
            OR LOWER(m.numMembre)    LIKE LOWER(CONCAT('%',:search,'%'))
            OR LOWER(p.nomProduit)   LIKE LOWER(CONCAT('%',:search,'%'))
        )
        AND (:statut IS NULL OR c.statut = :statut)
        AND (:numMembre IS NULL OR :numMembre = '' OR m.numMembre = :numMembre)
        ORDER BY c.idCredit DESC
        """)
    Page<Credits> search(
        @Param("search")     String       search,
        @Param("statut")     CreditStatut statut,
        @Param("numMembre")  String       numMembre,
        Pageable pageable
    );

    List<Credits> findByEtapeCourante(String etapeCourante);

    @Query("""
        SELECT DISTINCT c FROM Credits c
        LEFT JOIN FETCH c.produitCredit pc
        LEFT JOIN FETCH pc.produitIslamic
        LEFT JOIN FETCH c.modeDeCalculInteret
        WHERE c.idCredit = :id
        """)
    Optional<Credits> findWithAmortissementContextById(@Param("id") Long id);
}

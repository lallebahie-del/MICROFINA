package com.pfe.backend.repository;

import com.microfina.entity.ProduitCredit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProduitCreditRepository extends JpaRepository<ProduitCredit, String> {

    /** All active products – used by credit-creation forms. */
    List<ProduitCredit> findByActif(Integer actif);

    /** Paginated search by name or product code. */
    @Query("""
        SELECT p FROM ProduitCredit p
        WHERE (:search IS NULL OR :search = ''
            OR LOWER(p.numProduit)  LIKE LOWER(CONCAT('%',:search,'%'))
            OR LOWER(p.nomProduit)  LIKE LOWER(CONCAT('%',:search,'%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%',:search,'%'))
        )
        AND (:actif IS NULL OR p.actif = :actif)
        ORDER BY p.nomProduit ASC
        """)
    Page<ProduitCredit> search(
        @Param("search") String search,
        @Param("actif")  Integer actif,
        Pageable pageable
    );
}

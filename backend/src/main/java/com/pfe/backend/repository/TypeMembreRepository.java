package com.pfe.backend.repository;

import com.microfina.entity.ProduitCreditTypeMembre;
import com.microfina.entity.ProduitCreditTypeMembreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TypeMembreRepository — accès JPA à ProduitCreditTypeMembre pour la gestion des types membres.
 */
@Repository
public interface TypeMembreRepository extends JpaRepository<ProduitCreditTypeMembre, ProduitCreditTypeMembreId> {

    @Query("SELECT DISTINCT p.id.typeMembre FROM ProduitCreditTypeMembre p")
    List<String> findDistinctTypesMembre();

    List<ProduitCreditTypeMembre> findById_TypeMembre(String typeMembre);
}

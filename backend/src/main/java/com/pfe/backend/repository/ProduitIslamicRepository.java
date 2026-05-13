package com.pfe.backend.repository;

import com.microfina.entity.ProduitIslamic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProduitIslamicRepository extends JpaRepository<ProduitIslamic, String> {

    List<ProduitIslamic> findByActif(Integer actif);
}

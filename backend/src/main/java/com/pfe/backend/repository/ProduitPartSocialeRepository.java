package com.pfe.backend.repository;

import com.microfina.entity.ProduitPartSociale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProduitPartSocialeRepository extends JpaRepository<ProduitPartSociale, String> {

    List<ProduitPartSociale> findByActif(Integer actif);
}

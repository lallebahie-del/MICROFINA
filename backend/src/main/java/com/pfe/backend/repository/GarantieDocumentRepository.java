package com.pfe.backend.repository;

import com.microfina.entity.GarantieDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GarantieDocumentRepository extends JpaRepository<GarantieDocument, Long> {
    List<GarantieDocument> findByGarantie_IdGarantieOrderByUploadedAtDesc(Long idGarantie);
}


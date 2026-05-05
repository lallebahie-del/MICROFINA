package com.pfe.backend.service;

import com.microfina.entity.Garantie;
import com.microfina.entity.GarantieDocument;
import com.pfe.backend.dto.GarantieDocumentDTO;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.GarantieDocumentRepository;
import com.pfe.backend.repository.GarantieRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class GarantieDocumentService {

    private final GarantieRepository garantieRepository;
    private final GarantieDocumentRepository docRepository;
    private final GarantieDocumentStorageService storageService;

    public GarantieDocumentService(GarantieRepository garantieRepository,
                                  GarantieDocumentRepository docRepository,
                                  GarantieDocumentStorageService storageService) {
        this.garantieRepository = garantieRepository;
        this.docRepository = docRepository;
        this.storageService = storageService;
    }

    public GarantieDocumentDTO upload(Long idGarantie, MultipartFile file, String user) {
        Garantie garantie = garantieRepository.findById(idGarantie)
                .orElseThrow(() -> new ResourceNotFoundException("Garantie", idGarantie));

        var stored = storageService.store(idGarantie, file);

        GarantieDocument doc = new GarantieDocument();
        doc.setGarantie(garantie);
        doc.setFilename(file.getOriginalFilename() != null ? file.getOriginalFilename() : stored.storedFilename());
        doc.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        doc.setSizeBytes(file.getSize());
        doc.setStoragePath(stored.storagePath());
        doc.setUploadedBy(user);
        doc.setUploadedAt(LocalDateTime.now());

        GarantieDocument saved = docRepository.save(doc);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<GarantieDocumentDTO> list(Long idGarantie) {
        // ensure garantie exists
        if (!garantieRepository.existsById(idGarantie)) {
            throw new ResourceNotFoundException("Garantie", idGarantie);
        }
        return docRepository.findByGarantie_IdGarantieOrderByUploadedAtDesc(idGarantie)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Download download(Long idGarantie, Long docId) {
        GarantieDocument doc = docRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("GarantieDocument", docId));
        if (doc.getGarantie() == null || doc.getGarantie().getIdGarantie() == null
                || !doc.getGarantie().getIdGarantie().equals(idGarantie)) {
            throw new ResourceNotFoundException("GarantieDocument", docId);
        }
        Resource r = storageService.loadAsResource(doc.getStoragePath());
        return new Download(r, doc.getContentType(), doc.getFilename());
    }

    private GarantieDocumentDTO toDto(GarantieDocument d) {
        return new GarantieDocumentDTO(
                d.getId(),
                d.getGarantie() != null ? d.getGarantie().getIdGarantie() : null,
                d.getFilename(),
                d.getContentType(),
                d.getSizeBytes(),
                d.getUploadedBy(),
                d.getUploadedAt()
        );
    }

    public record Download(Resource resource, String contentType, String filename) {}
}


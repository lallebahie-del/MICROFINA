package com.pfe.backend.dto;

import java.time.LocalDateTime;

public record GarantieDocumentDTO(
        Long id,
        Long idGarantie,
        String filename,
        String contentType,
        Long sizeBytes,
        String uploadedBy,
        LocalDateTime uploadedAt
) {}


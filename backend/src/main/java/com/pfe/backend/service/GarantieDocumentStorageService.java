package com.pfe.backend.service;

import com.pfe.backend.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class GarantieDocumentStorageService {

    @Value("${app.garanties.dir:./garanties}")
    private String garantiesDir;

    public StoredFile store(Long idGarantie, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Fichier manquant.");
        }

        try {
            Path root = Paths.get(garantiesDir).toAbsolutePath();
            Path dir = root.resolve(String.valueOf(idGarantie));
            Files.createDirectories(dir);

            String original = file.getOriginalFilename();
            String safeName = (original == null || original.isBlank()) ? "document" : original.replaceAll("[\\\\/:*?\"<>|]", "_");
            String storedName = UUID.randomUUID() + "_" + safeName;
            Path target = dir.resolve(storedName);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String relPath = root.relativize(target).toString().replace("\\", "/");
            return new StoredFile(relPath, storedName);
        } catch (IOException e) {
            throw new BusinessException("Erreur lors du stockage du document: " + e.getMessage());
        }
    }

    public Resource loadAsResource(String storagePath) {
        try {
            Path root = Paths.get(garantiesDir).toAbsolutePath();
            Path file = root.resolve(storagePath).normalize();
            if (!Files.exists(file)) {
                throw new BusinessException("Fichier introuvable.");
            }
            return new UrlResource(file.toUri());
        } catch (MalformedURLException e) {
            throw new BusinessException("Erreur d'accès au fichier: " + e.getMessage());
        }
    }

    public record StoredFile(String storagePath, String storedFilename) {}
}


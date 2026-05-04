package com.pfe.backend.service;

import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.MembresRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Set;

/**
 * PhotoService — stockage et récupération des photos de membres.
 *
 * <h2>Contraintes</h2>
 * <ul>
 *   <li>Types acceptés : {@code image/jpeg}, {@code image/png}</li>
 *   <li>Taille maximale : 2 Mo (2 097 152 octets)</li>
 *   <li>Nom du fichier stocké : {@code {numMembre}.jpg} (converti en JPEG si PNG)</li>
 * </ul>
 *
 * <p>Le répertoire de stockage est configurable via {@code app.photos.dir}
 * (valeur par défaut : {@code ./photos}).</p>
 */
@Service
public class PhotoService {

    private static final Logger log = LoggerFactory.getLogger(PhotoService.class);

    private static final long   MAX_SIZE        = 2L * 1024 * 1024; // 2 MB
    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png");

    @Value("${app.photos.dir:./photos}")
    private String photosDir;

    private final MembresRepository membresRepository;

    public PhotoService(MembresRepository membresRepository) {
        this.membresRepository = membresRepository;
    }

    /**
     * Enregistre la photo d'un membre.
     *
     * @param numMembre identifiant du membre
     * @param file      fichier multipart uploadé
     * @return chemin relatif du fichier enregistré
     */
    public String sauvegarder(String numMembre, MultipartFile file) {
        // Vérifier que le membre existe
        if (!membresRepository.existsById(numMembre)) {
            throw new ResourceNotFoundException("Membre", numMembre);
        }

        // Valider le type MIME
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(
                    "Type de fichier non autorisé. Types acceptés : image/jpeg, image/png");
        }

        // Valider la taille
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(
                    "Fichier trop volumineux. Taille maximale : 2 Mo");
        }

        try {
            Path dir = Paths.get(photosDir).toAbsolutePath();
            Files.createDirectories(dir);

            String extension = contentType.equals("image/png") ? ".png" : ".jpg";
            String filename  = numMembre + extension;
            Path   target    = dir.resolve(filename);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("[Photo] Sauvegardé : {}", target);

            return filename;
        } catch (IOException e) {
            throw new BusinessException("Erreur lors de la sauvegarde de la photo : " + e.getMessage());
        }
    }

    /**
     * Charge la photo d'un membre en tant que ressource Spring.
     *
     * @param numMembre identifiant du membre
     * @return ressource téléchargeable
     */
    public PhotoResource charger(String numMembre) {
        Path dir = Paths.get(photosDir).toAbsolutePath();

        // Tenter jpg puis png
        for (String ext : new String[]{".jpg", ".png"}) {
            Path file = dir.resolve(numMembre + ext);
            if (Files.exists(file)) {
                try {
                    Resource resource = new UrlResource(file.toUri());
                    String   mime     = ext.equals(".png") ? "image/png" : "image/jpeg";
                    return new PhotoResource(resource, mime, file.getFileName().toString());
                } catch (MalformedURLException e) {
                    throw new BusinessException("Erreur d'accès à la photo : " + e.getMessage());
                }
            }
        }
        throw new ResourceNotFoundException("Photo du membre", numMembre);
    }

    /**
     * Contenu d'une réponse photo (ressource + type MIME + nom de fichier).
     */
    public record PhotoResource(
            Resource resource,
            String   contentType,
            String   filename
    ) {}
}

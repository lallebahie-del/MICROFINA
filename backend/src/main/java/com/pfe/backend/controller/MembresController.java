package com.pfe.backend.controller;

import com.pfe.backend.dto.MembreDTO;
import com.pfe.backend.service.MembresService;
import com.pfe.backend.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * MembresController – REST API for cooperative members.
 *
 * <h2>Base URL</h2>
 * <pre>/api/v1/membres</pre>
 *
 * <h2>Endpoints</h2>
 * <pre>
 *   GET    /api/v1/membres                           – paginated search           PRIV_VIEW_REPORTS
 *   GET    /api/v1/membres/{numMembre}               – single member              PRIV_VIEW_REPORTS
 *   POST   /api/v1/membres                           – create member              PRIV_OPEN_COMPTE_EPS
 *   PUT    /api/v1/membres/{numMembre}               – full update                PRIV_OPEN_COMPTE_EPS
 *   PATCH  /api/v1/membres/{numMembre}/desactiver    – soft-deactivate            PRIV_OPEN_COMPTE_EPS
 *   DELETE /api/v1/membres/{numMembre}               – hard-delete (admin only)   PRIV_MANAGE_USERS
 * </pre>
 */
@Tag(name = "Membres", description = "Gestion des membres de la coopérative")
@RestController
@RequestMapping("/api/v1/membres")
public class MembresController {

    private final MembresService service;
    private final PhotoService   photoService;

    public MembresController(MembresService service, PhotoService photoService) {
        this.service      = service;
        this.photoService = photoService;
    }

    @Operation(summary = "Rechercher les membres (paginé)")
    @GetMapping
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "")   String search,
            @RequestParam(defaultValue = "")   String statut,
            @RequestParam(defaultValue = "")   String etat,
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "20") int    size
    ) {
        return ResponseEntity.ok(service.search(search, statut, etat, page, size));
    }

    @Operation(summary = "Obtenir un membre par numéro")
    @GetMapping("/{numMembre}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<MembreDTO> getOne(@PathVariable String numMembre) {
        return service.findById(numMembre)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Créer un nouveau membre")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_OPEN_COMPTE_EPS')")
    public ResponseEntity<MembreDTO> create(@Valid @RequestBody MembreDTO.CreateRequest req) {
        return ResponseEntity.status(201).body(service.create(req));
    }

    @Operation(summary = "Modifier un membre existant")
    @PutMapping("/{numMembre}")
    @PreAuthorize("hasAuthority('PRIV_OPEN_COMPTE_EPS')")
    public ResponseEntity<MembreDTO> update(
            @PathVariable String numMembre,
            @Valid @RequestBody MembreDTO.UpdateRequest req) {
        return ResponseEntity.ok(service.update(numMembre, req));
    }

    @Operation(summary = "Désactiver un membre (soft-delete)")
    @PatchMapping("/{numMembre}/desactiver")
    @PreAuthorize("hasAuthority('PRIV_OPEN_COMPTE_EPS')")
    public ResponseEntity<Void> desactiver(@PathVariable String numMembre) {
        return service.desactiver(numMembre)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Supprimer définitivement un membre")
    @DeleteMapping("/{numMembre}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_USERS')")
    public ResponseEntity<Void> delete(@PathVariable String numMembre) {
        return service.delete(numMembre)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // ── Photo ─────────────────────────────────────────────────────────────────

    /**
     * Upload de la photo d'un membre.
     *
     * <p>Contraintes :</p>
     * <ul>
     *   <li>Types acceptés : {@code image/jpeg}, {@code image/png}</li>
     *   <li>Taille maximale : 2 Mo</li>
     * </ul>
     *
     * @param numMembre identifiant du membre
     * @param file      fichier image (multipart/form-data, champ {@code file})
     * @return nom du fichier enregistré
     */
    @Operation(summary = "Uploader la photo d'un membre")
    @PostMapping(value = "/{numMembre}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRIV_OPEN_COMPTE_EPS')")
    public ResponseEntity<Map<String, String>> uploadPhoto(
            @PathVariable String numMembre,
            @RequestParam("file") MultipartFile file) {

        String filename = photoService.sauvegarder(numMembre, file);
        return ResponseEntity.ok(Map.of("filename", filename, "numMembre", numMembre));
    }

    /**
     * Téléchargement de la photo d'un membre.
     *
     * @param numMembre identifiant du membre
     * @return image JPEG ou PNG
     */
    @Operation(summary = "Télécharger la photo d'un membre")
    @GetMapping("/{numMembre}/photo")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<org.springframework.core.io.Resource> getPhoto(
            @PathVariable String numMembre) {

        PhotoService.PhotoResource pr = photoService.charger(numMembre);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(pr.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + pr.filename() + "\"")
                .body(pr.resource());
    }
}

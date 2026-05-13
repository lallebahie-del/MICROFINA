package com.pfe.backend.controller;

import com.pfe.backend.dto.GarantieDTO;
import com.pfe.backend.dto.GarantieDocumentDTO;
import com.pfe.backend.service.GarantieDocumentService;
import com.pfe.backend.service.GarantieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

/**
 * GarantieController — CRUD REST pour les garanties.
 *
 * <pre>
 *   POST   /api/v1/garanties                          — créer une garantie
 *   GET    /api/v1/garanties/{id}                     — lire une garantie
 *   GET    /api/v1/garanties/credit/{idCredit}        — garanties actives d'un crédit
 *   GET    /api/v1/garanties/credit/{idCredit}/couverture — taux de couverture
 *   PATCH  /api/v1/garanties/{id}/liberer             — mainlevée
 * </pre>
 *
 * <p>Sécurité : PRIV_VIEW_REPORTS pour la lecture ; PRIV_POST_REGLEMENT pour
 * la création et la mainlevée.</p>
 */
@Tag(name = "Garanties", description = "Gestion des garanties de crédit")
@RestController
@RequestMapping("/api/v1/garanties")
public class GarantieController {

    private final GarantieService service;
    private final GarantieDocumentService documentService;

    public GarantieController(GarantieService service, GarantieDocumentService documentService) {
        this.service = service;
        this.documentService = documentService;
    }

    // ── POST /api/v1/garanties ────────────────────────────────────────────

    @Operation(summary = "Enregistrer une garantie")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_POST_REGLEMENT')")
    public ResponseEntity<GarantieDTO> creer(
            @Valid @RequestBody GarantieDTO.CreationRequest req,
            Authentication auth) {

        GarantieDTO created = service.enregistrer(req, auth.getName());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.idGarantie())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // ── GET /api/v1/garanties/{id} ────────────────────────────────────────

    @Operation(summary = "Obtenir une garantie par identifiant")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<GarantieDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    // ── GET /api/v1/garanties/credit/{idCredit} ───────────────────────────

    @Operation(summary = "Lister les garanties d'un crédit")
    @GetMapping("/credit/{idCredit}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<GarantieDTO>> getParCredit(@PathVariable Long idCredit) {
        return ResponseEntity.ok(service.findActivesParCredit(idCredit));
    }

    // ── GET /api/v1/garanties/credit/{idCredit}/couverture ────────────────

    @Operation(summary = "Taux de couverture des garanties d'un crédit")
    @GetMapping("/credit/{idCredit}/couverture")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<GarantieDTO.CouvertureDTO> getCouverture(
            @PathVariable Long idCredit) {
        return ResponseEntity.ok(service.calculerCouverture(idCredit));
    }

    // ── PATCH /api/v1/garanties/{id}/liberer ─────────────────────────────

    @Operation(summary = "Libérer une garantie (crédit soldé)")
    @PatchMapping("/{id}/liberer")
    @PreAuthorize("hasAuthority('PRIV_POST_REGLEMENT')")
    public ResponseEntity<GarantieDTO> liberer(
            @PathVariable Long id,
            @Valid @RequestBody GarantieDTO.MainleveeRequest req) {
        return ResponseEntity.ok(service.liberer(id, req));
    }

    // ── Documents justificatifs ────────────────────────────────────────────

    @Operation(summary = "Uploader une preuve (document) pour une garantie")
    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRIV_POST_REGLEMENT')")
    public ResponseEntity<GarantieDocumentDTO> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        GarantieDocumentDTO dto = documentService.upload(id, file, auth != null ? auth.getName() : "SYSTEM");
        return ResponseEntity.status(201).body(dto);
    }

    @Operation(summary = "Lister les documents d'une garantie")
    @GetMapping("/{id}/documents")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<GarantieDocumentDTO>> listDocuments(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.list(id));
    }

    @Operation(summary = "Télécharger un document d'une garantie")
    @GetMapping("/{id}/documents/{docId}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS')")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            @PathVariable Long docId) {
        var dl = documentService.download(id, docId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dl.filename() + "\"")
                .contentType(MediaType.parseMediaType(dl.contentType()))
                .body(dl.resource());
    }
}

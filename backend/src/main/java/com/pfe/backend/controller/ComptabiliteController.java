package com.pfe.backend.controller;

import com.pfe.backend.dto.ComptabiliteDTO;
import com.pfe.backend.service.ComptabiliteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ComptabiliteController – API REST en lecture seule pour le journal comptable.
 *
 * <h2>Base URL</h2>
 * <pre>/api/v1/comptabilite</pre>
 *
 * <p>Les écritures sont créées automatiquement par les modules fonctionnels.
 * Ce contrôleur expose uniquement des opérations de consultation.</p>
 */
@Tag(name = "Comptabilité", description = "Consultation du journal comptable (lecture seule)")
@RestController
@RequestMapping("/api/v1/comptabilite")
public class ComptabiliteController {

    private final ComptabiliteService service;

    public ComptabiliteController(ComptabiliteService service) {
        this.service = service;
    }

    // ── GET / ─────────────────────────────────────────────────────────────────

    @Operation(summary = "Lister toutes les écritures comptables")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_EXPORT_REPORTS')")
    public ResponseEntity<List<ComptabiliteDTO.Response>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // ── GET /{id} ─────────────────────────────────────────────────────────────

    @Operation(summary = "Obtenir une écriture comptable par identifiant")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_EXPORT_REPORTS')")
    public ResponseEntity<ComptabiliteDTO.Response> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    // ── GET /agence/{codeAgence} ──────────────────────────────────────────────

    @Operation(summary = "Lister les écritures comptables d'une agence")
    @GetMapping("/agence/{codeAgence}")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_EXPORT_REPORTS')")
    public ResponseEntity<List<ComptabiliteDTO.Response>> findByAgence(
            @PathVariable String codeAgence) {
        return ResponseEntity.ok(service.findByAgence(codeAgence));
    }

    // ── PATCH /{id}/lettrer ───────────────────────────────────────────────────

    /**
     * Lettre une écriture comptable en lui affectant un code de lettrage.
     *
     * <p>Le lettrage consiste à associer un code alphanumérique ({@code codeLettrage})
     * à l'écriture et à horodater l'opération ({@code dateLettrage = today}).</p>
     *
     * @param id   identifiant de l'écriture
     * @param req  payload JSON : {@code { "codeLettrage": "A1" }}
     * @return l'écriture mise à jour
     */
    @Operation(summary = "Lettrer une écriture comptable")
    @PatchMapping("/{id}/lettrer")
    @PreAuthorize("hasAuthority('PRIV_POST_REGLEMENT')")
    public ResponseEntity<ComptabiliteDTO.Response> lettrer(
            @PathVariable Long id,
            @Valid @RequestBody LettrageRequest req) {
        return ResponseEntity.ok(service.lettrer(id, req.codeLettrage()));
    }

    /** Payload du lettrage. */
    public record LettrageRequest(
            @NotBlank @Size(max = 20) String codeLettrage
    ) {}
}

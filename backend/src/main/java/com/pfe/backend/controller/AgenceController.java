package com.pfe.backend.controller;

import com.microfina.entity.Agence;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.AgenceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AgenceController — consultation du référentiel agences.
 *
 * <pre>
 *   GET /api/v1/agences               — liste toutes les agences
 *   GET /api/v1/agences/{code}        — détail d'une agence
 *   GET /api/v1/agences/sieges        — sièges uniquement (ISSIEGE=1)
 * </pre>
 *
 * <p>Sécurité : lecture seule, requiert {@code PRIV_VIEW_REPORTS}.</p>
 */
@Tag(name = "Agences", description = "Référentiel des agences et du réseau")
@RestController
@RequestMapping("/api/v1/agences")
@PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_EXPORT_REPORTS','PRIV_BANK_OPERATION')")
public class AgenceController {

    private final AgenceRepository agenceRepository;

    public AgenceController(AgenceRepository agenceRepository) {
        this.agenceRepository = agenceRepository;
    }

    // ── GET /api/v1/agences ───────────────────────────────────────────────────

    /**
     * Retourne toutes les agences (actives et inactives).
     *
     * @param actif filtre optionnel (true = actives seulement)
     */
    @Operation(summary = "Lister toutes les agences du réseau")
    @GetMapping
    public ResponseEntity<List<AgenceDto>> findAll(
            @RequestParam(required = false) Boolean actif) {

        List<Agence> agences = (actif != null)
                ? agenceRepository.findAll().stream()
                        .filter(a -> actif.equals(a.getActif()))
                        .toList()
                : agenceRepository.findAll();

        return ResponseEntity.ok(agences.stream().map(AgenceDto::from).toList());
    }

    // ── GET /api/v1/agences/{code} ────────────────────────────────────────────

    /**
     * Retourne le détail d'une agence par son code.
     *
     * @param code code agence (clé primaire)
     * @throws ResourceNotFoundException si l'agence est introuvable
     */
    @Operation(summary = "Obtenir une agence par son code")
    @GetMapping("/{code}")
    public ResponseEntity<AgenceDto> findByCode(@PathVariable String code) {
        return agenceRepository.findById(code)
                .map(AgenceDto::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Agence", code));
    }

    // ── GET /api/v1/agences/sieges ────────────────────────────────────────────

    /**
     * Retourne uniquement les agences siège (ISSIEGE = '1').
     */
    @Operation(summary = "Lister les agences siège")
    @GetMapping("/sieges")
    public ResponseEntity<List<AgenceDto>> findSieges() {
        List<AgenceDto> sieges = agenceRepository.findAll().stream()
                .filter(a -> "1".equals(a.getIsSiege()))
                .map(AgenceDto::from)
                .toList();
        return ResponseEntity.ok(sieges);
    }

    // ── DTO ───────────────────────────────────────────────────────────────────

    /**
     * Projection publique d'une agence (sans données techniques de connexion).
     */
    public record AgenceDto(
            String  codeAgence,
            String  nomAgence,
            Boolean actif,
            String  isSiege,
            String  chefAgence,
            Integer zoneGeographique
    ) {
        public static AgenceDto from(Agence a) {
            return new AgenceDto(
                    a.getCodeAgence(),
                    a.getNomAgence(),
                    a.getActif(),
                    a.getIsSiege(),
                    a.getChefAgence(),
                    a.getZoneGeographique()
            );
        }
    }
}

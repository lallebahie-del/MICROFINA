package com.pfe.backend.controller;

import com.microfina.entity.Agence;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.AgenceRepository;
import com.pfe.backend.service.AgenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

/**
 * AgenceController — référentiel agences (lecture + administration).
 *
 * <pre>
 *   GET    /api/v1/agences               — liste toutes les agences
 *   GET    /api/v1/agences/{code}        — détail d'une agence
 *   GET    /api/v1/agences/sieges        — sièges uniquement
 *   POST   /api/v1/agences               — création (PRIV_MANAGE_PARAMS)
 *   PUT    /api/v1/agences/{code}        — mise à jour (PRIV_MANAGE_PARAMS)
 *   DELETE /api/v1/agences/{code}        — suppression (PRIV_MANAGE_PARAMS)
 * </pre>
 */
@Tag(name = "Agences", description = "Référentiel des agences et du réseau")
@RestController
@RequestMapping("/api/v1/agences")
public class AgenceController {

    private final AgenceRepository agenceRepository;
    private final AgenceService agenceService;

    public AgenceController(AgenceRepository agenceRepository, AgenceService agenceService) {
        this.agenceRepository = agenceRepository;
        this.agenceService = agenceService;
    }

    // ── GET /api/v1/agences ───────────────────────────────────────────────────

    @Operation(summary = "Lister toutes les agences du réseau")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_EXPORT_REPORTS','PRIV_BANK_OPERATION','PRIV_MANAGE_PARAMS')")
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

    @Operation(summary = "Obtenir une agence par son code")
    @GetMapping("/{code}")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_EXPORT_REPORTS','PRIV_BANK_OPERATION','PRIV_MANAGE_PARAMS')")
    public ResponseEntity<AgenceDto> findByCode(@PathVariable String code) {
        return agenceRepository.findById(code)
                .map(AgenceDto::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Agence", code));
    }

    // ── GET /api/v1/agences/sieges ────────────────────────────────────────────

    @Operation(summary = "Lister les agences siège")
    @GetMapping("/sieges")
    @PreAuthorize("hasAnyAuthority('PRIV_VIEW_REPORTS','PRIV_EXPORT_REPORTS','PRIV_BANK_OPERATION','PRIV_MANAGE_PARAMS')")
    public ResponseEntity<List<AgenceDto>> findSieges() {
        List<AgenceDto> sieges = agenceRepository.findAll().stream()
                .filter(a -> "1".equals(a.getIsSiege()))
                .map(AgenceDto::from)
                .toList();
        return ResponseEntity.ok(sieges);
    }

    // ── POST /api/v1/agences ──────────────────────────────────────────────────

    @Operation(summary = "Créer une agence")
    @PostMapping
    @PreAuthorize("hasAuthority('PRIV_MANAGE_PARAMS')")
    public ResponseEntity<AgenceDto> create(@RequestBody AgenceWriteRequest req) {
        Agence created = agenceService.create(req.toEntity(new Agence()));
        return ResponseEntity
            .created(URI.create("/api/v1/agences/" + created.getCodeAgence()))
            .body(AgenceDto.from(created));
    }

    // ── PUT /api/v1/agences/{code} ────────────────────────────────────────────

    @Operation(summary = "Mettre à jour une agence")
    @PutMapping("/{code}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_PARAMS')")
    public ResponseEntity<AgenceDto> update(@PathVariable String code,
                                            @RequestBody AgenceWriteRequest req) {
        // Le code dans le path est la source de vérité ; on ignore celui du body.
        Agence patch = req.toEntity(new Agence());
        patch.setCodeAgence(code);
        Agence updated = agenceService.update(code, patch);
        return ResponseEntity.ok(AgenceDto.from(updated));
    }

    // ── DELETE /api/v1/agences/{code} ─────────────────────────────────────────

    @Operation(summary = "Supprimer une agence")
    @DeleteMapping("/{code}")
    @PreAuthorize("hasAuthority('PRIV_MANAGE_PARAMS')")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        agenceService.delete(code);
        return ResponseEntity.noContent().build();
    }

    // ── DTO de lecture ────────────────────────────────────────────────────────

    public record AgenceDto(
            String     codeAgence,
            String     nomAgence,
            String     nomCourt,
            Boolean    actif,
            String     isSiege,
            String     chefAgence,
            String     nomPrenomChefAgence,
            String     institution,
            Integer    zoneGeographique,
            String     numCompte,
            String     compteCaisse,
            String     compteCrediteur,
            String     numeroSms,
            BigDecimal longitude,
            BigDecimal latitude
    ) {
        public static AgenceDto from(Agence a) {
            return new AgenceDto(
                    a.getCodeAgence(),
                    a.getNomAgence(),
                    a.getNomCourt(),
                    a.getActif(),
                    a.getIsSiege(),
                    a.getChefAgence(),
                    a.getNomPrenomChefAgence(),
                    a.getInstitution(),
                    a.getZoneGeographique(),
                    a.getNumCompte(),
                    a.getCompteCaisse(),
                    a.getCompteCrediteur(),
                    a.getNumeroSms(),
                    a.getLongitude(),
                    a.getLatitude()
            );
        }
    }

    // ── DTO d'écriture ───────────────────────────────────────────────────────

    public record AgenceWriteRequest(
            String     codeAgence,
            String     nomAgence,
            String     nomCourt,
            Boolean    actif,
            String     isSiege,
            String     chefAgence,
            String     nomPrenomChefAgence,
            String     institution,
            Integer    zoneGeographique,
            String     numCompte,
            String     compteCaisse,
            String     compteCrediteur,
            String     numeroSms,
            BigDecimal longitude,
            BigDecimal latitude
    ) {
        public Agence toEntity(Agence target) {
            target.setCodeAgence(codeAgence);
            target.setNomAgence(nomAgence);
            target.setNomCourt(nomCourt);
            target.setActif(actif);
            target.setIsSiege(isSiege);
            target.setChefAgence(chefAgence);
            target.setNomPrenomChefAgence(nomPrenomChefAgence);
            target.setInstitution(institution);
            target.setZoneGeographique(zoneGeographique);
            target.setNumCompte(numCompte);
            target.setCompteCaisse(compteCaisse);
            target.setCompteCrediteur(compteCrediteur);
            target.setNumeroSms(numeroSms);
            target.setLongitude(longitude);
            target.setLatitude(latitude);
            return target;
        }
    }
}

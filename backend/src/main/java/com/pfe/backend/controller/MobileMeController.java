package com.pfe.backend.controller;

import com.microfina.entity.CompteEps;
import com.microfina.entity.Credits;
import com.microfina.entity.Utilisateur;
import com.pfe.backend.dto.CompteEpsDTO;
import com.pfe.backend.dto.CreditDTO;
import com.pfe.backend.repository.CompteEpsRepository;
import com.pfe.backend.repository.CreditsRepository;
import com.pfe.backend.repository.UtilisateurRepository;
import com.pfe.backend.service.MobileAccountProvisioningService;
import com.pfe.backend.service.MobileMeProfileService;
import com.pfe.backend.service.MobileMeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * MobileMeController — endpoints "self-service" pour l'application mobile.
 *
 * <p>Tous les endpoints opèrent sur le contexte de l'utilisateur authentifié
 * (JWT) — pas de paramètre {@code login} ou {@code phone} dans l'URL.</p>
 *
 * <pre>
 *   GET  /api/v1/mobile/me/profile                          — identité du user
 *   GET  /api/v1/mobile/me/comptes                          — comptes accessibles
 *   GET  /api/v1/mobile/me/transactions/{numCompte}         — historique paginé
 *   POST /api/v1/mobile/me/transfer                         — virement compte→compte
 *   POST /api/v1/mobile/me/pay                              — paiement service (facturier)
 * </pre>
 */
@Tag(name = "Mobile — Self-service", description = "API mobile centrée sur l'utilisateur courant")
@RestController
@RequestMapping("/api/v1/mobile/me")
@PreAuthorize("isAuthenticated()")
public class MobileMeController {

    private final UtilisateurRepository utilisateurRepo;
    private final CompteEpsRepository   compteEpsRepo;
    private final CreditsRepository     creditsRepo;
    private final MobileMeService       mobileService;
    private final MobileMeProfileService profileService;
    private final MobileAccountProvisioningService provisioningService;

    public MobileMeController(UtilisateurRepository utilisateurRepo,
                              CompteEpsRepository compteEpsRepo,
                              CreditsRepository creditsRepo,
                              MobileMeService mobileService,
                              MobileMeProfileService profileService,
                              MobileAccountProvisioningService provisioningService) {
        this.utilisateurRepo = utilisateurRepo;
        this.compteEpsRepo = compteEpsRepo;
        this.creditsRepo = creditsRepo;
        this.mobileService = mobileService;
        this.profileService = profileService;
        this.provisioningService = provisioningService;
    }

    // ── Profil ──────────────────────────────────────────────────────────────

    @Operation(summary = "Profil de l'utilisateur courant (mobile)")
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> profile(Authentication auth) {
        Utilisateur user = currentUser(auth);
        user = provisioningService.provisionIfMissing(user);
        return ResponseEntity.ok(profileService.buildProfile(user));
    }

    // ── Comptes ─────────────────────────────────────────────────────────────

    @Operation(summary = "Comptes épargne accessibles à l'utilisateur courant")
    @GetMapping("/comptes")
    public ResponseEntity<List<CompteEpsDTO.Response>> comptes(Authentication auth) {
        Utilisateur user = provisioningService.provisionIfMissing(currentUser(auth));

        List<CompteEps> list;
        if (user.getNumMembre() != null && !user.getNumMembre().isBlank()) {
            list = compteEpsRepo.findByMembre_NumMembre(user.getNumMembre());
        } else if (user.getAgence() != null && user.getAgence().getCodeAgence() != null) {
            list = compteEpsRepo.findByAgence_CodeAgence(user.getAgence().getCodeAgence());
        } else {
            list = compteEpsRepo.findAll();
        }
        if (list.size() > 100) {
            list = list.subList(0, 100);
        }
        return ResponseEntity.ok(list.stream().map(CompteEpsDTO.Response::from).toList());
    }

    // ── Crédits ─────────────────────────────────────────────────────────────

    @Operation(summary = "Crédits accessibles à l'utilisateur courant")
    @GetMapping("/credits")
    public ResponseEntity<List<CreditDTO>> credits(Authentication auth) {
        Utilisateur user = currentUser(auth);

        List<Credits> list;
        if (user.getAgence() != null && user.getAgence().getCodeAgence() != null) {
            list = creditsRepo.findByAgence_CodeAgence(user.getAgence().getCodeAgence());
        } else {
            list = creditsRepo.findAll();
        }
        if (list.size() > 100) {
            list = list.subList(0, 100);
        }
        return ResponseEntity.ok(list.stream().map(CreditDTO::from).toList());
    }

    // ── Transactions ────────────────────────────────────────────────────────

    @Operation(summary = "Historique paginé des mouvements d'un compte")
    @GetMapping("/transactions/{numCompte}")
    public ResponseEntity<Map<String, Object>> transactions(
            @PathVariable String numCompte,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        // Pas de filtre stricte ici : les rôles agent/admin peuvent consulter
        // tous les comptes de leur agence. Une vérification d'appartenance
        // membre→user serait à ajouter quand le data model lie Utilisateur↔Membres.
        return ResponseEntity.ok(mobileService.getTransactions(numCompte, page, size));
    }

    // ── Virement interne ────────────────────────────────────────────────────

    public record TransferRequest(
        String compteSource,
        String compteDestinataire,
        BigDecimal montant,
        String libelle
    ) {}

    @Operation(summary = "Virement entre deux comptes épargne (compte → compte)")
    @PostMapping("/transfer")
    public ResponseEntity<Map<String, Object>> transfer(
            @RequestBody TransferRequest req,
            Authentication auth) {
        return ResponseEntity.ok(mobileService.transfer(
            auth.getName(),
            req.compteSource(),
            req.compteDestinataire(),
            req.montant(),
            req.libelle()
        ));
    }

    public record ExternalTransferRequest(
        String compteSource,
        String telephoneBeneficiaire,
        BigDecimal montant,
        String libelle,
        String nomBeneficiaire,
        String banque
    ) {}

    @Operation(summary = "Virement vers un autre client mobile (par numéro de téléphone)")
    @PostMapping("/transfer-external")
    public ResponseEntity<Map<String, Object>> transferExternal(
            @RequestBody ExternalTransferRequest req,
            Authentication auth) {
        return ResponseEntity.ok(mobileService.transferExternalByPhone(
            auth.getName(),
            req.compteSource(),
            req.telephoneBeneficiaire(),
            req.nomBeneficiaire(),
            req.banque(),
            req.montant(),
            req.libelle()
        ));
    }

    // ── Paiement service ───────────────────────────────────────────────────

    public record PayRequest(
        String compte,
        String service,
        String reference,
        BigDecimal montant
    ) {}

    @Operation(summary = "Paiement d'une facture / d'un service depuis un compte épargne")
    @PostMapping("/pay")
    public ResponseEntity<Map<String, Object>> pay(
            @RequestBody PayRequest req,
            Authentication auth) {
        return ResponseEntity.ok(mobileService.payService(
            auth.getName(),
            req.compte(),
            req.service(),
            req.reference(),
            req.montant()
        ));
    }

    // ── Notifications ───────────────────────────────────────────────────────

    @Operation(summary = "Notifications de l'utilisateur courant (paginées)")
    @GetMapping("/notifications")
    public ResponseEntity<Map<String, Object>> notifications(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        return ResponseEntity.ok(mobileService.getNotifications(auth.getName(), page, size));
    }

    @Operation(summary = "Marquer une notification comme lue")
    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long id,
            Authentication auth) {
        var n = mobileService.markAsRead(auth.getName(), id);
        return ResponseEntity.ok(Map.of(
            "id", n.getId(),
            "lu", Boolean.TRUE.equals(n.getLu()),
            "dateLecture", n.getDateLecture() != null ? n.getDateLecture().toString() : ""
        ));
    }

    @Operation(summary = "Marquer toutes les notifications comme lues")
    @PatchMapping("/notifications/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication auth) {
        int updated = mobileService.markAllAsRead(auth.getName());
        return ResponseEntity.ok(Map.of("statut", "OK", "marquees", updated));
    }

    // ── Upload photo de profil ─────────────────────────────────────────────

    @Operation(summary = "Upload de la photo de profil de l'utilisateur courant (multipart/form-data)")
    @PostMapping(value = "/photo", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        return ResponseEntity.ok(mobileService.uploadProfilePhoto(auth.getName(), file));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private Utilisateur currentUser(Authentication auth) {
        return utilisateurRepo.findByLogin(auth.getName())
            .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable : " + auth.getName()));
    }
}

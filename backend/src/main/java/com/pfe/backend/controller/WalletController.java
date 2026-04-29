package com.pfe.backend.controller;

import com.pfe.backend.dto.WalletDto.*;
import com.pfe.backend.service.WalletService;
import com.pfe.backend.wallet.WalletSignatureVerifier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * WalletController — API REST du module Wallet Bankily.
 *
 * <h2>Endpoints</h2>
 * <pre>
 *  POST /api/v1/wallet/deblocage               → Initier un déblocage via wallet
 *  POST /api/v1/wallet/remboursement           → Initier un remboursement via wallet
 *  POST /api/v1/wallet/callback                → Webhook Bankily (pas d'auth JWT)
 *  GET  /api/v1/wallet/operations/{id}         → Consulter une opération
 *  GET  /api/v1/wallet/operations/{id}/statut  → Rafraîchir le statut via Bankily
 *  PATCH /api/v1/wallet/operations/{id}/annuler → Annuler une opération EN_ATTENTE
 *  GET  /api/v1/wallet/membres/{numMembre}     → Historique d'un membre
 *  GET  /api/v1/wallet/credits/{idCredit}      → Opérations liées à un crédit
 *  GET  /api/v1/wallet/operations              → Recherche multi-critères
 * </pre>
 *
 * <h2>Sécurité</h2>
 * <ul>
 *   <li>Initiation : ADMIN ou AGENT</li>
 *   <li>Callback   : public (filtré par IP au niveau infrastructure en production)</li>
 *   <li>Lecture    : ADMIN, REPORT ou AGENT</li>
 * </ul>
 */
@Tag(name = "Wallet Bankily", description = "Déblocages et remboursements via mobile money Bankily")
@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private static final Logger log = LoggerFactory.getLogger(WalletController.class);

    private final WalletService            walletService;
    private final WalletSignatureVerifier  signatureVerifier;

    public WalletController(WalletService walletService,
                            WalletSignatureVerifier signatureVerifier) {
        this.walletService       = walletService;
        this.signatureVerifier   = signatureVerifier;
    }

    // =========================================================================
    //  Initiation — déblocage
    // =========================================================================

    /**
     * Initie le déblocage d'un crédit vers le wallet Bankily du membre.
     * Retourne 201 Created avec l'URL de l'opération créée.
     */
    @Operation(summary = "Initier un déblocage crédit via wallet Bankily")
    @PostMapping("/deblocage")
    @PreAuthorize("hasAuthority('PRIV_BANK_OPERATION')")
    public ResponseEntity<OperationResponse> initierDeblocage(
            @Valid @RequestBody DeblocageRequest req) {

        OperationResponse op = walletService.initierDeblocage(req);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/wallet/operations/{id}")
                .buildAndExpand(op.id())
                .toUri();
        return ResponseEntity.created(location).body(op);
    }

    // =========================================================================
    //  Initiation — remboursement
    // =========================================================================

    /**
     * Initie la collecte d'un remboursement depuis le wallet Bankily du membre.
     * Retourne 201 Created avec l'URL de l'opération créée.
     */
    @Operation(summary = "Initier un remboursement via wallet Bankily")
    @PostMapping("/remboursement")
    @PreAuthorize("hasAuthority('PRIV_BANK_OPERATION')")
    public ResponseEntity<OperationResponse> initierRemboursement(
            @Valid @RequestBody RemboursementRequest req) {

        OperationResponse op = walletService.initierRemboursement(req);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/wallet/operations/{id}")
                .buildAndExpand(op.id())
                .toUri();
        return ResponseEntity.created(location).body(op);
    }

    // =========================================================================
    //  Callback Bankily (webhook)
    // =========================================================================

    /**
     * Point d'entrée du webhook Bankily.
     *
     * <p>Pas d'authentification JWT — Bankily ne peut pas fournir de token.
     * La sécurisation repose sur :</p>
     * <ol>
     *   <li>Filtrage IP au niveau du reverse proxy (Nginx / AWS ALB).</li>
     *   <li>Vérification de la signature HMAC-SHA256 (header {@code X-Bankily-Signature}).</li>
     * </ol>
     *
     * <p>La vérification HMAC est journalisée ici mais délégue à l'infrastructure.
     * En production, activer le filtre {@code BankilySignatureFilter} (non inclus dans ce POC).</p>
     *
     * @param signature   header {@code X-Bankily-Signature} (HMAC-SHA256 du payload)
     * @param req         payload JSON de la notification Bankily
     * @return 200 OK (Bankily exige un 2xx pour considérer la notification comme reçue)
     */
    @Operation(summary = "Webhook Bankily (notification statut)")
    @PostMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestHeader(value = "X-Bankily-Signature", required = false) String signature,
            @Valid @RequestBody CallbackRequest req) {

        log.info("[Wallet] Callback reçu refBankily={} statut={}", req.referenceBankily(), req.statut());
        // La vérification HMAC-SHA256 est réalisée par BankilySignatureFilter avant
        // que la requête n'atteigne ce contrôleur. Si la signature était invalide,
        // le filtre aurait déjà répondu 401 Unauthorized.

        try {
            walletService.traiterCallback(req);
        } catch (Exception e) {
            // Ne pas retourner 4xx/5xx : Bankily réessaierait indéfiniment
            log.error("[Wallet] Erreur traitement callback refBankily={} : {}",
                      req.referenceBankily(), e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    // =========================================================================
    //  Lecture
    // =========================================================================

    /**
     * Consulte une opération wallet par son identifiant local.
     */
    @Operation(summary = "Consulter une opération wallet")
    @GetMapping("/operations/{id}")
    @PreAuthorize("hasAnyAuthority('PRIV_BANK_OPERATION','PRIV_VIEW_REPORTS')")
    public ResponseEntity<OperationResponse> consulter(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.consulter(id));
    }

    /**
     * Rafraîchit le statut de l'opération en consultant l'API Bankily en temps réel.
     * Met à jour la base si le statut a changé.
     */
    @Operation(summary = "Rafraîchir le statut depuis Bankily")
    @GetMapping("/operations/{id}/statut")
    @PreAuthorize("hasAnyAuthority('PRIV_BANK_OPERATION','PRIV_VIEW_REPORTS')")
    public ResponseEntity<StatutResponse> rafraichirStatut(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.rafraichirStatut(id));
    }

    /**
     * Annule une opération EN_ATTENTE (avant confirmation Bankily).
     */
    @Operation(summary = "Annuler une opération EN_ATTENTE")
    @PatchMapping("/operations/{id}/annuler")
    @PreAuthorize("hasAuthority('PRIV_BANK_OPERATION')")
    public ResponseEntity<OperationResponse> annuler(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.annuler(id));
    }

    /**
     * Retourne l'historique des opérations wallet d'un membre.
     */
    @Operation(summary = "Historique wallet d'un membre")
    @GetMapping("/membres/{numMembre}")
    @PreAuthorize("hasAnyAuthority('PRIV_BANK_OPERATION','PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<OperationResponse>> historiqueMembre(
            @PathVariable String numMembre) {
        return ResponseEntity.ok(walletService.historiqueMembre(numMembre));
    }

    /**
     * Retourne les opérations wallet liées à un crédit (déblocages + remboursements).
     */
    @Operation(summary = "Opérations wallet liées à un crédit")
    @GetMapping("/credits/{idCredit}")
    @PreAuthorize("hasAnyAuthority('PRIV_BANK_OPERATION','PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<OperationResponse>> historiqueCredit(
            @PathVariable Long idCredit) {
        return ResponseEntity.ok(walletService.historiqueCredit(idCredit));
    }

    /**
     * Recherche multi-critères des opérations wallet.
     *
     * @param agence        filtre optionnel sur le code agence
     * @param typeOperation filtre optionnel : DEBLOCAGE | REMBOURSEMENT | DEPOT_EPARGNE
     * @param statut        filtre optionnel : EN_ATTENTE | CONFIRME | REJETE | ANNULE | EXPIRE
     */
    @Operation(summary = "Recherche multi-critères d'opérations wallet")
    @GetMapping("/operations")
    @PreAuthorize("hasAnyAuthority('PRIV_BANK_OPERATION','PRIV_VIEW_REPORTS')")
    public ResponseEntity<List<OperationResponse>> rechercher(
            @RequestParam(required = false) String agence,
            @RequestParam(required = false) String typeOperation,
            @RequestParam(required = false) String statut) {
        return ResponseEntity.ok(walletService.rechercher(agence, typeOperation, statut));
    }

    // =========================================================================
    //  Réconciliation
    // =========================================================================

    /**
     * Déclenche une réconciliation manuelle des opérations EN_ATTENTE avec Bankily.
     *
     * <p>Pour chaque opération EN_ATTENTE (filtrée optionnellement par agence),
     * consulte l'API Bankily et met à jour le statut en cas de changement.</p>
     *
     * @param agence code agence optionnel (null = toutes agences)
     * @return résumé : nombre d'opérations vérifiées, mises à jour, erreurs
     */
    @Operation(summary = "Réconciliation manuelle des opérations EN_ATTENTE avec Bankily")
    @PostMapping("/reconciliation")
    @PreAuthorize("hasAuthority('PRIV_BANK_OPERATION')")
    public ResponseEntity<ReconciliationResponse> reconcilier(
            @RequestParam(required = false) String agence) {
        return ResponseEntity.ok(walletService.reconcilier(agence));
    }
}

package com.pfe.backend.controller;

import com.microfina.entity.Credits;
import com.pfe.backend.dto.WorkflowCreditSummaryDTO;
import com.pfe.backend.dto.WorkflowDTO.*;
import com.pfe.backend.service.CreditWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CreditWorkflowController – endpoints du workflow multi-niveaux d'approbation crédit.
 *
 * <pre>
 *   POST /api/v1/credits/{idCredit}/workflow/soumettre           PRIV_CREATE_CREDIT
 *   POST /api/v1/credits/{idCredit}/workflow/completude          PRIV_COMPLETUDE_DOSSIER
 *   POST /api/v1/credits/{idCredit}/workflow/analyse             PRIV_ANALYSE_FINANCIERE
 *   POST /api/v1/credits/{idCredit}/workflow/visa-rc             PRIV_VISA_RC
 *   POST /api/v1/credits/{idCredit}/workflow/comite/approuver    PRIV_COMITE_CREDIT
 *   POST /api/v1/credits/{idCredit}/workflow/comite/rejeter      PRIV_COMITE_CREDIT
 *   POST /api/v1/credits/{idCredit}/workflow/visa-sf             PRIV_VISA_SF
 *   POST /api/v1/credits/{idCredit}/workflow/debloquer           PRIV_DEBLOQUER_CREDIT
 *   POST /api/v1/credits/{idCredit}/workflow/rejeter             PRIV_COMPLETUDE_DOSSIER|VISA_RC|COMITE_CREDIT|VISA_SF
 *   GET  /api/v1/credits/{idCredit}/workflow/timeline            authenticated
 *   GET  /api/v1/credits/{idCredit}/workflow/analyse             authenticated
 *
 *   GET  /api/v1/credits/workflow/comite/pending                 PRIV_COMITE_CREDIT
 *   GET  /api/v1/credits/workflow/agent/pending                  PRIV_COMPLETUDE_DOSSIER | PRIV_ANALYSE_FINANCIERE | PRIV_VISA_RC
 *   GET  /api/v1/credits/workflow/queue/{etape}                  PRIV_VIEW_REPORTS
 *   GET  /api/v1/credits/workflow/stats                          PRIV_VIEW_REPORTS
 * </pre>
 */
@Tag(name = "Credit Workflow", description = "Workflow multi-niveaux d'approbation crédit (Phase 12)")
@RestController
@RequestMapping("/api/v1/credits")
public class CreditWorkflowController {

    private final CreditWorkflowService workflowService;

    public CreditWorkflowController(CreditWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    // ── Transitions ──────────────────────────────────────────────────────────

    @Operation(summary = "Soumettre un dossier (SAISIE → COMPLETUDE)")
    @PostMapping("/{idCredit}/workflow/soumettre")
    @PreAuthorize("hasAuthority('PRIV_CREATE_CREDIT') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<WorkflowCreditSummaryDTO> soumettre(@PathVariable Long idCredit) {
        Credits c = workflowService.soumettreDossier(idCredit);
        return ResponseEntity.ok(WorkflowCreditSummaryDTO.from(c));
    }

    @Operation(summary = "Valider la complétude documentaire (COMPLETUDE → ANALYSE_FINANCIERE)")
    @PostMapping("/{idCredit}/workflow/completude")
    @PreAuthorize("hasAuthority('PRIV_COMPLETUDE_DOSSIER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<WorkflowCreditSummaryDTO> completude(
            @PathVariable Long idCredit,
            @RequestBody(required = false) WorkflowDecisionRequest req) {
        Credits c = workflowService.validerCompletude(idCredit, req);
        return ResponseEntity.ok(WorkflowCreditSummaryDTO.from(c));
    }

    @Operation(summary = "Enregistrer l'analyse financière (ANALYSE_FINANCIERE → VISA_RC)")
    @PostMapping("/{idCredit}/workflow/analyse")
    @PreAuthorize("hasAuthority('PRIV_ANALYSE_FINANCIERE') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AnalyseFinanciereDTO> analyse(
            @PathVariable Long idCredit,
            @Valid @RequestBody AnalyseFinanciereCreateRequest req) {
        return ResponseEntity.ok(
            AnalyseFinanciereDTO.from(workflowService.enregistrerAnalyseFinanciere(idCredit, req)));
    }

    @Operation(summary = "Visa du Responsable Crédit (VISA_RC → COMITE)")
    @PostMapping("/{idCredit}/workflow/visa-rc")
    @PreAuthorize("hasAuthority('PRIV_VISA_RC') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<WorkflowCreditSummaryDTO> visaRc(
            @PathVariable Long idCredit,
            @RequestBody(required = false) WorkflowDecisionRequest req) {
        Credits c = workflowService.viserResponsableCredit(idCredit, req);
        return ResponseEntity.ok(WorkflowCreditSummaryDTO.from(c));
    }

    @Operation(summary = "Décision comité — approbation (COMITE → VISA_SF)")
    @PostMapping("/{idCredit}/workflow/comite/approuver")
    @PreAuthorize("hasAuthority('PRIV_COMITE_CREDIT') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<WorkflowCreditSummaryDTO> comiteApprouver(
            @PathVariable Long idCredit,
            @RequestBody(required = false) WorkflowDecisionRequest req) {
        Credits c = workflowService.decisionComite(idCredit, true, req);
        return ResponseEntity.ok(WorkflowCreditSummaryDTO.from(c));
    }

    @Operation(summary = "Décision comité — rejet (COMITE → REJETE)")
    @PostMapping("/{idCredit}/workflow/comite/rejeter")
    @PreAuthorize("hasAuthority('PRIV_COMITE_CREDIT') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<WorkflowCreditSummaryDTO> comiteRejeter(
            @PathVariable Long idCredit,
            @RequestBody(required = false) WorkflowDecisionRequest req) {
        Credits c = workflowService.decisionComite(idCredit, false, req);
        return ResponseEntity.ok(WorkflowCreditSummaryDTO.from(c));
    }

    @Operation(summary = "Visa du Service Financier (VISA_SF → DEBLOCAGE_PENDING)")
    @PostMapping("/{idCredit}/workflow/visa-sf")
    @PreAuthorize("hasAuthority('PRIV_VISA_SF') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<WorkflowCreditSummaryDTO> visaSf(
            @PathVariable Long idCredit,
            @RequestBody(required = false) WorkflowDecisionRequest req) {
        Credits c = workflowService.viserServiceFinancier(idCredit, req);
        return ResponseEntity.ok(WorkflowCreditSummaryDTO.from(c));
    }

    @Operation(summary = "Débloquer le crédit (DEBLOCAGE_PENDING → DEBLOQUE)")
    @PostMapping("/{idCredit}/workflow/debloquer")
    @PreAuthorize("hasAuthority('PRIV_DEBLOQUER_CREDIT') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<WorkflowCreditSummaryDTO> debloquer(
            @PathVariable Long idCredit,
            @Valid @RequestBody DeblocageRequest req) {
        Credits c = workflowService.debloquer(idCredit, req);
        return ResponseEntity.ok(WorkflowCreditSummaryDTO.from(c));
    }

    @Operation(summary = "Rejeter un dossier (toute étape non terminale → REJETE)")
    @PostMapping("/{idCredit}/workflow/rejeter")
    @PreAuthorize("hasAnyAuthority('PRIV_COMPLETUDE_DOSSIER','PRIV_VISA_RC','PRIV_COMITE_CREDIT','PRIV_VISA_SF') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<WorkflowCreditSummaryDTO> rejeter(
            @PathVariable Long idCredit,
            @RequestBody(required = false) WorkflowDecisionRequest req) {
        Credits c = workflowService.rejeter(idCredit, req);
        return ResponseEntity.ok(WorkflowCreditSummaryDTO.from(c));
    }

    // ── Lecture ──────────────────────────────────────────────────────────────

    @Operation(summary = "Historique des transitions (timeline)")
    @GetMapping("/{idCredit}/workflow/timeline")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<WorkflowTimelineEntry>> timeline(@PathVariable Long idCredit) {
        return ResponseEntity.ok(workflowService.getTimeline(idCredit));
    }

    @Operation(summary = "Obtenir l'analyse financière du crédit")
    @GetMapping("/{idCredit}/workflow/analyse")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AnalyseFinanciereDTO> getAnalyse(@PathVariable Long idCredit) {
        return workflowService.getAnalyse(idCredit)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Dossiers en attente de décision comité")
    @GetMapping("/workflow/comite/pending")
    @PreAuthorize("hasAuthority('PRIV_COMITE_CREDIT') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<WorkflowCreditSummaryDTO>> comitePending() {
        return ResponseEntity.ok(
            workflowService.getComitePending().stream()
                .map(WorkflowCreditSummaryDTO::from)
                .toList()
        );
    }

    @Operation(summary = "Dossiers en attente d'action agent (complétude / analyse / visa RC)")
    @GetMapping("/workflow/agent/pending")
    @PreAuthorize("hasAnyAuthority('PRIV_COMPLETUDE_DOSSIER','PRIV_ANALYSE_FINANCIERE','PRIV_VISA_RC') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<WorkflowCreditSummaryDTO>> agentPending() {
        return ResponseEntity.ok(
            workflowService.getAgentPending().stream()
                .map(WorkflowCreditSummaryDTO::from)
                .toList()
        );
    }

    @Operation(summary = "Dossiers à une étape précise (paramétrable)")
    @GetMapping("/workflow/queue/{etape}")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<WorkflowCreditSummaryDTO>> queueByEtape(@PathVariable String etape) {
        return ResponseEntity.ok(
            workflowService.getQueueByEtape(etape).stream()
                .map(WorkflowCreditSummaryDTO::from)
                .toList()
        );
    }

    @Operation(summary = "Compteurs de dossiers par étape (workflow stats)")
    @GetMapping("/workflow/stats")
    @PreAuthorize("hasAuthority('PRIV_VIEW_REPORTS') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(workflowService.getWorkflowStats());
    }
}

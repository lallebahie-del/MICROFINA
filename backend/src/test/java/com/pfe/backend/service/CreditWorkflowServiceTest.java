package com.pfe.backend.service;

import com.microfina.entity.*;
import com.microfina.service.AmortissementService;
import com.pfe.backend.dto.WorkflowDTO.*;
import com.pfe.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreditWorkflowService — tests unitaires")
class CreditWorkflowServiceTest {

    @Mock CreditsRepository              creditsRepo;
    @Mock AnalyseFinanciereRepository    analyseRepo;
    @Mock HistoriqueVisaCreditRepository historiqueRepo;
    @Mock AmortpRepository               amortpRepo;
    @Mock AmortissementService           amortissementService;

    @InjectMocks CreditWorkflowService service;

    private Credits creditSaisie;

    @BeforeEach
    void setup() {
        creditSaisie = new Credits();
        creditSaisie.setIdCredit(1L);
        creditSaisie.setStatut(CreditStatut.BROUILLON);
        creditSaisie.setEtapeCourante("SAISIE");
        creditSaisie.setMontantDemande(new BigDecimal("500000"));
    }

    // ── 1. Happy path : soumettre ────────────────────────────────────────────

    @Test
    @DisplayName("soumettreDossier : SAISIE → COMPLETUDE, statut SOUMIS")
    void soumettreDossierHappyPath() {
        when(creditsRepo.findById(1L)).thenReturn(Optional.of(creditSaisie));
        when(creditsRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(historiqueRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Credits result = service.soumettreDossier(1L);

        assertThat(result.getStatut()).isEqualTo(CreditStatut.SOUMIS);
        assertThat(result.getEtapeCourante()).isEqualTo("COMPLETUDE");
        verify(historiqueRepo).save(argThat(h -> "COMPLETUDE".equals(h.getEtape())));
    }

    // ── 2. Transition illégale ───────────────────────────────────────────────

    @Test
    @DisplayName("viserResponsableCredit avec étape COMPLETUDE → 409 CONFLICT")
    void transitionIllegale_viserRCsurCompletude() {
        creditSaisie.setEtapeCourante("COMPLETUDE");
        when(creditsRepo.findById(1L)).thenReturn(Optional.of(creditSaisie));

        assertThatThrownBy(() -> service.viserResponsableCredit(1L, null))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409");
    }

    // ── 3. Rejet au comité ───────────────────────────────────────────────────

    @Test
    @DisplayName("decisionComite(false) : COMITE → REJETE")
    void decisionComiteRejet() {
        creditSaisie.setStatut(CreditStatut.VALIDE_AGENT);
        creditSaisie.setEtapeCourante("COMITE");
        when(creditsRepo.findById(1L)).thenReturn(Optional.of(creditSaisie));
        when(creditsRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(historiqueRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WorkflowDecisionRequest req = new WorkflowDecisionRequest("Dossier incomplet");
        Credits result = service.decisionComite(1L, false, req);

        assertThat(result.getStatut()).isEqualTo(CreditStatut.REJETE);
        assertThat(result.getEtapeCourante()).isEqualTo("REJETE");
        verify(historiqueRepo).save(argThat(h -> "REFUSE".equals(h.getDecision())));
    }

    // ── 4. Rejet depuis état terminal → 409 ─────────────────────────────────

    @Test
    @DisplayName("rejeter depuis état DEBLOQUE (terminal) → 409 CONFLICT")
    void rejeterDepuisEtatTerminal() {
        creditSaisie.setStatut(CreditStatut.DEBLOQUE);
        creditSaisie.setEtapeCourante("DEBLOQUE");
        when(creditsRepo.findById(1L)).thenReturn(Optional.of(creditSaisie));

        assertThatThrownBy(() -> service.rejeter(1L, null))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("terminal");
    }

    // ── 5. Analyse financière calcule ratio et capacité ─────────────────────

    @Test
    @DisplayName("enregistrerAnalyseFinanciere : ratio = charges/revenus, capacite = revenus-charges")
    void analyseCalculsNumeriques() {
        creditSaisie.setEtapeCourante("ANALYSE_FINANCIERE");
        creditSaisie.setStatut(CreditStatut.SOUMIS);
        when(creditsRepo.findById(1L)).thenReturn(Optional.of(creditSaisie));
        when(analyseRepo.findByCredit_IdCredit(1L)).thenReturn(Optional.empty());
        when(analyseRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(creditsRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(historiqueRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AnalyseFinanciereCreateRequest req = new AnalyseFinanciereCreateRequest(
            new BigDecimal("100000"), // revenus
            new BigDecimal("30000"),  // charges
            null, null, null, null, null
        );
        AnalyseFinanciere saved = service.enregistrerAnalyseFinanciere(1L, req);

        assertThat(saved.getRatioEndettement()).isEqualByComparingTo("0.3000");
        assertThat(saved.getCapaciteRemboursement()).isEqualByComparingTo("70000");
    }

    // ── 6. Crédit introuvable → 404 ──────────────────────────────────────────

    @Test
    @DisplayName("soumettreDossier avec id inexistant → 404 NOT_FOUND")
    void creditIntrouvable() {
        when(creditsRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.soumettreDossier(99L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404");
    }
}

package com.microfina.service;

import com.microfina.entity.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour {@link BudgetService}.
 *
 * <p>Valide :
 * <ol>
 *   <li>Atomicité nominale de chaque flux (persist + merge dans la même transaction).</li>
 *   <li>Rollback : toute violation de règle lève une exception <em>avant</em>
 *       tout appel à {@code EntityManager}.</li>
 *   <li>Cohérence des totaux budgétaires après ajout de lignes et mouvements.</li>
 * </ol>
 *
 * DDL source of truth: P8-001 à P8-003.
 * Spec: cahier §3.1.1 (Module Budget).
 */
@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private BudgetService service;

    private Agence agence;
    private Budget budgetBrouillon;
    private Budget budgetValide;
    private LigneBudget ligneRecette;
    private LigneBudget ligneDepense;

    @BeforeEach
    void setUp() {
        agence = new Agence();
        agence.setCodeAgence("NKC");

        budgetBrouillon = new Budget();
        budgetBrouillon.setId(1L);
        budgetBrouillon.setExerciceFiscal(2024);
        budgetBrouillon.setStatut(StatutBudget.BROUILLON);
        budgetBrouillon.setMontantTotalRecettes(BigDecimal.ZERO);
        budgetBrouillon.setMontantTotalDepenses(BigDecimal.ZERO);
        budgetBrouillon.setAgence(agence);

        budgetValide = new Budget();
        budgetValide.setId(2L);
        budgetValide.setExerciceFiscal(2024);
        budgetValide.setStatut(StatutBudget.VALIDE);
        budgetValide.setMontantTotalRecettes(new BigDecimal("500000.0000"));
        budgetValide.setMontantTotalDepenses(new BigDecimal("300000.0000"));
        budgetValide.setAgence(agence);

        ligneRecette = new LigneBudget();
        ligneRecette.setId(10L);
        ligneRecette.setBudget(budgetValide);
        ligneRecette.setCodeRubrique("7101");
        ligneRecette.setLibelle("Intérêts sur crédits");
        ligneRecette.setTypeLigne(TypeLigneBudget.RECETTE);
        ligneRecette.setMontantPrevu(new BigDecimal("200000.0000"));
        ligneRecette.setMontantRealise(BigDecimal.ZERO);

        ligneDepense = new LigneBudget();
        ligneDepense.setId(11L);
        ligneDepense.setBudget(budgetValide);
        ligneDepense.setCodeRubrique("6101");
        ligneDepense.setLibelle("Salaires");
        ligneDepense.setTypeLigne(TypeLigneBudget.DEPENSE);
        ligneDepense.setMontantPrevu(new BigDecimal("150000.0000"));
        ligneDepense.setMontantRealise(BigDecimal.ZERO);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // CRÉER BUDGET
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("creerBudget()")
    class CreerBudgetTests {

        @Test
        @DisplayName("Nominal : Budget persisté au statut BROUILLON avec totaux à zéro")
        void creerBudget_nominal_statut_brouillon_totaux_zero() {
            Budget b = service.creerBudget(agence, 2025, "admin");

            verify(em).persist(b);
            assertThat(b.getStatut()).isEqualTo(StatutBudget.BROUILLON);
            assertThat(b.getExerciceFiscal()).isEqualTo(2025);
            assertThat(b.getMontantTotalRecettes()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(b.getMontantTotalDepenses()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(b.getDateCreation()).isNotNull();
        }

        @Test
        @DisplayName("Rollback : exercice invalide (1999) → exception, aucune persistance")
        void creerBudget_exerciceInvalide_exception_aucune_persistance() {
            assertThatThrownBy(() -> service.creerBudget(agence, 1999, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Exercice fiscal invalide");

            verifyNoInteractions(em);
        }

        @Test
        @DisplayName("Rollback : exercice futur extrême (2101) → exception")
        void creerBudget_exerciceTropGrand_exception() {
            assertThatThrownBy(() -> service.creerBudget(agence, 2101, "admin"))
                .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(em);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // AJOUTER LIGNE
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ajouterLigne()")
    class AjouterLigneTests {

        @Test
        @DisplayName("Nominal RECETTE : ligne persistée + total recettes mis à jour")
        void ajouterLigne_recette_totalRecettesMisAJour() {
            BigDecimal montant = new BigDecimal("100000.0000");

            LigneBudget ligne = service.ajouterLigne(
                budgetBrouillon, "7201", "Commissions", TypeLigneBudget.RECETTE, montant, "7201"
            );

            // LigneBudget persistée
            verify(em).persist(ligne);
            // Budget mergé
            verify(em).merge(budgetBrouillon);

            assertThat(ligne.getTypeLigne()).isEqualTo(TypeLigneBudget.RECETTE);
            assertThat(ligne.getMontantPrevu()).isEqualByComparingTo(montant);
            assertThat(ligne.getMontantRealise()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(budgetBrouillon.getMontantTotalRecettes()).isEqualByComparingTo(montant);
            assertThat(budgetBrouillon.getMontantTotalDepenses()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Nominal DEPENSE : ligne persistée + total dépenses mis à jour")
        void ajouterLigne_depense_totalDepensesMisAJour() {
            BigDecimal montant = new BigDecimal("60000.0000");

            LigneBudget ligne = service.ajouterLigne(
                budgetBrouillon, "6201", "Loyers", TypeLigneBudget.DEPENSE, montant, null
            );

            verify(em).persist(ligne);
            verify(em).merge(budgetBrouillon);

            assertThat(budgetBrouillon.getMontantTotalDepenses()).isEqualByComparingTo(montant);
            assertThat(budgetBrouillon.getMontantTotalRecettes()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Nominal : deux lignes RECETTE successives → total cumulé")
        void ajouterLigne_deuxRecettes_totalCumule() {
            BigDecimal m1 = new BigDecimal("80000.0000");
            BigDecimal m2 = new BigDecimal("40000.0000");

            service.ajouterLigne(budgetBrouillon, "7101", "Ligne A", TypeLigneBudget.RECETTE, m1, null);
            service.ajouterLigne(budgetBrouillon, "7102", "Ligne B", TypeLigneBudget.RECETTE, m2, null);

            assertThat(budgetBrouillon.getMontantTotalRecettes())
                .isEqualByComparingTo(m1.add(m2));
        }

        @Test
        @DisplayName("Rollback : budget VALIDE → exception, aucune persistance")
        void ajouterLigne_budgetValide_exception_aucune_persistance() {
            assertThatThrownBy(() ->
                service.ajouterLigne(budgetValide, "7101", "Test",
                                     TypeLigneBudget.RECETTE, new BigDecimal("1000"), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BROUILLON");

            verifyNoInteractions(em);
        }

        @Test
        @DisplayName("Rollback : montant négatif → exception, aucune persistance")
        void ajouterLigne_montantNegatif_exception_aucune_persistance() {
            assertThatThrownBy(() ->
                service.ajouterLigne(budgetBrouillon, "7101", "Test",
                                     TypeLigneBudget.RECETTE, new BigDecimal("-100"), null))
                .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(em);
        }

        @Test
        @DisplayName("Nominal : montant zéro accepté (provision sans montant encore connu)")
        void ajouterLigne_montantZero_accepte() {
            assertThatNoException().isThrownBy(() ->
                service.ajouterLigne(budgetBrouillon, "7901", "Provision",
                                     TypeLigneBudget.DEPENSE, BigDecimal.ZERO, null));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // VALIDER BUDGET
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("validerBudget()")
    class ValiderBudgetTests {

        @Test
        @DisplayName("Nominal : BROUILLON → VALIDE, dateValidation renseignée")
        void validerBudget_nominal_statut_valide_date_renseignee() {
            when(em.merge(budgetBrouillon)).thenReturn(budgetBrouillon);

            Budget result = service.validerBudget(budgetBrouillon, "superviseur");

            verify(em).merge(budgetBrouillon);
            assertThat(result.getStatut()).isEqualTo(StatutBudget.VALIDE);
            assertThat(result.getDateValidation()).isNotNull();
        }

        @Test
        @DisplayName("Rollback : budget déjà VALIDE → exception, aucun merge")
        void validerBudget_dejaValide_exception_aucun_merge() {
            assertThatThrownBy(() -> service.validerBudget(budgetValide, "superviseur"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BROUILLON");

            verify(em, never()).merge(any());
        }

        @Test
        @DisplayName("Rollback : budget CLOTURE → exception")
        void validerBudget_cloture_exception() {
            budgetBrouillon.setStatut(StatutBudget.CLOTURE);
            assertThatThrownBy(() -> service.validerBudget(budgetBrouillon, "admin"))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ENREGISTRER MOUVEMENT
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("enregistrerMouvement()")
    class EnregistrerMouvementTests {

        @Test
        @DisplayName("Nominal RECETTE : Comptabilite + MouvementBudget persistés, réalisé et total mis à jour")
        void enregistrerMouvement_recette_atomique_totaux_mis_a_jour() {
            BigDecimal montant = new BigDecimal("45000.0000");
            BigDecimal totalRecettesInitial = budgetValide.getMontantTotalRecettes();

            MouvementBudget mv = service.enregistrerMouvement(
                ligneRecette, montant, "Encaissement intérêts T1", "comptable01"
            );

            // 2 persist : Comptabilite + MouvementBudget
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            verify(em, times(2)).persist(captor.capture());

            assertThat(captor.getAllValues().get(0)).isInstanceOf(Comptabilite.class);
            Comptabilite ecriture = (Comptabilite) captor.getAllValues().get(0);
            assertThat(ecriture.getCredit()).isEqualByComparingTo(montant);  // RECETTE = crédit
            assertThat(ecriture.getDebit()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(ecriture.getReference()).isEqualTo("BUDGET_RECETTE");

            assertThat(captor.getAllValues().get(1)).isInstanceOf(MouvementBudget.class);

            // 2 merge : LigneBudget + Budget
            verify(em, times(2)).merge(any());

            assertThat(ligneRecette.getMontantRealise()).isEqualByComparingTo(montant);
            assertThat(budgetValide.getMontantTotalRecettes())
                .isEqualByComparingTo(totalRecettesInitial.add(montant));

            assertThat(mv.getMontant()).isEqualByComparingTo(montant);
            assertThat(mv.getComptabilite()).isNotNull();
        }

        @Test
        @DisplayName("Nominal DEPENSE : écriture au débit, total dépenses augmenté")
        void enregistrerMouvement_depense_ecriture_debit() {
            BigDecimal montant = new BigDecimal("30000.0000");
            BigDecimal totalDepInitial = budgetValide.getMontantTotalDepenses();

            service.enregistrerMouvement(ligneDepense, montant, "Paiement salaires janv.", "rh01");

            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            verify(em, atLeastOnce()).persist(captor.capture());

            Comptabilite ecriture = captor.getAllValues().stream()
                .filter(o -> o instanceof Comptabilite)
                .map(o -> (Comptabilite) o)
                .findFirst().orElseThrow();

            assertThat(ecriture.getDebit()).isEqualByComparingTo(montant);   // DEPENSE = débit
            assertThat(ecriture.getCredit()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(ecriture.getReference()).isEqualTo("BUDGET_DEPENSE");

            assertThat(budgetValide.getMontantTotalDepenses())
                .isEqualByComparingTo(totalDepInitial.add(montant));
        }

        @Test
        @DisplayName("Rollback : budget BROUILLON → exception, aucune persistance")
        void enregistrerMouvement_budgetBrouillon_exception_aucune_persistance() {
            ligneRecette.setBudget(budgetBrouillon);

            assertThatThrownBy(() ->
                service.enregistrerMouvement(ligneRecette, new BigDecimal("1000"), "Test", "u"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("VALIDE");

            verifyNoInteractions(em);
        }

        @Test
        @DisplayName("Rollback : montant nul → exception, aucune persistance")
        void enregistrerMouvement_montantNul_exception_aucune_persistance() {
            assertThatThrownBy(() ->
                service.enregistrerMouvement(ligneRecette, BigDecimal.ZERO, "Test", "u"))
                .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(em);
        }

        @Test
        @DisplayName("Rollback : budget CLOTURE → exception, aucune persistance")
        void enregistrerMouvement_budgetCloture_exception() {
            budgetValide.setStatut(StatutBudget.CLOTURE);
            ligneRecette.setBudget(budgetValide);

            assertThatThrownBy(() ->
                service.enregistrerMouvement(ligneRecette, new BigDecimal("5000"), "Test", "u"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("VALIDE");

            verifyNoInteractions(em);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // CLÔTURER BUDGET
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cloturerBudget()")
    class CloturerBudgetTests {

        @Test
        @DisplayName("Nominal : VALIDE → CLOTURE")
        void cloturerBudget_valide_vers_cloture() {
            when(em.merge(budgetValide)).thenReturn(budgetValide);

            Budget result = service.cloturerBudget(budgetValide, "admin");

            assertThat(result.getStatut()).isEqualTo(StatutBudget.CLOTURE);
            verify(em).merge(budgetValide);
        }

        @Test
        @DisplayName("Rollback : budget BROUILLON → exception")
        void cloturerBudget_brouillon_exception() {
            assertThatThrownBy(() -> service.cloturerBudget(budgetBrouillon, "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("VALIDE");

            verify(em, never()).merge(any());
        }
    }
}

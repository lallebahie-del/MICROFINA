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
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour {@link OperationBanqueService}.
 *
 * <p>Chaque test valide <strong>deux propriétés fondamentales</strong> :
 * <ol>
 *   <li><b>Atomicité nominale</b> : l'opération ET l'écriture comptable sont persistées
 *       dans le même appel transactionnel.</li>
 *   <li><b>Rollback sur erreur</b> : si une règle métier est violée, aucune persistance
 *       ne doit avoir lieu (simulé par la levée d'une exception avant tout
 *       {@code em.persist()}).</li>
 * </ol>
 *
 * Spec: cahier §3.1.1 (Module Banque).
 * Spec: règle générale §2 — "chaque opération qui touche l'argent doit poster
 * une Comptabilite dans la même transaction".
 */
@ExtendWith(MockitoExtension.class)
class OperationBanqueServiceTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private OperationBanqueService service;

    // ── Fixtures ─────────────────────────────────────────────────────────────

    private CompteBanque comptePrincipal;
    private CompteBanque compteSecondaire;
    private Agence agence;

    @BeforeEach
    void setUp() {
        agence = new Agence();
        agence.setCodeAgence("NKC");
        agence.setNomAgence("Nouakchott");

        comptePrincipal = new CompteBanque();
        comptePrincipal.setId(1L);
        comptePrincipal.setNumeroCompte("MR12345678901234567890");
        comptePrincipal.setSolde(new BigDecimal("100000.0000"));
        comptePrincipal.setDevise("MRU");

        compteSecondaire = new CompteBanque();
        compteSecondaire.setId(2L);
        compteSecondaire.setNumeroCompte("MR98765432109876543210");
        compteSecondaire.setSolde(new BigDecimal("50000.0000"));
        compteSecondaire.setDevise("MRU");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // DÉPÔT
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deposer()")
    class DepositTests {

        @Test
        @DisplayName("Nominal : écriture comptable ET DepotBanque persistés, solde augmenté")
        void deposer_nominal_persiste_ecriture_et_depot_et_augmente_solde() {
            // GIVEN
            BigDecimal montant = new BigDecimal("5000.0000");
            BigDecimal soldeInitial = comptePrincipal.getSolde();

            // WHEN
            DepotBanque depot = service.deposer(
                comptePrincipal, agence, montant, "ESPECES", "Dépôt initial", "caissier01"
            );

            // THEN — deux em.persist() attendus : Comptabilite + DepotBanque
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            verify(em, times(2)).persist(captor.capture());

            Object premierPersist = captor.getAllValues().get(0);
            Object deuxiemePersist = captor.getAllValues().get(1);

            assertThat(premierPersist).isInstanceOf(Comptabilite.class);
            Comptabilite ecriture = (Comptabilite) premierPersist;
            assertThat(ecriture.getDebit()).isEqualByComparingTo(montant);
            assertThat(ecriture.getLibelle()).contains("Dépôt bancaire");

            assertThat(deuxiemePersist).isInstanceOf(DepotBanque.class);

            // em.merge() appelé une fois pour le compte
            verify(em, times(1)).merge(comptePrincipal);

            // Solde mis à jour en mémoire
            assertThat(comptePrincipal.getSolde())
                .isEqualByComparingTo(soldeInitial.add(montant));

            // L'objet retourné est bien validé
            assertThat(depot).isNotNull();
            assertThat(depot.getStatut()).isEqualTo(StatutOperationBanque.VALIDE);
            assertThat(depot.getMontant()).isEqualByComparingTo(montant);
        }

        @Test
        @DisplayName("Rollback : montant nul → aucune persistance")
        void deposer_montantNul_leve_exception_sans_persistance() {
            // WHEN / THEN
            assertThatThrownBy(() ->
                service.deposer(comptePrincipal, agence, BigDecimal.ZERO,
                                "ESPECES", "Test", "user01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strictement positif");

            // Aucun persist ni merge ne doit avoir été appelé
            verifyNoInteractions(em);
        }

        @Test
        @DisplayName("Rollback : montant négatif → aucune persistance")
        void deposer_montantNegatif_leve_exception_sans_persistance() {
            assertThatThrownBy(() ->
                service.deposer(comptePrincipal, agence, new BigDecimal("-100"),
                                "ESPECES", "Test", "user01"))
                .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(em);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // RETRAIT
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("retirer()")
    class WithdrawalTests {

        @Test
        @DisplayName("Nominal : écriture comptable ET RetraitBanque persistés, solde diminué")
        void retirer_nominal_persiste_ecriture_et_retrait_et_diminue_solde() {
            // GIVEN
            BigDecimal montant = new BigDecimal("20000.0000");
            BigDecimal soldeInitial = comptePrincipal.getSolde(); // 100 000

            // WHEN
            RetraitBanque retrait = service.retirer(
                comptePrincipal, agence, montant, "ESPECES", "Retrait caisse", "caissier01"
            );

            // THEN
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            verify(em, times(2)).persist(captor.capture());

            assertThat(captor.getAllValues().get(0)).isInstanceOf(Comptabilite.class);
            assertThat(captor.getAllValues().get(1)).isInstanceOf(RetraitBanque.class);

            verify(em, times(1)).merge(comptePrincipal);
            assertThat(comptePrincipal.getSolde())
                .isEqualByComparingTo(soldeInitial.subtract(montant));

            assertThat(retrait.getStatut()).isEqualTo(StatutOperationBanque.VALIDE);
        }

        @Test
        @DisplayName("Rollback : solde insuffisant → exception IllegalStateException, aucune persistance")
        void retirer_soldeInsuffisant_leve_exception_sans_persistance() {
            // GIVEN — montant > solde disponible
            BigDecimal montantExcessif = new BigDecimal("999999.0000");

            // WHEN / THEN
            assertThatThrownBy(() ->
                service.retirer(comptePrincipal, agence, montantExcessif,
                                "ESPECES", "Test dépassement", "user01"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solde insuffisant");

            // Aucune écriture comptable ni opération ne doit être persistée
            verifyNoInteractions(em);
        }

        @Test
        @DisplayName("Rollback : montant nul → aucune persistance")
        void retirer_montantNul_leve_exception_sans_persistance() {
            assertThatThrownBy(() ->
                service.retirer(comptePrincipal, agence, BigDecimal.ZERO,
                                "ESPECES", "Test", "user01"))
                .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(em);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // VIREMENT INTRA-BANQUE
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("virer() — INTRA_BANQUE")
    class VirementIntraTests {

        @Test
        @DisplayName("Nominal : écriture comptable ET Virement persistés, deux soldes mis à jour")
        void virer_intra_nominal_persiste_ecriture_et_virement_met_a_jour_deux_soldes() {
            // GIVEN
            BigDecimal montant = new BigDecimal("10000.0000");
            BigDecimal soldeSourceInitial = comptePrincipal.getSolde();
            BigDecimal soldeDestInitial   = compteSecondaire.getSolde();

            // WHEN
            Virement virement = service.virer(
                comptePrincipal, compteSecondaire, agence,
                montant, TypeVirement.INTRA_BANQUE,
                "REF-001", "Virement interne test", "comptable01"
            );

            // THEN — Comptabilite + Virement persistés
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            verify(em, times(2)).persist(captor.capture());

            assertThat(captor.getAllValues().get(0)).isInstanceOf(Comptabilite.class);
            Comptabilite ecriture = (Comptabilite) captor.getAllValues().get(0);
            assertThat(ecriture.getDebit()).isEqualByComparingTo(montant);

            assertThat(captor.getAllValues().get(1)).isInstanceOf(Virement.class);

            // Deux merges : compteSource + compteDestination
            verify(em, times(1)).merge(comptePrincipal);
            verify(em, times(1)).merge(compteSecondaire);

            assertThat(comptePrincipal.getSolde())
                .isEqualByComparingTo(soldeSourceInitial.subtract(montant));
            assertThat(compteSecondaire.getSolde())
                .isEqualByComparingTo(soldeDestInitial.add(montant));

            assertThat(virement.getTypeVirement()).isEqualTo(TypeVirement.INTRA_BANQUE);
            assertThat(virement.getReferenceExterne()).isEqualTo("REF-001");
        }

        @Test
        @DisplayName("Rollback : solde insuffisant → aucune persistance")
        void virer_soldeInsuffisant_leve_exception_sans_persistance() {
            BigDecimal montantExcessif = new BigDecimal("9999999.0000");

            assertThatThrownBy(() ->
                service.virer(comptePrincipal, compteSecondaire, agence,
                              montantExcessif, TypeVirement.INTRA_BANQUE,
                              null, "Test", "user01"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solde insuffisant");

            verifyNoInteractions(em);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // VIREMENT INTER-BANQUE
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("virer() — INTER_BANQUE")
    class VirementInterTests {

        @Test
        @DisplayName("Nominal : seul compteSource est débité (compteDestination est externe)")
        void virer_inter_seul_compte_source_est_debite() {
            // GIVEN
            BigDecimal montant = new BigDecimal("30000.0000");
            BigDecimal soldeSourceInitial = comptePrincipal.getSolde();
            BigDecimal soldeDestInitial   = compteSecondaire.getSolde();

            // WHEN
            service.virer(
                comptePrincipal, compteSecondaire, agence,
                montant, TypeVirement.INTER_BANQUE,
                "SWIFT-XYZ", "Virement SWIFT", "superviseur01"
            );

            // THEN — un seul merge sur la source
            verify(em, times(1)).merge(comptePrincipal);
            // compteSecondaire ne doit PAS être mergé (contrepartie externe BCM)
            verify(em, never()).merge(compteSecondaire);

            assertThat(comptePrincipal.getSolde())
                .isEqualByComparingTo(soldeSourceInitial.subtract(montant));
            // solde destination inchangé
            assertThat(compteSecondaire.getSolde())
                .isEqualByComparingTo(soldeDestInitial);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // REMISE DE CHÈQUE
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("encaisserCheque()")
    class RemiseChequeTests {

        private Cheque cheque;

        @BeforeEach
        void setUp() {
            cheque = new Cheque();
            cheque.setId(1L);
            cheque.setNumero("CHQ-001");
            cheque.setMontant(new BigDecimal("7500.0000"));
            cheque.setStatut(StatutCheque.EMIS);
        }

        @Test
        @DisplayName("Nominal : écriture comptable ET RemiseCheque persistés, solde augmenté, chèque ENCAISSE")
        void encaisserCheque_nominal_complet() {
            // GIVEN
            BigDecimal soldeInitial = comptePrincipal.getSolde();

            // WHEN
            RemiseCheque remise = service.encaisserCheque(
                cheque, comptePrincipal, agence,
                "BCM Mauritanie", LocalDate.now(), "caissier01"
            );

            // THEN
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            verify(em, times(2)).persist(captor.capture());
            assertThat(captor.getAllValues().get(0)).isInstanceOf(Comptabilite.class);
            assertThat(captor.getAllValues().get(1)).isInstanceOf(RemiseCheque.class);

            // Chèque et compte mergés
            verify(em).merge(cheque);
            verify(em).merge(comptePrincipal);

            // Statut chèque mis à jour
            assertThat(cheque.getStatut()).isEqualTo(StatutCheque.ENCAISSE);
            assertThat(cheque.getDateEncaissement()).isEqualTo(LocalDate.now());

            // Solde crédité
            assertThat(comptePrincipal.getSolde())
                .isEqualByComparingTo(soldeInitial.add(cheque.getMontant()));

            assertThat(remise.getBanquePresentatrice()).isEqualTo("BCM Mauritanie");
        }

        @Test
        @DisplayName("Rollback : chèque déjà ENCAISSE → exception, aucune persistance")
        void encaisserCheque_dejaEncaisse_leve_exception() {
            cheque.setStatut(StatutCheque.ENCAISSE);

            assertThatThrownBy(() ->
                service.encaisserCheque(cheque, comptePrincipal, agence,
                                        "BCM", LocalDate.now(), "user01"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ne peut pas être encaissé");

            verifyNoInteractions(em);
        }

        @Test
        @DisplayName("Rollback : chèque OPPOSE → exception, aucune persistance")
        void encaisserCheque_oppose_leve_exception() {
            cheque.setStatut(StatutCheque.OPPOSE);

            assertThatThrownBy(() ->
                service.encaisserCheque(cheque, comptePrincipal, agence,
                                        "BCM", LocalDate.now(), "user01"))
                .isInstanceOf(IllegalStateException.class);

            verifyNoInteractions(em);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // CARNET DE CHÈQUE
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("demanderCarnet() / remettreCarnnet()")
    class CarnetChequeTests {

        @Test
        @DisplayName("demanderCarnet() : persiste le carnet au statut DEMANDE")
        void demanderCarnet_persiste_statut_demande() {
            // WHEN
            Membres membre = new Membres();
            membre.setNumMembre("M-0001");

            CarnetCheque carnet = service.demanderCarnet(
                comptePrincipal, membre, "CARNET-2024-001", 25
            );

            // THEN
            verify(em).persist(carnet);
            assertThat(carnet.getStatut()).isEqualTo(StatutCarnetCheque.DEMANDE);
            assertThat(carnet.getNombreCheques()).isEqualTo(25);
            assertThat(carnet.getDateDemande()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("remettreCarnnet() : passe de IMPRIME à REMIS avec dateRemise")
        void remettreCarnnet_imprime_vers_remis() {
            // GIVEN
            CarnetCheque carnet = new CarnetCheque();
            carnet.setStatut(StatutCarnetCheque.IMPRIME);
            carnet.setNumeroCarnet("CARNET-2024-002");
            when(em.merge(carnet)).thenReturn(carnet);

            // WHEN
            CarnetCheque result = service.remettreCarnnet(carnet);

            // THEN
            assertThat(result.getStatut()).isEqualTo(StatutCarnetCheque.REMIS);
            assertThat(result.getDateRemise()).isEqualTo(LocalDate.now());
            verify(em).merge(carnet);
        }

        @Test
        @DisplayName("remettreCarnnet() : carnet non-imprimé → exception, aucune persistance")
        void remettreCarnnet_pasImprime_leve_exception() {
            CarnetCheque carnet = new CarnetCheque();
            carnet.setStatut(StatutCarnetCheque.DEMANDE);
            carnet.setNumeroCarnet("CARNET-2024-003");

            assertThatThrownBy(() -> service.remettreCarnnet(carnet))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("IMPRIME avant remise");

            verify(em, never()).merge(any());
        }
    }
}

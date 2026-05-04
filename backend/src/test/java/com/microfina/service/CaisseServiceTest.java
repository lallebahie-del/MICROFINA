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
 * Tests unitaires pour {@link CaisseService}.
 *
 * <p>Valide deux propriétés fondamentales sur chaque opération :
 * <ol>
 *   <li><b>Atomicité nominale</b> — l'opération caisse ET l'écriture
 *       {@link Comptabilite} sont persistées ensemble.</li>
 *   <li><b>Rollback sur erreur</b> — toute violation de règle métier
 *       (montant nul, solde insuffisant) lève une exception <em>avant</em>
 *       tout appel à {@code em.persist()} ou {@code em.merge()}.</li>
 * </ol>
 *
 * DDL source of truth: P7-001 à P7-004.
 * Spec: cahier §3.1.1.
 */
@ExtendWith(MockitoExtension.class)
class CaisseServiceTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private CaisseService service;

    // ── Fixtures ─────────────────────────────────────────────────────────────

    private CompteEps compte;
    private Agence agence;
    private ValeurFraisMembre valeurFrais;

    @BeforeEach
    void setUp() {
        agence = new Agence();
        agence.setCodeAgence("NKC");

        compte = new CompteEps();
        compte.setNumCompte("EPS-2024-001");
        compte.setMontantDepot(new BigDecimal("80000.0000"));
        compte.setMontantBloque(new BigDecimal("5000.0000"));

        valeurFrais = new ValeurFraisMembre();
        valeurFrais.setIdValeurFraisMembre(1L);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // DÉPÔT
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deposer()")
    class DepotTests {

        @Test
        @DisplayName("Nominal : Comptabilite ET DepotEpargne persistés, solde augmenté")
        void deposer_nominal_atomique_solde_mis_a_jour() {
            // GIVEN
            BigDecimal montant = new BigDecimal("10000.0000");
            BigDecimal soldeInitial = compte.getMontantDepot();

            // WHEN
            DepotEpargne depot = service.deposer(
                compte, agence, montant, ModePaiementCaisse.ESPECES, "Dépôt mensuel", "caissier01"
            );

            // THEN — deux persist attendus dans l'ordre : Comptabilite puis DepotEpargne
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            verify(em, times(2)).persist(captor.capture());

            assertThat(captor.getAllValues().get(0)).isInstanceOf(Comptabilite.class);
            Comptabilite ecriture = (Comptabilite) captor.getAllValues().get(0);
            assertThat(ecriture.getDebit()).isEqualByComparingTo(montant);
            assertThat(ecriture.getLibelle()).contains("Dépôt épargne");
            assertThat(ecriture.getReference()).isEqualTo("DEPOT_EPARGNE");

            assertThat(captor.getAllValues().get(1)).isInstanceOf(DepotEpargne.class);

            // merge du compte
            verify(em, times(1)).merge(compte);
            assertThat(compte.getMontantDepot())
                .isEqualByComparingTo(soldeInitial.add(montant));

            // Contrôle de l'objet retourné
            assertThat(depot.getStatut()).isEqualTo(StatutOperationCaisse.VALIDE);
            assertThat(depot.getMontant()).isEqualByComparingTo(montant);
            assertThat(depot.getNumPiece()).startsWith("PC-DEP-");
        }

        @Test
        @DisplayName("Nominal : compte avec solde nul → dépôt accepté, solde = montant")
        void deposer_soldeNul_accepte() {
            compte.setMontantDepot(BigDecimal.ZERO);
            BigDecimal montant = new BigDecimal("1000.0000");

            service.deposer(compte, agence, montant, ModePaiementCaisse.CHEQUE, null, "u01");

            assertThat(compte.getMontantDepot()).isEqualByComparingTo(montant);
        }

        @Test
        @DisplayName("Rollback : montant nul → exception avant toute persistance")
        void deposer_montantNul_exception_aucune_persistance() {
            assertThatThrownBy(() ->
                service.deposer(compte, agence, BigDecimal.ZERO,
                                ModePaiementCaisse.ESPECES, "Test", "u01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strictement positif");

            verifyNoInteractions(em);
        }

        @Test
        @DisplayName("Rollback : montant négatif → exception avant toute persistance")
        void deposer_montantNegatif_exception_aucune_persistance() {
            assertThatThrownBy(() ->
                service.deposer(compte, agence, new BigDecimal("-500"),
                                ModePaiementCaisse.ESPECES, "Test", "u01"))
                .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(em);
        }

        @Test
        @DisplayName("Nominal : mode MOBILE_MONEY accepté")
        void deposer_modeMobileMoney_accepte() {
            DepotEpargne depot = service.deposer(
                compte, agence, new BigDecimal("2000.0000"),
                ModePaiementCaisse.MOBILE_MONEY, "Dépôt mobile", "u01"
            );
            assertThat(depot.getModePaiement()).isEqualTo(ModePaiementCaisse.MOBILE_MONEY);
        }

        @Test
        @DisplayName("Nominal : mode null → défaut ESPECES")
        void deposer_modeNull_defautEspeces() {
            DepotEpargne depot = service.deposer(
                compte, agence, new BigDecimal("500.0000"), null, null, "u01"
            );
            assertThat(depot.getModePaiement()).isEqualTo(ModePaiementCaisse.ESPECES);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // RETRAIT
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("retirer()")
    class RetraitTests {

        @Test
        @DisplayName("Nominal : Comptabilite ET RetraitEpargne persistés, solde diminué")
        void retirer_nominal_atomique_solde_debite() {
            // GIVEN — disponible = 80 000 - 5 000 = 75 000, on retire 20 000
            BigDecimal montant = new BigDecimal("20000.0000");
            BigDecimal soldeInitial = compte.getMontantDepot();

            // WHEN
            RetraitEpargne retrait = service.retirer(
                compte, agence, montant, ModePaiementCaisse.ESPECES, "Retrait urgent", "caissier02"
            );

            // THEN
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            verify(em, times(2)).persist(captor.capture());

            assertThat(captor.getAllValues().get(0)).isInstanceOf(Comptabilite.class);
            assertThat(captor.getAllValues().get(1)).isInstanceOf(RetraitEpargne.class);

            verify(em, times(1)).merge(compte);
            assertThat(compte.getMontantDepot())
                .isEqualByComparingTo(soldeInitial.subtract(montant));

            assertThat(retrait.getStatut()).isEqualTo(StatutOperationCaisse.VALIDE);
            assertThat(retrait.getNumPiece()).startsWith("PC-RET-");
        }

        @Test
        @DisplayName("Nominal : retrait exact du disponible (disponible = montant) accepté")
        void retirer_exactDisponible_accepte() {
            // disponible = 80 000 - 5 000 = 75 000
            BigDecimal montant = new BigDecimal("75000.0000");

            assertThatNoException().isThrownBy(() ->
                service.retirer(compte, agence, montant,
                                ModePaiementCaisse.ESPECES, "Clôture", "u01"));
        }

        @Test
        @DisplayName("Rollback : solde disponible insuffisant → IllegalStateException, aucune persistance")
        void retirer_soldeInsuffisant_exception_aucune_persistance() {
            // disponible = 75 000, on demande 76 000
            BigDecimal montantExcessif = new BigDecimal("76000.0000");

            assertThatThrownBy(() ->
                service.retirer(compte, agence, montantExcessif,
                                ModePaiementCaisse.ESPECES, "Test", "u01"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solde disponible insuffisant")
                .hasMessageContaining("EPS-2024-001");

            verifyNoInteractions(em);
        }

        @Test
        @DisplayName("Rollback : montant nul → exception avant toute persistance")
        void retirer_montantNul_exception_aucune_persistance() {
            assertThatThrownBy(() ->
                service.retirer(compte, agence, BigDecimal.ZERO,
                                ModePaiementCaisse.ESPECES, "Test", "u01"))
                .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(em);
        }

        @Test
        @DisplayName("Rollback : compte sans montantBloque → solde disponible = montantDepot")
        void retirer_sansMontantBloque_soldeDisponibleEgalDepot() {
            compte.setMontantBloque(null);
            // disponible = 80 000, on demande 80 001 → refuse
            assertThatThrownBy(() ->
                service.retirer(compte, agence, new BigDecimal("80001.0000"),
                                ModePaiementCaisse.ESPECES, null, "u01"))
                .isInstanceOf(IllegalStateException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // FRAIS D'ADHÉSION
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("payerFraisAdhesion()")
    class FraisAdhesionTests {

        @Test
        @DisplayName("Nominal : Comptabilite ET FraisAdhesion persistés, aucun merge de compte")
        void payerFraisAdhesion_nominal_atomique_sans_compte() {
            BigDecimal montant = new BigDecimal("2500.0000");

            FraisAdhesion frais = service.payerFraisAdhesion(
                agence, valeurFrais, montant, ModePaiementCaisse.ESPECES,
                "Adhésion nouveau membre", "caissier01"
            );

            // THEN — deux persist : Comptabilite + FraisAdhesion
            ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
            verify(em, times(2)).persist(captor.capture());

            assertThat(captor.getAllValues().get(0)).isInstanceOf(Comptabilite.class);
            Comptabilite ecriture = (Comptabilite) captor.getAllValues().get(0);
            assertThat(ecriture.getDebit()).isEqualByComparingTo(montant);
            assertThat(ecriture.getReference()).isEqualTo("FRAIS_ADHESION");

            assertThat(captor.getAllValues().get(1)).isInstanceOf(FraisAdhesion.class);

            // Aucun merge : les frais ne touchent pas de compte épargne
            verify(em, never()).merge(any());

            assertThat(frais.getStatut()).isEqualTo(StatutOperationCaisse.VALIDE);
            assertThat(frais.getCompteEps()).isNull();
            assertThat(frais.getValeurFraisMembre()).isEqualTo(valeurFrais);
            assertThat(frais.getNumPiece()).startsWith("PC-ADH-");
        }

        @Test
        @DisplayName("Rollback : montant nul → exception, aucune persistance")
        void payerFraisAdhesion_montantNul_exception_aucune_persistance() {
            assertThatThrownBy(() ->
                service.payerFraisAdhesion(agence, valeurFrais, null,
                                            ModePaiementCaisse.ESPECES, null, "u01"))
                .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(em);
        }

        @Test
        @DisplayName("Nominal : mode null → défaut ESPECES")
        void payerFraisAdhesion_modeNull_defautEspeces() {
            FraisAdhesion frais = service.payerFraisAdhesion(
                agence, valeurFrais, new BigDecimal("1000.0000"),
                null, "Test", "u01"
            );
            assertThat(frais.getModePaiement()).isEqualTo(ModePaiementCaisse.ESPECES);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // NUMÉROTATION DES PIÈCES
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Numérotation pièces")
    class NumeroPieceTests {

        @Test
        @DisplayName("Deux dépôts consécutifs ont des numéros de pièce distincts")
        void deposer_deuxFois_numeroPieceDistincts() {
            BigDecimal m = new BigDecimal("1000.0000");
            DepotEpargne d1 = service.deposer(compte, agence, m, ModePaiementCaisse.ESPECES, null, "u01");
            DepotEpargne d2 = service.deposer(compte, agence, m, ModePaiementCaisse.ESPECES, null, "u01");
            assertThat(d1.getNumPiece()).isNotEqualTo(d2.getNumPiece());
        }
    }
}

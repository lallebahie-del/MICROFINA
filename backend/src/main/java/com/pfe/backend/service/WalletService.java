package com.pfe.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microfina.entity.OperationWallet;
import com.pfe.backend.dto.WalletDto.*;
import com.pfe.backend.repository.OperationWalletRepository;
import com.pfe.backend.wallet.BankilyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * WalletService — orchestration des transactions mobile money Bankily.
 *
 * <h2>Responsabilités</h2>
 * <ol>
 *   <li>Générer une référence MFI unique ({@code MFI-yyyyMMdd-UUID8}).</li>
 *   <li>Valider les pré-conditions métier (crédit existant, statut cohérent).</li>
 *   <li>Appeler le {@link BankilyClient} et persister l'opération.</li>
 *   <li>Traiter les callbacks entrants de Bankily (mise à jour statut).</li>
 *   <li>Exposer les méthodes de lecture (historique, recherche, rafraîchissement).</li>
 * </ol>
 *
 * <h2>Mapping statut Bankily → statut MFI</h2>
 * <pre>
 *   SUCCESS   → CONFIRME
 *   PENDING   → EN_ATTENTE (pas de changement)
 *   FAILED    → REJETE
 *   EXPIRED   → EXPIRE
 *   CANCELLED → ANNULE
 * </pre>
 */
@Service
@Transactional
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private static final DateTimeFormatter REF_DATE_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OperationWalletRepository walletRepo;
    private final BankilyClient             bankilyClient;
    private final JdbcTemplate              jdbc;
    private final ObjectMapper              objectMapper;

    public WalletService(OperationWalletRepository walletRepo,
                         BankilyClient bankilyClient,
                         JdbcTemplate jdbc,
                         ObjectMapper objectMapper) {
        this.walletRepo    = walletRepo;
        this.bankilyClient = bankilyClient;
        this.jdbc          = jdbc;
        this.objectMapper  = objectMapper;
    }

    // =========================================================================
    //  Déblocage via wallet
    // =========================================================================

    /**
     * Initie le déblocage d'un crédit vers le wallet Bankily du membre.
     *
     * <p>Pré-conditions :</p>
     * <ul>
     *   <li>Le crédit doit exister et avoir le statut {@code VALIDE} (prêt à décaisser).</li>
     *   <li>Aucun déblocage wallet EN_ATTENTE ou CONFIRME pour ce crédit.</li>
     * </ul>
     *
     * @param req requête contenant l'ID du crédit et le numéro de téléphone
     * @return opération persistée
     * @throws IllegalArgumentException si le crédit n'existe pas ou n'est pas débloquable
     * @throws IllegalStateException    si un déblocage wallet est déjà en cours
     */
    public OperationResponse initierDeblocage(DeblocageRequest req) {
        // Vérifier que le crédit existe et a le bon statut
        Map<String, Object> credit = chargerCredit(req.idCredit());
        String statutCredit = (String) credit.get("STATUT");
        if (!"VALIDE".equalsIgnoreCase(statutCredit) && !"EN_COURS".equalsIgnoreCase(statutCredit)) {
            throw new IllegalArgumentException(
                    "Le crédit " + req.idCredit() + " n'est pas en statut VALIDE (statut actuel : " + statutCredit + ")");
        }

        // Vérifier l'absence d'un déblocage déjà actif pour ce crédit
        List<OperationWallet> existants = walletRepo.findByIdCredit(req.idCredit());
        boolean dejaActif = existants.stream()
                .filter(o -> OperationWallet.TYPE_DEBLOCAGE.equals(o.getTypeOperation()))
                .anyMatch(o -> OperationWallet.STATUT_EN_ATTENTE.equals(o.getStatut())
                            || OperationWallet.STATUT_CONFIRME.equals(o.getStatut()));
        if (dejaActif) {
            throw new IllegalStateException(
                    "Un déblocage wallet est déjà actif pour le crédit " + req.idCredit());
        }

        BigDecimal montant = toBigDecimal(credit.get("MONTANT_DEBLOQUER"));
        String     motif   = req.motif() != null ? req.motif()
                : "Déblocage crédit " + credit.get("NUMCREDIT");

        String ref = genererReference();

        // Appeler Bankily
        BankilyInitResponse bankilyResp = bankilyClient.initierPaiement(
                req.numeroTelephone(), montant, ref, motif);

        // Persister l'opération
        OperationWallet op = new OperationWallet();
        op.setReferenceMfi(ref);
        op.setReferenceBankily(bankilyResp.referenceBankily());
        op.setNumeroTelephone(req.numeroTelephone());
        op.setMontant(montant);
        op.setTypeOperation(OperationWallet.TYPE_DEBLOCAGE);
        op.setStatut(bankilyResp.succes() ? OperationWallet.STATUT_EN_ATTENTE
                                           : OperationWallet.STATUT_REJETE);
        op.setDateOperation(LocalDate.now());
        op.setMotif(motif);
        op.setCodeRetour(bankilyResp.codeRetour());
        op.setMessageRetour(bankilyResp.message());
        op.setIdCredit(req.idCredit());
        op.setNumMembre((String) credit.get("nummembre"));
        op.setCodeAgence((String) credit.get("agence"));
        op.setUtilisateur(utilisateurCourant());

        if (!bankilyResp.succes()) {
            log.warn("[Wallet] Déblocage rejeté par Bankily ref={} code={}", ref, bankilyResp.codeRetour());
        }

        return toResponse(walletRepo.save(op));
    }

    // =========================================================================
    //  Remboursement via wallet
    // =========================================================================

    /**
     * Initie la collecte d'un remboursement depuis le wallet Bankily du membre.
     *
     * <p>Pré-conditions :</p>
     * <ul>
     *   <li>Le crédit doit exister et avoir le statut {@code DEBLOQUE}.</li>
     *   <li>Le montant doit être positif et ≤ solde capital restant.</li>
     * </ul>
     *
     * @param req requête contenant l'ID du crédit, le montant et le numéro de téléphone
     * @return opération persistée
     */
    public OperationResponse initierRemboursement(RemboursementRequest req) {
        Map<String, Object> credit = chargerCredit(req.idCredit());
        String statutCredit = (String) credit.get("STATUT");
        if (!"DEBLOQUE".equalsIgnoreCase(statutCredit)) {
            throw new IllegalArgumentException(
                    "Le crédit " + req.idCredit() + " n'est pas débloqué (statut : " + statutCredit + ")");
        }

        BigDecimal solde = toBigDecimal(credit.get("SOLDE_CAPITAL"));
        if (req.montant().compareTo(solde) > 0) {
            throw new IllegalArgumentException(
                    "Le montant " + req.montant() + " dépasse le solde capital restant " + solde);
        }

        String motif = req.motif() != null ? req.motif()
                : "Remboursement crédit " + credit.get("NUMCREDIT");
        String ref   = genererReference();

        BankilyInitResponse bankilyResp = bankilyClient.initierCollecte(
                req.numeroTelephone(), req.montant(), ref, motif);

        OperationWallet op = new OperationWallet();
        op.setReferenceMfi(ref);
        op.setReferenceBankily(bankilyResp.referenceBankily());
        op.setNumeroTelephone(req.numeroTelephone());
        op.setMontant(req.montant());
        op.setTypeOperation(OperationWallet.TYPE_REMBOURSEMENT);
        op.setStatut(bankilyResp.succes() ? OperationWallet.STATUT_EN_ATTENTE
                                           : OperationWallet.STATUT_REJETE);
        op.setDateOperation(LocalDate.now());
        op.setMotif(motif);
        op.setCodeRetour(bankilyResp.codeRetour());
        op.setMessageRetour(bankilyResp.message());
        op.setIdCredit(req.idCredit());
        op.setNumMembre((String) credit.get("nummembre"));
        op.setCodeAgence((String) credit.get("agence"));
        op.setUtilisateur(utilisateurCourant());

        return toResponse(walletRepo.save(op));
    }

    // =========================================================================
    //  Traitement du callback Bankily
    // =========================================================================

    /**
     * Traite la notification de fin de transaction reçue de Bankily (webhook).
     *
     * <p>La vérification de signature HMAC doit être effectuée <em>avant</em>
     * d'appeler cette méthode (responsabilité du contrôleur).</p>
     *
     * @param req payload du callback Bankily
     * @return opération mise à jour
     * @throws IllegalArgumentException si la transaction n'est pas trouvée
     * @throws IllegalStateException    si la transaction est déjà dans un état terminal
     */
    public OperationResponse traiterCallback(CallbackRequest req) {
        OperationWallet op = walletRepo.findByReferenceBankily(req.referenceBankily())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucune opération trouvée pour la référence Bankily : " + req.referenceBankily()));

        if (op.isTerminal()) {
            log.warn("[Wallet] Callback ignoré — opération déjà terminale ref={} statut={}",
                     op.getReferenceMfi(), op.getStatut());
            return toResponse(op);
        }

        // Mapper statut Bankily → statut MFI
        String nouveauStatut = mapperStatutBankily(req.statut());
        op.setStatut(nouveauStatut);
        op.setCodeRetour(req.codeRetour());
        op.setMessageRetour(req.message());
        if (!OperationWallet.STATUT_EN_ATTENTE.equals(nouveauStatut)) {
            op.setDateConfirmation(LocalDateTime.now());
        }

        // Conserver le payload brut pour audit
        try {
            op.setPayloadCallback(objectMapper.writeValueAsString(req));
        } catch (Exception e) {
            op.setPayloadCallback(req.toString());
        }

        log.info("[Wallet] Callback traité ref={} statut Bankily={} → statut MFI={}",
                 op.getReferenceMfi(), req.statut(), nouveauStatut);

        return toResponse(walletRepo.save(op));
    }

    // =========================================================================
    //  Rafraîchissement du statut (polling Bankily)
    // =========================================================================

    /**
     * Consulte le statut temps-réel de la transaction auprès de Bankily et
     * met à jour la base si le statut a changé.
     *
     * @param id identifiant local de l'opération
     * @return réponse de rafraîchissement
     */
    public StatutResponse rafraichirStatut(Long id) {
        OperationWallet op = walletRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Opération introuvable : " + id));

        String statutLocal = op.getStatut();

        if (op.isTerminal()) {
            return new StatutResponse(op.getReferenceMfi(), op.getReferenceBankily(),
                    statutLocal, null, false, "Opération déjà terminée");
        }

        if (op.getReferenceBankily() == null) {
            return new StatutResponse(op.getReferenceMfi(), null, statutLocal, null, false,
                    "Pas de référence Bankily — initiation probablement échouée");
        }

        BankilyStatutResponse bankilyResp = bankilyClient.consulterTransaction(op.getReferenceBankily());
        String nouveauStatutMfi = mapperStatutBankily(bankilyResp.statut());

        boolean changement = !nouveauStatutMfi.equals(statutLocal)
                          && !OperationWallet.STATUT_EN_ATTENTE.equals(nouveauStatutMfi);

        if (changement) {
            op.setStatut(nouveauStatutMfi);
            op.setCodeRetour(bankilyResp.codeRetour());
            op.setMessageRetour(bankilyResp.message());
            op.setDateConfirmation(LocalDateTime.now());
            walletRepo.save(op);
            log.info("[Wallet] Statut rafraîchi ref={} : {} → {}", op.getReferenceMfi(), statutLocal, nouveauStatutMfi);
        }

        return new StatutResponse(op.getReferenceMfi(), op.getReferenceBankily(),
                statutLocal, bankilyResp.statut(), changement, bankilyResp.message());
    }

    // =========================================================================
    //  Annulation
    // =========================================================================

    /**
     * Annule une opération EN_ATTENTE (avant confirmation Bankily).
     */
    public OperationResponse annuler(Long id) {
        OperationWallet op = walletRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Opération introuvable : " + id));

        if (op.isTerminal()) {
            throw new IllegalStateException(
                    "Impossible d'annuler une opération en statut " + op.getStatut());
        }
        op.setStatut(OperationWallet.STATUT_ANNULE);
        op.setDateConfirmation(LocalDateTime.now());
        op.setMessageRetour("Annulation manuelle");
        return toResponse(walletRepo.save(op));
    }

    // =========================================================================
    //  Lecture
    // =========================================================================

    @Transactional(readOnly = true)
    public OperationResponse consulter(Long id) {
        return walletRepo.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Opération introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<OperationResponse> historiqueMembre(String numMembre) {
        return walletRepo.findByNumMembre(numMembre).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OperationResponse> historiqueCredit(Long idCredit) {
        return walletRepo.findByIdCredit(idCredit).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OperationResponse> rechercher(String codeAgence, String typeOperation, String statut) {
        return walletRepo.rechercher(codeAgence, typeOperation, statut)
                         .stream().map(this::toResponse).toList();
    }

    // =========================================================================
    //  Réconciliation Bankily
    // =========================================================================

    /**
     * Réconcilie les opérations EN_ATTENTE avec le statut réel Bankily.
     *
     * <p>Pour chaque opération EN_ATTENTE de l'agence spécifiée (ou toutes les agences
     * si {@code codeAgence} est null), consulte l'API Bankily et met à jour le statut
     * si celui-ci a changé.</p>
     *
     * @param codeAgence filtre optionnel sur le code agence
     * @return résumé de la réconciliation
     */
    @Transactional
    public ReconciliationResponse reconcilier(String codeAgence) {
        List<OperationWallet> enAttente =
                walletRepo.rechercher(codeAgence, null, OperationWallet.STATUT_EN_ATTENTE);

        int verifie    = 0;
        int misAJour   = 0;
        List<String> erreurs = new java.util.ArrayList<>();

        for (OperationWallet op : enAttente) {
            verifie++;
            if (op.getReferenceBankily() == null) continue;
            try {
                BankilyStatutResponse resp = bankilyClient.consulterTransaction(op.getReferenceBankily());
                String nouveauStatut = mapperStatutBankily(resp.statut());
                if (!nouveauStatut.equals(op.getStatut())
                        && !OperationWallet.STATUT_EN_ATTENTE.equals(nouveauStatut)) {
                    op.setStatut(nouveauStatut);
                    op.setCodeRetour(resp.codeRetour());
                    op.setMessageRetour(resp.message());
                    op.setDateConfirmation(LocalDateTime.now());
                    walletRepo.save(op);
                    misAJour++;
                    log.info("[Reconciliation] ref={} → {}", op.getReferenceMfi(), nouveauStatut);
                }
            } catch (Exception e) {
                log.error("[Reconciliation] Erreur pour ref={} : {}", op.getReferenceMfi(), e.getMessage());
                erreurs.add(op.getReferenceMfi());
            }
        }

        String agenceLabel = codeAgence != null ? codeAgence : "TOUTES";
        log.info("[Reconciliation] agence={} vérifié={} mis-à-jour={} erreurs={}",
                 agenceLabel, verifie, misAJour, erreurs.size());

        return new ReconciliationResponse(agenceLabel, verifie, misAJour, erreurs);
    }

    // =========================================================================
    //  Méthodes privées
    // =========================================================================

    /** Génère une référence MFI unique : {@code MFI-yyyyMMdd-XXXXXXXX}. */
    private static String genererReference() {
        String dateStr = LocalDate.now().format(REF_DATE_FMT);
        String uuid8   = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "MFI-" + dateStr + "-" + uuid8;
    }

    /** Charge les colonnes nécessaires d'un crédit. Lance exception si introuvable. */
    private Map<String, Object> chargerCredit(Long idCredit) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT IDCREDIT, NUMCREDIT, STATUT, MONTANT_DEBLOQUER, SOLDE_CAPITAL,
                       nummembre, agence
                FROM Credits
                WHERE IDCREDIT = ?
                """, idCredit);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Crédit introuvable : " + idCredit);
        }
        return rows.get(0);
    }

    /**
     * Mappe un statut Bankily vers un statut MFI.
     * Les statuts non reconnus restent EN_ATTENTE par précaution.
     */
    private static String mapperStatutBankily(String statutBankily) {
        if (statutBankily == null) return OperationWallet.STATUT_EN_ATTENTE;
        return switch (statutBankily.toUpperCase()) {
            case "SUCCESS"   -> OperationWallet.STATUT_CONFIRME;
            case "FAILED",
                 "INSUFFICIENT_FUNDS",
                 "INVALID_MSISDN" -> OperationWallet.STATUT_REJETE;
            case "EXPIRED",
                 "TIMEOUT"   -> OperationWallet.STATUT_EXPIRE;
            case "CANCELLED",
                 "CANCELED"  -> OperationWallet.STATUT_ANNULE;
            default          -> OperationWallet.STATUT_EN_ATTENTE;
        };
    }

    private static BigDecimal toBigDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(v.toString());
    }

    private static String utilisateurCourant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    private OperationResponse toResponse(OperationWallet op) {
        return new OperationResponse(
                op.getId(),
                op.getReferenceMfi(),
                op.getReferenceBankily(),
                op.getNumeroTelephone(),
                op.getMontant(),
                op.getTypeOperation(),
                op.getStatut(),
                op.getDateOperation(),
                op.getDateConfirmation(),
                op.getMotif(),
                op.getCodeRetour(),
                op.getMessageRetour(),
                op.getNumMembre(),
                op.getIdCredit(),
                op.getCodeAgence(),
                op.getUtilisateur()
        );
    }
}

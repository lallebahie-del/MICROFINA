package com.pfe.backend.service;

import com.microfina.entity.CompteEps;
import com.microfina.entity.Epargne;
import com.microfina.entity.Notification;
import com.microfina.entity.Utilisateur;
import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.CompteEpsRepository;
import com.pfe.backend.repository.EpargneRepository;
import com.pfe.backend.repository.NotificationRepository;
import com.pfe.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * MobileMeService — logique des endpoints self-service mobile.
 *
 * <p>Centralise :
 *   <ul>
 *     <li>la résolution de l'utilisateur courant à partir du JWT,</li>
 *     <li>l'historique des mouvements épargne par compte,</li>
 *     <li>les virements internes (compte → compte),</li>
 *     <li>les paiements de services (facturier).</li>
 *   </ul>
 * </p>
 *
 * <p>Toutes les écritures vont dans la table EPARGNE comme journal
 * de mouvements et mettent à jour {@code CompteEps.montantDepot}.</p>
 */
@Service
@Transactional(readOnly = true)
public class MobileMeService {

    private final UtilisateurRepository  utilisateurRepo;
    private final CompteEpsRepository    compteEpsRepo;
    private final EpargneRepository      epargneRepo;
    private final NotificationRepository notificationRepo;
    private final MobileAccountProvisioningService provisioningService;

    @Value("${app.photos.dir:./photos}")
    private String photosDir;

    private static final long MAX_PHOTO_SIZE = 2L * 1024 * 1024;
    private static final Set<String> ALLOWED_PHOTO_TYPES =
        Set.of("image/jpeg", "image/png");
    private static final String NOTIF_TYPE_OPERATION = "OPERATION";
    private static final List<String> MOBILE_OPERATION_CODES = List.of(
        "VIREMENT_DEBIT", "VIREMENT_CREDIT", "PAIEMENT_SERVICE"
    );

    public MobileMeService(UtilisateurRepository utilisateurRepo,
                           CompteEpsRepository compteEpsRepo,
                           EpargneRepository epargneRepo,
                           NotificationRepository notificationRepo,
                           MobileAccountProvisioningService provisioningService) {
        this.utilisateurRepo  = utilisateurRepo;
        this.compteEpsRepo    = compteEpsRepo;
        this.epargneRepo      = epargneRepo;
        this.notificationRepo = notificationRepo;
        this.provisioningService = provisioningService;
    }

    public Utilisateur currentUser(String login) {
        return utilisateurRepo.findByLogin(login)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", login));
    }

    // ── Notifications ───────────────────────────────────────────────────────

    public Map<String, Object> getNotifications(String login, int page, int size) {
        // Priorité aux mouvements EPARGNE (virements / paiements mobile) — toujours synchronisés.
        Map<String, Object> fromEpargne = getNotificationsFromEpargne(login, page, size);
        long epargneTotal = ((Number) fromEpargne.get("totalElements")).longValue();
        if (epargneTotal > 0) {
            return fromEpargne;
        }

        List<String> loginAliases = loginAliases(login);
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        Page<Notification> result = notificationRepo.findByUserLoginIn(loginAliases, pageable);

        if (result.getTotalElements() > 0) {
            List<Map<String, Object>> items = result.getContent().stream()
                .map(MobileMeService::toNotificationDto)
                .toList();
            long unread = notificationRepo.countByUserLoginInAndLuFalse(loginAliases);
            return notificationPageBody(items, result, unread);
        }

        return fromEpargne;
    }

    private Map<String, Object> getNotificationsFromEpargne(String login, int page, int size) {
        Optional<Utilisateur> userOpt = resolveUserByLoginAliases(login);
        if (userOpt.isEmpty()) {
            return emptyNotificationPage(page, size);
        }

        Utilisateur user = provisioningService.provisionIfMissing(userOpt.get());
        List<String> comptes = resolveCompteNumbers(user);
        if (comptes.isEmpty()) {
            return emptyNotificationPage(page, size);
        }

        Pageable pageable = PageRequest.of(
            page, size,
            Sort.by("dateOperation").descending().and(Sort.by("idEpargne").descending()));
        Page<Epargne> epargnes = epargneRepo.findByNumCompteIn(comptes, pageable);

        List<Map<String, Object>> items = epargnes.getContent().stream()
            .filter(MobileMeService::isMobileMovement)
            .map(MobileMeService::epargneToNotificationDto)
            .toList();

        long unread = items.stream().filter(m -> !Boolean.TRUE.equals(m.get("lu"))).count();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content",       items);
        body.put("totalElements", epargnes.getTotalElements());
        body.put("totalPages",    epargnes.getTotalPages());
        body.put("page",          epargnes.getNumber());
        body.put("size",          epargnes.getSize());
        body.put("unread",        unread);
        return body;
    }

    private static Map<String, Object> notificationPageBody(
            List<Map<String, Object>> items, Page<Notification> result, long unread) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content",       items);
        body.put("totalElements", result.getTotalElements());
        body.put("totalPages",    result.getTotalPages());
        body.put("page",          result.getNumber());
        body.put("size",          result.getSize());
        body.put("unread",        unread);
        return body;
    }

    private static Map<String, Object> emptyNotificationPage(int page, int size) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content",       List.of());
        body.put("totalElements", 0L);
        body.put("totalPages",    0);
        body.put("page",          page);
        body.put("size",          size);
        body.put("unread",        0L);
        return body;
    }

    private static boolean isMobileMovement(Epargne e) {
        if (e == null) {
            return false;
        }
        String code = e.getCodeTypeOperation();
        if (code != null && !code.isBlank()) {
            String u = code.toUpperCase(Locale.ROOT);
            if (MOBILE_OPERATION_CODES.stream().anyMatch(u::contains)) {
                return true;
            }
            if (u.contains("VIREMENT") || u.contains("PAIEMENT") || u.contains("RETRAIT")
                || u.contains("DEPOT")) {
                return true;
            }
        }
        boolean hasDebit = e.getMontantDebit() != null && e.getMontantDebit().signum() > 0;
        boolean hasCredit = e.getMontantCredit() != null && e.getMontantCredit().signum() > 0;
        return hasDebit || hasCredit;
    }

    private static Map<String, Object> epargneToNotificationDto(Epargne e) {
        boolean credit = e.getMontantCredit() != null && e.getMontantCredit().signum() > 0;
        BigDecimal montant = credit
            ? e.getMontantCredit()
            : (e.getMontantDebit() != null ? e.getMontantDebit() : BigDecimal.ZERO);

        Map<String, Object> m = new HashMap<>();
        m.put("id",           e.getIdEpargne());
        m.put("titre",        credit ? "Crédit sur compte" : "Débit sur compte");
        m.put("message",      (e.getLibelleOperation() != null ? e.getLibelleOperation() : "")
            + " · " + formatMontant(montant));
        m.put("type",         NOTIF_TYPE_OPERATION);
        m.put("lu",           false);
        LocalDateTime created = e.getDateOperation() != null
            ? e.getDateOperation().atStartOfDay()
            : null;
        m.put("dateCreation", created != null ? created.toString() : null);
        m.put("dateLecture",  null);
        m.put("lien",         "epargne:" + e.getIdEpargne());
        return m;
    }

    private List<String> resolveCompteNumbers(Utilisateur user) {
        List<String> nums = new ArrayList<>();
        if (user.getNumCompteCourant() != null && !user.getNumCompteCourant().isBlank()) {
            nums.add(user.getNumCompteCourant().trim());
        }
        if (user.getNumMembre() != null && !user.getNumMembre().isBlank()) {
            compteEpsRepo.findByMembre_NumMembre(user.getNumMembre()).stream()
                .map(CompteEps::getNumCompte)
                .filter(n -> n != null && !n.isBlank())
                .forEach(nums::add);
        }
        return nums.stream().distinct().toList();
    }

    private Optional<Utilisateur> resolveUserByLoginAliases(String login) {
        for (String alias : loginAliases(login)) {
            Optional<Utilisateur> u = utilisateurRepo.findByLogin(alias);
            if (u.isPresent()) {
                return u;
            }
        }
        return Optional.empty();
    }

    private static List<String> loginAliases(String login) {
        if (login == null || login.isBlank()) {
            return List.of();
        }
        String trimmed = login.trim();
        String digits = normalizeLogin(trimmed);
        List<String> aliases = new ArrayList<>();
        aliases.add(trimmed);
        if (!digits.isBlank() && !digits.equals(trimmed)) {
            aliases.add(digits);
        }
        return aliases.stream().distinct().toList();
    }

    private static String normalizeLogin(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("\\D", "");
    }

    @Transactional
    public Notification markAsRead(String login, Long id) {
        Notification n = notificationRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        if (!loginAliases(login).contains(n.getUserLogin())) {
            throw new BusinessException("ACCES_REFUSE", "Cette notification ne vous appartient pas.");
        }
        if (Boolean.FALSE.equals(n.getLu())) {
            n.setLu(true);
            n.setDateLecture(LocalDateTime.now());
            notificationRepo.save(n);
        }
        return n;
    }

    @Transactional
    public int markAllAsRead(String login) {
        Page<Notification> page = notificationRepo.findByUserLoginIn(
            loginAliases(login), PageRequest.of(0, 500, Sort.by("dateCreation").descending()));
        int updated = 0;
        LocalDateTime now = LocalDateTime.now();
        for (Notification n : page.getContent()) {
            if (Boolean.FALSE.equals(n.getLu())) {
                n.setLu(true);
                n.setDateLecture(now);
                notificationRepo.save(n);
                updated++;
            }
        }
        return updated;
    }

    /**
     * Crée une notification in-app pour l'utilisateur mobile (écran Notifications).
     */
    private void notifyOperation(String userLogin, String titre, String message, Long epargneId) {
        String login = resolveUserByLoginAliases(userLogin)
            .map(Utilisateur::getLogin)
            .orElse(userLogin != null ? userLogin.trim() : "");
        if (login.isBlank()) {
            return;
        }
        Notification n = new Notification(login, titre, message, NOTIF_TYPE_OPERATION);
        if (epargneId != null) {
            n.setLien("epargne:" + epargneId);
        }
        notificationRepo.save(n);
    }

    private static String formatMontant(BigDecimal montant) {
        if (montant == null) {
            return "0 FCFA";
        }
        NumberFormat fmt = NumberFormat.getInstance(Locale.FRANCE);
        fmt.setMaximumFractionDigits(0);
        fmt.setMinimumFractionDigits(0);
        return fmt.format(montant) + " FCFA";
    }

    /** Résout le login mobile (téléphone) associé à un compte épargne. */
    private Optional<String> resolveUserLoginForCompte(CompteEps compte) {
        if (compte == null || compte.getNumCompte() == null) {
            return Optional.empty();
        }
        Optional<Utilisateur> byCompte = utilisateurRepo.findByNumCompteCourant(compte.getNumCompte());
        if (byCompte.isPresent()) {
            return Optional.of(byCompte.get().getLogin());
        }
        if (compte.getMembre() != null && compte.getMembre().getNumMembre() != null) {
            return utilisateurRepo.findByNumMembre(compte.getMembre().getNumMembre())
                .map(Utilisateur::getLogin);
        }
        return Optional.empty();
    }

    private static Map<String, Object> toNotificationDto(Notification n) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",            n.getId());
        m.put("titre",         n.getTitre());
        m.put("message",       n.getMessage() != null ? n.getMessage() : "");
        m.put("type",          n.getType());
        m.put("lu",            Boolean.TRUE.equals(n.getLu()));
        m.put("dateCreation",  n.getDateCreation() != null ? n.getDateCreation().toString() : null);
        m.put("dateLecture",   n.getDateLecture()  != null ? n.getDateLecture().toString()  : null);
        m.put("lien",          n.getLien());
        return m;
    }

    // ── Upload photo de profil ──────────────────────────────────────────────

    public Map<String, Object> uploadProfilePhoto(String login, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("FICHIER_REQUIS", "Aucun fichier fourni.");
        }
        String type = file.getContentType();
        if (type == null || !ALLOWED_PHOTO_TYPES.contains(type.toLowerCase())) {
            throw new BusinessException("TYPE_INVALIDE",
                "Type de fichier non autorisé. Accepté : image/jpeg, image/png.");
        }
        if (file.getSize() > MAX_PHOTO_SIZE) {
            throw new BusinessException("FICHIER_TROP_GROS", "Photo trop volumineuse (max 2 Mo).");
        }

        try {
            Path dir = Paths.get(photosDir, "users").toAbsolutePath();
            Files.createDirectories(dir);
            String ext = type.equalsIgnoreCase("image/png") ? "png" : "jpg";
            Path target = dir.resolve(safeFileName(login) + "." + ext);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("statut",   "OK");
            body.put("login",    login);
            body.put("path",     target.toString());
            body.put("size",     file.getSize());
            body.put("contentType", type);
            return body;
        } catch (IOException e) {
            throw new BusinessException("UPLOAD_ECHOUE", "Échec d'écriture : " + e.getMessage());
        }
    }

    private static String safeFileName(String s) {
        return s.replaceAll("[^a-zA-Z0-9_.\\-]", "_");
    }

    /** Solde disponible d'un compte = montantOuvert + montantDepot. */
    private BigDecimal soldeDisponible(CompteEps c) {
        BigDecimal o = c.getMontantOuvert() != null ? c.getMontantOuvert() : BigDecimal.ZERO;
        BigDecimal d = c.getMontantDepot()  != null ? c.getMontantDepot()  : BigDecimal.ZERO;
        return o.add(d);
    }

    // ── Historique transactions ─────────────────────────────────────────────

    public Map<String, Object> getTransactions(String numCompte, int page, int size) {
        if (!compteEpsRepo.existsById(numCompte)) {
            throw new ResourceNotFoundException("CompteEps", numCompte);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("dateOperation").descending());
        Page<Epargne> result = epargneRepo.findByCompteEps_NumCompte(numCompte, pageable);

        List<Map<String, Object>> items = result.getContent().stream()
            .map(MobileMeService::toTransactionDto)
            .toList();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content",       items);
        body.put("totalElements", result.getTotalElements());
        body.put("totalPages",    result.getTotalPages());
        body.put("page",          result.getNumber());
        body.put("size",          result.getSize());
        return body;
    }

    private static Map<String, Object> toTransactionDto(Epargne e) {
        boolean credit = e.getMontantCredit() != null && e.getMontantCredit().signum() > 0;
        BigDecimal montant = credit
            ? e.getMontantCredit()
            : (e.getMontantDebit() != null ? e.getMontantDebit() : BigDecimal.ZERO);

        Map<String, Object> m = new HashMap<>();
        m.put("id",        e.getIdEpargne());
        m.put("accountId", e.getCompteEps() != null ? e.getCompteEps().getNumCompte() : null);
        m.put("date",      e.getDateOperation() != null ? e.getDateOperation().toString() : null);
        m.put("dateValeur", e.getDateValeur() != null ? e.getDateValeur().toString() : null);
        m.put("type",      credit ? "CREDIT" : "DEBIT");
        m.put("montant",   montant);
        m.put("libelle",   e.getLibelleOperation() != null ? e.getLibelleOperation() : "");
        m.put("numPiece",  e.getNumPiece());
        m.put("soldeApres", e.getSoldeNouveau());
        return m;
    }

    // ── Virement interne ────────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> transfer(String login,
                                         String numCompteSource,
                                         String numCompteDest,
                                         BigDecimal montant,
                                         String libelle) {
        if (montant == null || montant.signum() <= 0) {
            throw new BusinessException("MONTANT_INVALIDE", "Le montant doit être strictement positif.");
        }
        if (numCompteSource == null || numCompteDest == null) {
            throw new BusinessException("COMPTE_REQUIS", "Comptes source et destinataire requis.");
        }
        if (numCompteSource.equals(numCompteDest)) {
            throw new BusinessException("COMPTE_IDENTIQUE", "Source et destination identiques.");
        }

        CompteEps source = compteEpsRepo.findById(numCompteSource)
            .orElseThrow(() -> new ResourceNotFoundException("CompteEps", numCompteSource));
        CompteEps dest = compteEpsRepo.findById(numCompteDest)
            .orElseThrow(() -> new ResourceNotFoundException("CompteEps", numCompteDest));

        if ("O".equalsIgnoreCase(source.getBloque()) || "O".equalsIgnoreCase(source.getFerme())) {
            throw new BusinessException("COMPTE_INDISPONIBLE", "Compte source bloqué ou fermé.");
        }
        if ("O".equalsIgnoreCase(dest.getFerme())) {
            throw new BusinessException("COMPTE_INDISPONIBLE", "Compte destinataire fermé.");
        }

        BigDecimal soldeSrc = soldeDisponible(source);
        if (soldeSrc.compareTo(montant) < 0) {
            throw new BusinessException("SOLDE_INSUFFISANT",
                "Solde insuffisant : " + soldeSrc + " disponible, " + montant + " demandé.");
        }

        // Débit source
        BigDecimal newDepotSrc = (source.getMontantDepot() != null ? source.getMontantDepot() : BigDecimal.ZERO).subtract(montant);
        source.setMontantDepot(newDepotSrc);

        // Crédit destination
        BigDecimal newDepotDest = (dest.getMontantDepot() != null ? dest.getMontantDepot() : BigDecimal.ZERO).add(montant);
        dest.setMontantDepot(newDepotDest);

        compteEpsRepo.save(source);
        compteEpsRepo.save(dest);

        // Journal — débit
        Epargne dbg = new Epargne();
        dbg.setCompteEps(source);
        dbg.setNumCompte(source.getNumCompte());
        dbg.setAgence(source.getAgence());
        dbg.setDateOperation(LocalDate.now());
        dbg.setDateValeur(LocalDate.now());
        dbg.setCodeTypeOperation("VIREMENT_DEBIT");
        dbg.setLibelleOperation(libelle != null && !libelle.isBlank()
            ? libelle : "Virement vers " + numCompteDest);
        dbg.setMontantDebit(montant);
        dbg.setMontantCredit(BigDecimal.ZERO);
        dbg.setSoldeAncien(soldeSrc);
        dbg.setSoldeNouveau(soldeSrc.subtract(montant));
        dbg.setUtilisateur(login);
        epargneRepo.save(dbg);

        // Journal — crédit
        BigDecimal soldeDest = soldeDisponible(dest).subtract(montant); // ancien (avant ajout)
        Epargne crd = new Epargne();
        crd.setCompteEps(dest);
        crd.setNumCompte(dest.getNumCompte());
        crd.setAgence(dest.getAgence());
        crd.setDateOperation(LocalDate.now());
        crd.setDateValeur(LocalDate.now());
        crd.setCodeTypeOperation("VIREMENT_CREDIT");
        crd.setLibelleOperation(libelle != null && !libelle.isBlank()
            ? libelle : "Virement depuis " + numCompteSource);
        crd.setMontantCredit(montant);
        crd.setMontantDebit(BigDecimal.ZERO);
        crd.setSoldeAncien(soldeDest);
        crd.setSoldeNouveau(soldeDest.add(montant));
        crd.setUtilisateur(login);
        epargneRepo.save(crd);

        String montantFmt = formatMontant(montant);
        notifyOperation(login, "Débit sur compte",
            dbg.getLibelleOperation() + " · " + montantFmt, dbg.getIdEpargne());
        resolveUserLoginForCompte(dest)
            .filter(benLogin -> !normalizeLogin(benLogin).equals(normalizeLogin(login)))
            .ifPresent(benLogin -> notifyOperation(benLogin, "Crédit sur compte",
                crd.getLibelleOperation() + " · " + montantFmt, crd.getIdEpargne()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("statut",      "OK");
        body.put("compteSource", numCompteSource);
        body.put("compteDest",   numCompteDest);
        body.put("montant",      montant);
        body.put("soldeSourceApres", soldeSrc.subtract(montant));
        return body;
    }

    /**
     * Virement vers un autre client mobile identifié par son numéro de téléphone (login).
     */
    @Transactional
    public Map<String, Object> transferExternalByPhone(String senderLogin,
                                                        String compteSource,
                                                        String telephoneBeneficiaire,
                                                        String nomBeneficiaire,
                                                        String banque,
                                                        BigDecimal montant,
                                                        String libelle) {
        String phone = normalizePhone(telephoneBeneficiaire);
        if (phone.isBlank()) {
            throw new BusinessException("TELEPHONE_INVALIDE", "Numéro de téléphone bénéficiaire invalide.");
        }
        if (phone.equals(normalizePhone(senderLogin))) {
            throw new BusinessException("SELF_TRANSFER", "Vous ne pouvez pas vous virer des fonds à vous-même.");
        }

        Utilisateur beneficiary = utilisateurRepo.findByLogin(phone)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Utilisateur", "Aucun client mobile enregistré pour le numéro " + phone));

        beneficiary = provisioningService.provisionIfMissing(beneficiary);

        String destCompte = beneficiary.getNumCompteCourant();
        if (destCompte == null || destCompte.isBlank()) {
            if (beneficiary.getNumMembre() != null && !beneficiary.getNumMembre().isBlank()) {
                var comptes = compteEpsRepo.findByMembre_NumMembre(beneficiary.getNumMembre());
                if (!comptes.isEmpty()) {
                    destCompte = comptes.get(0).getNumCompte();
                }
            }
        }
        if (destCompte == null || destCompte.isBlank()) {
            throw new BusinessException("COMPTE_BENEFICIAIRE_ABSENT",
                "Le bénéficiaire n'a pas de compte courant actif.");
        }

        StringBuilder lbl = new StringBuilder();
        lbl.append("Virement vers ");
        if (nomBeneficiaire != null && !nomBeneficiaire.isBlank()) {
            lbl.append(nomBeneficiaire.trim());
        } else if (beneficiary.getNomComplet() != null) {
            lbl.append(beneficiary.getNomComplet());
        } else {
            lbl.append(phone);
        }
        lbl.append(" (").append(phone).append(")");
        if (banque != null && !banque.isBlank()) {
            lbl.append(" — ").append(banque.trim());
        }
        if (libelle != null && !libelle.isBlank()) {
            lbl.append(" — ").append(libelle.trim());
        }

        Map<String, Object> result = transfer(senderLogin, compteSource, destCompte, montant, lbl.toString());
        result.put("telephoneBeneficiaire", phone);
        result.put("compteBeneficiaire", destCompte);
        return result;
    }

    private static String normalizePhone(String raw) {
        return normalizeLogin(raw);
    }

    // ── Paiement service ───────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> payService(String login,
                                          String numCompte,
                                          String serviceName,
                                          String reference,
                                          BigDecimal montant) {
        if (montant == null || montant.signum() <= 0) {
            throw new BusinessException("MONTANT_INVALIDE", "Le montant doit être strictement positif.");
        }
        if (serviceName == null || serviceName.isBlank()) {
            throw new BusinessException("SERVICE_REQUIS", "Nom du service requis.");
        }

        CompteEps source = compteEpsRepo.findById(numCompte)
            .orElseThrow(() -> new ResourceNotFoundException("CompteEps", numCompte));

        if ("O".equalsIgnoreCase(source.getBloque()) || "O".equalsIgnoreCase(source.getFerme())) {
            throw new BusinessException("COMPTE_INDISPONIBLE", "Compte source bloqué ou fermé.");
        }

        BigDecimal soldeSrc = soldeDisponible(source);
        if (soldeSrc.compareTo(montant) < 0) {
            throw new BusinessException("SOLDE_INSUFFISANT",
                "Solde insuffisant : " + soldeSrc + " disponible, " + montant + " demandé.");
        }

        BigDecimal newDepot = (source.getMontantDepot() != null ? source.getMontantDepot() : BigDecimal.ZERO).subtract(montant);
        source.setMontantDepot(newDepot);
        compteEpsRepo.save(source);

        Epargne mvt = new Epargne();
        mvt.setCompteEps(source);
        mvt.setNumCompte(source.getNumCompte());
        mvt.setAgence(source.getAgence());
        mvt.setDateOperation(LocalDate.now());
        mvt.setDateValeur(LocalDate.now());
        mvt.setCodeTypeOperation("PAIEMENT_SERVICE");
        mvt.setLibelleOperation("Paiement " + serviceName + (reference != null ? " — " + reference : ""));
        mvt.setMontantDebit(montant);
        mvt.setMontantCredit(BigDecimal.ZERO);
        mvt.setSoldeAncien(soldeSrc);
        mvt.setSoldeNouveau(soldeSrc.subtract(montant));
        mvt.setUtilisateur(login);
        if (reference != null) mvt.setNumPiece(reference);
        epargneRepo.save(mvt);

        notifyOperation(login, "Débit sur compte",
            mvt.getLibelleOperation() + " · " + formatMontant(montant), mvt.getIdEpargne());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("statut",        "OK");
        body.put("compte",        numCompte);
        body.put("service",       serviceName);
        body.put("reference",     reference);
        body.put("montant",       montant);
        body.put("soldeApres",    soldeSrc.subtract(montant));
        return body;
    }

}

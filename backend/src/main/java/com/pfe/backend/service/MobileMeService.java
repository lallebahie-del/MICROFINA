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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @Value("${app.photos.dir:./photos}")
    private String photosDir;

    private static final long MAX_PHOTO_SIZE = 2L * 1024 * 1024;
    private static final Set<String> ALLOWED_PHOTO_TYPES =
        Set.of("image/jpeg", "image/png");

    public MobileMeService(UtilisateurRepository utilisateurRepo,
                           CompteEpsRepository compteEpsRepo,
                           EpargneRepository epargneRepo,
                           NotificationRepository notificationRepo) {
        this.utilisateurRepo  = utilisateurRepo;
        this.compteEpsRepo    = compteEpsRepo;
        this.epargneRepo      = epargneRepo;
        this.notificationRepo = notificationRepo;
    }

    public Utilisateur currentUser(String login) {
        return utilisateurRepo.findByLogin(login)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", login));
    }

    // ── Notifications ───────────────────────────────────────────────────────

    public Map<String, Object> getNotifications(String login, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        Page<Notification> result = notificationRepo.findByUserLogin(login, pageable);

        List<Map<String, Object>> items = result.getContent().stream()
            .map(MobileMeService::toNotificationDto)
            .toList();

        long unread = notificationRepo.countByUserLoginAndLuFalse(login);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content",       items);
        body.put("totalElements", result.getTotalElements());
        body.put("totalPages",    result.getTotalPages());
        body.put("page",          result.getNumber());
        body.put("size",          result.getSize());
        body.put("unread",        unread);
        return body;
    }

    @Transactional
    public Notification markAsRead(String login, Long id) {
        Notification n = notificationRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        if (!login.equals(n.getUserLogin())) {
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
        Page<Notification> page = notificationRepo.findByUserLogin(
            login, PageRequest.of(0, 500, Sort.by("dateCreation").descending()));
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

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("statut",      "OK");
        body.put("compteSource", numCompteSource);
        body.put("compteDest",   numCompteDest);
        body.put("montant",      montant);
        body.put("soldeSourceApres", soldeSrc.subtract(montant));
        return body;
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

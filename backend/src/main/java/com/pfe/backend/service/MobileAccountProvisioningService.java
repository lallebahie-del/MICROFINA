package com.pfe.backend.service;

import com.microfina.entity.Adresse;
import com.microfina.entity.Agence;
import com.microfina.entity.CompteEps;
import com.microfina.entity.Membres;
import com.microfina.entity.Utilisateur;
import com.pfe.backend.repository.AdresseRepository;
import com.pfe.backend.repository.AgenceRepository;
import com.pfe.backend.repository.CompteEpsRepository;
import com.pfe.backend.repository.MembresRepository;
import com.pfe.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Crée membre + compte courant pour un {@link Utilisateur} mobile incomplet
 * (comptes créés avant la migration ou ancienne version du backend).
 */
@Service
public class MobileAccountProvisioningService {

    private final UtilisateurRepository utilisateurRepo;
    private final MembresRepository membresRepo;
    private final AdresseRepository adresseRepo;
    private final CompteEpsRepository compteEpsRepo;
    private final AgenceRepository agenceRepo;

    @Value("${microfina.mobile.default-agence:NKC}")
    private String defaultAgenceCode;

    public MobileAccountProvisioningService(
            UtilisateurRepository utilisateurRepo,
            MembresRepository membresRepo,
            AdresseRepository adresseRepo,
            CompteEpsRepository compteEpsRepo,
            AgenceRepository agenceRepo) {
        this.utilisateurRepo = utilisateurRepo;
        this.membresRepo = membresRepo;
        this.adresseRepo = adresseRepo;
        this.compteEpsRepo = compteEpsRepo;
        this.agenceRepo = agenceRepo;
    }

    /**
     * @return utilisateur rechargé avec numMembre / numCompteCourant si provision effectuée
     */
    @Transactional
    public Utilisateur provisionIfMissing(Utilisateur user) {
        if (user == null) {
            return null;
        }
        boolean needsMembre = isBlank(user.getNumMembre());
        boolean needsCompte = isBlank(user.getNumCompteCourant());
        if (!needsMembre && !needsCompte) {
            return user;
        }

        String login = user.getLogin();
        String numMembre = needsMembre ? buildNumMembre(login) : user.getNumMembre();

        Agence agence = resolveAgence(user);
        if (needsMembre && !membresRepo.existsById(numMembre)) {
            NameParts names = splitNomComplet(user.getNomComplet(), login);
            Membres membre = new Membres();
            membre.setNumMembre(numMembre);
            membre.setDtype("PP");
            membre.setNom(names.nom());
            membre.setPrenom(names.prenom());
            membre.setEtat("ACTIF");
            membre.setStatut("VALIDE");
            membre.setDateCreationUser(LocalDate.now());
            membre.setDateDemande(LocalDate.now());
            membre.setDateValidation(LocalDate.now());
            membre.setAgence(agence);
            membresRepo.save(membre);
        } else if (!isBlank(numMembre)) {
            membresRepo.findById(numMembre).ifPresent(m -> {
                if (isBlank(m.getDtype()) || "Membres".equalsIgnoreCase(m.getDtype())) {
                    m.setDtype("PP");
                    membresRepo.save(m);
                }
            });
        }

        String numCompte = user.getNumCompteCourant();
        if (isBlank(numCompte)) {
            numCompte = buildNumCompte(numMembre);
            if (compteEpsRepo.existsById(numCompte)) {
                numCompte = numCompte + "-" + (System.currentTimeMillis() % 10000);
            }
        }

        if (!compteEpsRepo.existsById(numCompte)) {
            CompteEps compte = new CompteEps();
            compte.setNumCompte(numCompte);
            compte.setMembre(membresRepo.getReferenceById(numMembre));
            compte.setCodeMembre(numMembre);
            compte.setAgence(agence);
            compte.setTypeEpargne("Compte courant");
            compte.setProduitEpargne("EPARGNE");
            compte.setMontantOuvert(BigDecimal.ZERO);
            compte.setMontantDepot(BigDecimal.ZERO);
            compte.setDateCreation(LocalDate.now());
            compte.setBloque("N");
            compte.setFerme("N");
            compte.setExonere("N");
            compteEpsRepo.save(compte);
        }

        user.setNumMembre(numMembre);
        user.setNumCompteCourant(numCompte);
        if (user.getAgence() == null) {
            user.setAgence(agence);
        }
        return utilisateurRepo.save(user);
    }

    @Transactional
    public Adresse saveAddressForUser(Utilisateur user, MobileRegistrationService.AddressInput address) {
        if (address == null || !hasAddressData(address)) {
            return null;
        }
        Adresse adresseEntity = new Adresse();
        adresseEntity.setIdAdresse(UUID.randomUUID().toString().replace("-", ""));
        adresseEntity.setDtype("AdresseMembre");
        adresseEntity.setAdresse(firstNonBlank(address.adresse(), formatAddressLine(address)));
        adresseEntity.setAdresse1(firstNonBlank(address.adresse1(), address.ville()));
        adresseEntity.setRueMaison(address.rueMaison());
        adresseEntity.setDescriptionAdresse(address.ville());
        adresseEntity.setTelephoneMobile(user.getTelephone());
        adresseEntity.setLatitude(trimTo(address.latitude(), 255));
        adresseEntity.setLongitude(trimTo(address.longitude(), 255));
        final Adresse saved = adresseRepo.save(adresseEntity);

        user.setAdresse(firstNonBlank(saved.getAdresse(), formatAddressLine(address)));
        user.setVille(firstNonBlank(saved.getDescriptionAdresse(), address.ville()));
        user.setLatitude(saved.getLatitude());
        user.setLongitude(saved.getLongitude());
        utilisateurRepo.save(user);

        if (user.getNumMembre() != null && !user.getNumMembre().isBlank()) {
            membresRepo.findById(user.getNumMembre()).ifPresent(m -> {
                m.setAdresse(saved);
                membresRepo.save(m);
            });
        }
        return saved;
    }

    private Agence resolveAgence(Utilisateur user) {
        if (user.getAgence() != null && user.getAgence().getCodeAgence() != null) {
            return user.getAgence();
        }
        String code = agenceRepo.existsById(defaultAgenceCode)
                ? defaultAgenceCode
                : agenceRepo.findAll().stream()
                    .findFirst()
                    .map(Agence::getCodeAgence)
                    .orElse(defaultAgenceCode);
        return agenceRepo.getReferenceById(code);
    }

    static String buildNumMembre(String phone) {
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() > 18) {
            digits = digits.substring(digits.length() - 18);
        }
        return "MOB" + digits;
    }

    static String buildNumCompte(String numMembre) {
        String suffix = numMembre.length() > 12 ? numMembre.substring(numMembre.length() - 12) : numMembre;
        return "EPS-" + suffix + "-CC";
    }

    static boolean hasAddressData(MobileRegistrationService.AddressInput address) {
        return isNotBlank(address.adresse())
                || isNotBlank(address.adresse1())
                || isNotBlank(address.rueMaison())
                || isNotBlank(address.ville())
                || isNotBlank(address.latitude())
                || isNotBlank(address.longitude());
    }

    static String formatAddressLine(MobileRegistrationService.AddressInput address) {
        StringBuilder sb = new StringBuilder();
        if (isNotBlank(address.rueMaison())) {
            sb.append(address.rueMaison().trim());
        }
        if (isNotBlank(address.ville())) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(address.ville().trim());
        }
        return sb.toString();
    }

    static NameParts splitNomComplet(String nomComplet, String fallback) {
        String full = nomComplet != null && !nomComplet.isBlank() ? nomComplet.trim() : fallback;
        int space = full.lastIndexOf(' ');
        if (space > 0 && space < full.length() - 1) {
            return new NameParts(full.substring(space + 1).trim(), full.substring(0, space).trim());
        }
        return new NameParts(full, "");
    }

    static String firstNonBlank(String primary, String fallback) {
        if (isNotBlank(primary)) return primary.trim();
        if (isNotBlank(fallback)) return fallback.trim();
        return null;
    }

    static String trimTo(String value, int max) {
        if (value == null || value.isBlank()) return null;
        String t = value.trim();
        return t.length() > max ? t.substring(0, max) : t;
    }

    static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    record NameParts(String nom, String prenom) {}
}

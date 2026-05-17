package com.pfe.backend.service;

import com.microfina.entity.Adresse;
import com.microfina.entity.Agence;
import com.microfina.entity.Membres;
import com.microfina.entity.Utilisateur;
import com.pfe.backend.repository.AdresseRepository;
import com.pfe.backend.repository.AgenceRepository;
import com.pfe.backend.repository.MembresRepository;
import com.pfe.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Inscription mobile : Utilisateur + Membre + Adresse + compte courant (COMPTEEPS).
 */
@Service
public class MobileRegistrationService {

    private final UtilisateurRepository utilisateurRepo;
    private final MembresRepository membresRepo;
    private final AdresseRepository adresseRepo;
    private final AgenceRepository agenceRepo;
    private final PasswordEncoder passwordEncoder;
    private final MobileAccountProvisioningService provisioningService;

    @Value("${microfina.mobile.default-agence:NKC}")
    private String defaultAgenceCode;

    public MobileRegistrationService(
            UtilisateurRepository utilisateurRepo,
            MembresRepository membresRepo,
            AdresseRepository adresseRepo,
            AgenceRepository agenceRepo,
            PasswordEncoder passwordEncoder,
            MobileAccountProvisioningService provisioningService) {
        this.utilisateurRepo = utilisateurRepo;
        this.membresRepo = membresRepo;
        this.adresseRepo = adresseRepo;
        this.agenceRepo = agenceRepo;
        this.passwordEncoder = passwordEncoder;
        this.provisioningService = provisioningService;
    }

    public record AddressInput(
            String adresse,
            String adresse1,
            String rueMaison,
            String ville,
            String latitude,
            String longitude
    ) {}

    public record RegistrationResult(
            Utilisateur utilisateur,
            String numMembre,
            String numCompte
    ) {}

    @Transactional
    public RegistrationResult register(
            String phone,
            String pin,
            String nomComplet,
            String email,
            String codeAgenceHint,
            AddressInput address) {

        if (utilisateurRepo.findByLogin(phone).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un compte existe déjà pour ce numéro.");
        }

        String codeAgence = resolveAgenceCode(codeAgenceHint);
        Agence agence = agenceRepo.getReferenceById(codeAgence);

        String numMembre = MobileAccountProvisioningService.buildNumMembre(phone);
        if (membresRepo.existsById(numMembre)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Un membre existe déjà pour ce numéro.");
        }

        Adresse adresseEntity = null;
        if (address != null && MobileAccountProvisioningService.hasAddressData(address)) {
            adresseEntity = new Adresse();
            adresseEntity.setIdAdresse(UUID.randomUUID().toString().replace("-", ""));
            adresseEntity.setDtype("AdresseMembre");
            adresseEntity.setAdresse(MobileAccountProvisioningService.firstNonBlank(
                    address.adresse(), MobileAccountProvisioningService.formatAddressLine(address)));
            adresseEntity.setAdresse1(MobileAccountProvisioningService.firstNonBlank(
                    address.adresse1(), address.ville()));
            adresseEntity.setRueMaison(address.rueMaison());
            adresseEntity.setDescriptionAdresse(address.ville());
            adresseEntity.setTelephoneMobile(phone);
            adresseEntity.setLatitude(MobileAccountProvisioningService.trimTo(address.latitude(), 255));
            adresseEntity.setLongitude(MobileAccountProvisioningService.trimTo(address.longitude(), 255));
            adresseEntity = adresseRepo.save(adresseEntity);
        }

        var names = MobileAccountProvisioningService.splitNomComplet(nomComplet, phone);

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
        membre.setAdresse(adresseEntity);
        membresRepo.save(membre);

        Utilisateur u = new Utilisateur();
        u.setLogin(phone);
        u.setMotDePasseHash(passwordEncoder.encode(pin));
        u.setNomComplet(nomComplet != null && !nomComplet.isBlank() ? nomComplet : phone);
        u.setEmail(email);
        u.setTelephone(phone);
        u.setActif(Boolean.TRUE);
        u.setNombreEchecs(0);
        u.setAgence(agence);
        u.setNumMembre(numMembre);

        if (adresseEntity != null) {
            u.setAdresse(adresseEntity.getAdresse());
            u.setVille(adresseEntity.getDescriptionAdresse());
            u.setLatitude(adresseEntity.getLatitude());
            u.setLongitude(adresseEntity.getLongitude());
        } else if (address != null) {
            u.setAdresse(MobileAccountProvisioningService.formatAddressLine(address));
            u.setVille(address.ville());
            u.setLatitude(MobileAccountProvisioningService.trimTo(address.latitude(), 50));
            u.setLongitude(MobileAccountProvisioningService.trimTo(address.longitude(), 50));
        }

        u = utilisateurRepo.save(u);
        u = provisioningService.provisionIfMissing(u);

        return new RegistrationResult(u, u.getNumMembre(), u.getNumCompteCourant());
    }

    private String resolveAgenceCode(String hint) {
        if (hint != null && !hint.isBlank() && agenceRepo.existsById(hint.trim())) {
            return hint.trim();
        }
        if (agenceRepo.existsById(defaultAgenceCode)) {
            return defaultAgenceCode;
        }
        return agenceRepo.findAll().stream()
                .findFirst()
                .map(Agence::getCodeAgence)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Aucune agence configurée pour l'inscription mobile."));
    }
}

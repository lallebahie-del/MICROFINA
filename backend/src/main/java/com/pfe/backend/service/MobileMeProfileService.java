package com.pfe.backend.service;

import com.microfina.entity.Adresse;
import com.microfina.entity.CompteEps;
import com.microfina.entity.Utilisateur;
import com.pfe.backend.repository.CompteEpsRepository;
import com.pfe.backend.repository.MembresRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class MobileMeProfileService {

    private final MembresRepository membresRepo;
    private final CompteEpsRepository compteEpsRepo;

    public MobileMeProfileService(MembresRepository membresRepo, CompteEpsRepository compteEpsRepo) {
        this.membresRepo = membresRepo;
        this.compteEpsRepo = compteEpsRepo;
    }

    public Map<String, Object> buildProfile(Utilisateur user) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("login", user.getLogin());
        body.put("nomComplet", user.getNomComplet() != null ? user.getNomComplet() : "");
        body.put("email", user.getEmail() != null ? user.getEmail() : "");
        body.put("telephone", user.getTelephone() != null ? user.getTelephone() : "");
        body.put("codeAgence", user.getAgence() != null ? user.getAgence().getCodeAgence() : "");
        body.put("numMembre", user.getNumMembre() != null ? user.getNumMembre() : "");
        body.put("typeMembre", resolveTypeMembre(user));
        body.put("actif", user.getActif() != null ? user.getActif() : Boolean.FALSE);

        String adresse = user.getAdresse();
        String ville = user.getVille();
        String latitude = user.getLatitude();
        String longitude = user.getLongitude();

        if (isBlank(adresse) && user.getNumMembre() != null && !user.getNumMembre().isBlank()) {
            var membreOpt = membresRepo.findById(user.getNumMembre());
            if (membreOpt.isPresent() && membreOpt.get().getAdresse() != null) {
                Adresse a = membreOpt.get().getAdresse();
                adresse = formatAdresse(a);
                if (isBlank(ville)) {
                    ville = a.getDescriptionAdresse();
                }
                if (isBlank(latitude)) {
                    latitude = a.getLatitude();
                }
                if (isBlank(longitude)) {
                    longitude = a.getLongitude();
                }
            }
        }

        body.put("adresse", adresse != null ? adresse : "");
        body.put("ville", ville != null ? ville : "");
        body.put("latitude", latitude != null ? latitude : "");
        body.put("longitude", longitude != null ? longitude : "");

        String numCompte = user.getNumCompteCourant();
        if (isBlank(numCompte) && user.getNumMembre() != null && !user.getNumMembre().isBlank()) {
            numCompte = resolveCompteCourant(user.getNumMembre());
        }
        body.put("numCompteCourant", numCompte != null ? numCompte : "");

        return body;
    }

    private String resolveTypeMembre(Utilisateur user) {
        if (user.getNumMembre() == null || user.getNumMembre().isBlank()) {
            return "PP";
        }
        return membresRepo.findById(user.getNumMembre())
                .map(m -> m.getDtype() != null && !m.getDtype().isBlank() ? m.getDtype() : "PP")
                .orElse("PP");
    }

    private String resolveCompteCourant(String numMembre) {
        List<CompteEps> comptes = compteEpsRepo.findByMembre_NumMembre(numMembre);
        return comptes.stream()
                .filter(c -> c.getTypeEpargne() != null
                        && c.getTypeEpargne().toLowerCase().contains("courant"))
                .map(CompteEps::getNumCompte)
                .findFirst()
                .orElse(comptes.isEmpty() ? null : comptes.get(0).getNumCompte());
    }

    private static String formatAdresse(Adresse a) {
        StringBuilder sb = new StringBuilder();
        if (!isBlank(a.getAdresse())) sb.append(a.getAdresse().trim());
        if (!isBlank(a.getRueMaison())) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(a.getRueMaison().trim());
        }
        if (!isBlank(a.getAdresse1())) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(a.getAdresse1().trim());
        }
        if (!isBlank(a.getDescriptionAdresse())) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(a.getDescriptionAdresse().trim());
        }
        return sb.toString();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}

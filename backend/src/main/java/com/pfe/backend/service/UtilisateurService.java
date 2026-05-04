package com.pfe.backend.service;

import com.microfina.entity.Agence;
import com.microfina.entity.Utilisateur;
import com.pfe.backend.dto.UtilisateurDTO;
import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.AgenceRepository;
import com.pfe.backend.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UtilisateurService — gestion des comptes utilisateurs applicatifs.
 */
@Service
@Transactional(readOnly = true)
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final AgenceRepository agenceRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                               AgenceRepository agenceRepository,
                               PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.agenceRepository = agenceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retourne la liste de tous les utilisateurs.
     */
    public List<UtilisateurDTO.Response> findAll() {
        return utilisateurRepository.findAll()
            .stream()
            .map(UtilisateurDTO.Response::from)
            .toList();
    }

    /**
     * Retourne un utilisateur par son identifiant technique.
     *
     * @param id identifiant technique
     * @return DTO Response
     * @throws ResourceNotFoundException si introuvable
     */
    public UtilisateurDTO.Response findById(Long id) {
        Utilisateur u = utilisateurRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
        return UtilisateurDTO.Response.from(u);
    }

    /**
     * Retourne les utilisateurs rattachés à une agence donnée.
     *
     * @param codeAgence code de l'agence
     * @return liste de DTOs Response
     */
    public List<UtilisateurDTO.Response> findByAgence(String codeAgence) {
        return utilisateurRepository.findByAgence_CodeAgence(codeAgence)
            .stream()
            .map(UtilisateurDTO.Response::from)
            .toList();
    }

    /**
     * Crée un nouvel utilisateur. Le mot de passe est BCrypt-encodé avant persistance.
     *
     * @param req données de création
     * @return DTO Response de l'utilisateur créé
     * @throws BusinessException si le login est déjà utilisé
     */
    @Transactional
    public UtilisateurDTO.Response create(UtilisateurDTO.CreateRequest req) {
        if (utilisateurRepository.existsByLogin(req.login())) {
            throw new BusinessException("Login déjà utilisé : " + req.login());
        }

        Utilisateur u = new Utilisateur();
        u.setLogin(req.login());
        u.setMotDePasseHash(passwordEncoder.encode(req.motDePasse()));
        u.setNomComplet(req.nomComplet());
        u.setEmail(req.email());
        u.setTelephone(req.telephone());
        u.setActif(req.actif() != null ? req.actif() : Boolean.TRUE);
        u.setDateExpirationCompte(req.dateExpirationCompte());
        u.setNombreEchecs(0);

        if (req.codeAgence() != null && !req.codeAgence().isBlank()) {
            Agence agence = agenceRepository.findById(req.codeAgence())
                .orElseThrow(() -> new ResourceNotFoundException("Agence", req.codeAgence()));
            u.setAgence(agence);
        }

        return UtilisateurDTO.Response.from(utilisateurRepository.save(u));
    }

    /**
     * Met à jour partiellement un utilisateur (patch — seuls les champs non-null sont appliqués).
     *
     * @param id  identifiant technique
     * @param req champs à mettre à jour
     * @return DTO Response mis à jour
     * @throws ResourceNotFoundException si l'utilisateur est introuvable
     */
    @Transactional
    public UtilisateurDTO.Response update(Long id, UtilisateurDTO.UpdateRequest req) {
        Utilisateur u = utilisateurRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        if (req.nomComplet() != null)           u.setNomComplet(req.nomComplet());
        if (req.email() != null)                u.setEmail(req.email());
        if (req.telephone() != null)            u.setTelephone(req.telephone());
        if (req.actif() != null)                u.setActif(req.actif());
        if (req.dateExpirationCompte() != null) u.setDateExpirationCompte(req.dateExpirationCompte());

        if (req.codeAgence() != null) {
            if (req.codeAgence().isBlank()) {
                u.setAgence(null);
            } else {
                Agence agence = agenceRepository.findById(req.codeAgence())
                    .orElseThrow(() -> new ResourceNotFoundException("Agence", req.codeAgence()));
                u.setAgence(agence);
            }
        }

        return UtilisateurDTO.Response.from(utilisateurRepository.save(u));
    }

    /**
     * Désactive un compte utilisateur (actif = false).
     *
     * @param id identifiant technique
     * @throws ResourceNotFoundException si l'utilisateur est introuvable
     */
    @Transactional
    public void desactiver(Long id) {
        Utilisateur u = utilisateurRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
        u.setActif(false);
        utilisateurRepository.save(u);
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur et remet les échecs à zéro.
     *
     * @param id              identifiant technique
     * @param nouveauMotDePasse nouveau mot de passe en clair (sera BCrypt-encodé)
     * @throws ResourceNotFoundException si l'utilisateur est introuvable
     */
    @Transactional
    public void reinitialiserMotDePasse(Long id, String nouveauMotDePasse) {
        Utilisateur u = utilisateurRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
        u.setMotDePasseHash(passwordEncoder.encode(nouveauMotDePasse));
        u.setNombreEchecs(0);
        utilisateurRepository.save(u);
    }

    /**
     * Supprime un utilisateur.
     *
     * @param id identifiant technique
     * @throws ResourceNotFoundException si l'utilisateur est introuvable
     */
    @Transactional
    public void delete(Long id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur", id);
        }
        utilisateurRepository.deleteById(id);
    }
}

package com.pfe.backend.service;

import com.microfina.entity.Agence;
import com.microfina.entity.Role;
import com.microfina.entity.Utilisateur;
import com.microfina.entity.UtilisateurRole;
import com.microfina.entity.UtilisateurRoleId;
import com.pfe.backend.dto.UtilisateurDTO;
import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.AgenceRepository;
import com.pfe.backend.repository.RoleRepository;
import com.pfe.backend.repository.UtilisateurRepository;
import com.pfe.backend.repository.UtilisateurRoleRepository;
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
    private final UtilisateurRoleRepository utilisateurRoleRepository;
    private final RoleRepository roleRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                               AgenceRepository agenceRepository,
                               PasswordEncoder passwordEncoder,
                               UtilisateurRoleRepository utilisateurRoleRepository,
                               RoleRepository roleRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.agenceRepository = agenceRepository;
        this.passwordEncoder = passwordEncoder;
        this.utilisateurRoleRepository = utilisateurRoleRepository;
        this.roleRepository = roleRepository;
    }

    public List<UtilisateurDTO.Response> findAll() {
        return utilisateurRepository.findAll()
            .stream()
            .map(u -> UtilisateurDTO.Response.from(u, getRoles(u.getId())))
            .toList();
    }

    public UtilisateurDTO.Response findById(Long id) {
        Utilisateur u = utilisateurRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
        return UtilisateurDTO.Response.from(u, getRoles(u.getId()));
    }

    public List<UtilisateurDTO.Response> findByAgence(String codeAgence) {
        return utilisateurRepository.findByAgence_CodeAgence(codeAgence)
            .stream()
            .map(u -> UtilisateurDTO.Response.from(u, getRoles(u.getId())))
            .toList();
    }

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

        Utilisateur saved = utilisateurRepository.save(u);

        if (req.roles() != null && !req.roles().isEmpty()) {
            assignRoles(saved, req.roles());
        }

        return UtilisateurDTO.Response.from(saved, getRoles(saved.getId()));
    }

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

        if (req.roles() != null) {
            assignRoles(u, req.roles());
        }

        Utilisateur saved = utilisateurRepository.save(u);
        return UtilisateurDTO.Response.from(saved, getRoles(saved.getId()));
    }

    @Transactional
    public void desactiver(Long id) {
        Utilisateur u = utilisateurRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
        u.setActif(false);
        utilisateurRepository.save(u);
    }

    @Transactional
    public void reinitialiserMotDePasse(Long id, String nouveauMotDePasse) {
        Utilisateur u = utilisateurRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
        u.setMotDePasseHash(passwordEncoder.encode(nouveauMotDePasse));
        u.setNombreEchecs(0);
        utilisateurRepository.save(u);
    }

    @Transactional
    public void delete(Long id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur", id);
        }
        utilisateurRoleRepository.deleteByUserId(id);
        utilisateurRepository.deleteById(id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<String> getRoles(Long userId) {
        return utilisateurRoleRepository.findWithRoleByUserId(userId)
            .stream()
            .map(ur -> ur.getRole().getCodeRole())
            .toList();
    }

    private void assignRoles(Utilisateur u, List<String> roleCodes) {
        utilisateurRoleRepository.deleteByUserId(u.getId());
        for (String code : roleCodes) {
            Role role = roleRepository.findByCodeRole(code)
                .orElseThrow(() -> new BusinessException("Rôle inconnu : " + code));
            utilisateurRoleRepository.save(
                new UtilisateurRole(new UtilisateurRoleId(u.getId(), role.getId()), u, role));
        }
    }
}

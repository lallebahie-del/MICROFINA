package com.pfe.backend.service;

import com.microfina.entity.Agence;
import com.microfina.entity.Privilege;
import com.microfina.entity.Role;
import com.microfina.entity.RolePrivilege;
import com.microfina.entity.RolePrivilegeId;
import com.microfina.entity.Utilisateur;
import com.microfina.entity.UtilisateurRole;
import com.microfina.entity.UtilisateurRoleId;
import com.pfe.backend.dto.UtilisateurDTO;
import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.AgenceRepository;
import com.pfe.backend.repository.PrivilegeRepository;
import com.pfe.backend.repository.RolePrivilegeRepository;
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
    private final PrivilegeRepository privilegeRepository;
    private final RolePrivilegeRepository rolePrivilegeRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                               AgenceRepository agenceRepository,
                               PasswordEncoder passwordEncoder,
                               UtilisateurRoleRepository utilisateurRoleRepository,
                               RoleRepository roleRepository,
                               PrivilegeRepository privilegeRepository,
                               RolePrivilegeRepository rolePrivilegeRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.agenceRepository = agenceRepository;
        this.passwordEncoder = passwordEncoder;
        this.utilisateurRoleRepository = utilisateurRoleRepository;
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
        this.rolePrivilegeRepository = rolePrivilegeRepository;
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

    /**
     * Récupère les codes de privilèges actuellement accordés à l'utilisateur
     * via son rôle "direct" auto-géré (USER_<login>_DIRECT).
     */
    public List<String> getDirectPrivileges(Long userId) {
        Utilisateur u = utilisateurRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));
        String code = directRoleCode(u);
        return roleRepository.findByCodeRole(code)
            .map(r -> rolePrivilegeRepository.findWithPrivilegeByRoleId(r.getId())
                .stream()
                .map(rp -> rp.getPrivilege().getCodePrivilege())
                .toList())
            .orElseGet(List::of);
    }

    /**
     * Attribue à l'utilisateur la liste exacte de privilèges fournie en
     * créant/mettant à jour son rôle "direct" auto-géré
     * (code = USER_<login>_DIRECT) puis en s'assurant que ce rôle lui
     * est bien rattaché.
     */
    @Transactional
    public UtilisateurDTO.Response setDirectPrivileges(Long userId, List<String> codePrivileges) {
        Utilisateur u = utilisateurRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

        String code = directRoleCode(u);
        Role role = roleRepository.findByCodeRole(code).orElseGet(() -> {
            Role r = new Role();
            r.setCodeRole(code);
            r.setLibelle("Privilèges directs de " + u.getLogin());
            r.setDescription("Rôle technique auto-géré pour attribuer des privilèges directs.");
            return roleRepository.save(r);
        });

        // Réinitialise les associations RolePrivilege
        rolePrivilegeRepository.deleteByRoleId(role.getId());
        rolePrivilegeRepository.flush();

        if (codePrivileges != null && !codePrivileges.isEmpty()) {
            List<Privilege> privs = privilegeRepository.findByCodePrivilegeIn(codePrivileges);
            for (Privilege p : privs) {
                rolePrivilegeRepository.save(new RolePrivilege(
                    new RolePrivilegeId(role.getId(), p.getId()), role, p));
            }
        }

        // Assure l'attribution du rôle direct à l'utilisateur
        boolean hasRole = utilisateurRoleRepository.findWithRoleByUserId(u.getId())
            .stream()
            .anyMatch(ur -> code.equals(ur.getRole().getCodeRole()));
        if (!hasRole) {
            utilisateurRoleRepository.save(new UtilisateurRole(
                new UtilisateurRoleId(u.getId(), role.getId()), u, role));
        }

        return UtilisateurDTO.Response.from(u, getRoles(u.getId()));
    }

    private String directRoleCode(Utilisateur u) {
        String login = u.getLogin() != null ? u.getLogin().toUpperCase() : ("ID" + u.getId());
        return "USER_" + login + "_DIRECT";
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

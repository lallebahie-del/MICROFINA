package com.pfe.backend.service;

import com.microfina.entity.Role;
import com.pfe.backend.dto.RoleDTO;
import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RoleService — gestion des rôles fonctionnels.
 */
@Service
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Retourne la liste de tous les rôles.
     */
    public List<RoleDTO.Response> findAll() {
        return roleRepository.findAll()
            .stream()
            .map(RoleDTO.Response::from)
            .toList();
    }

    /**
     * Retourne un rôle par son identifiant technique.
     *
     * @param id identifiant technique
     * @return DTO Response
     * @throws ResourceNotFoundException si introuvable
     */
    public RoleDTO.Response findById(Long id) {
        Role r = roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        return RoleDTO.Response.from(r);
    }

    /**
     * Retourne un rôle par son code unique.
     *
     * @param codeRole code du rôle
     * @return DTO Response
     * @throws ResourceNotFoundException si introuvable
     */
    public RoleDTO.Response findByCode(String codeRole) {
        Role r = roleRepository.findByCodeRole(codeRole)
            .orElseThrow(() -> new ResourceNotFoundException("Role", codeRole));
        return RoleDTO.Response.from(r);
    }

    /**
     * Crée un nouveau rôle.
     *
     * @param req données de création
     * @return DTO Response du rôle créé
     * @throws BusinessException si le code rôle est déjà utilisé
     */
    @Transactional
    public RoleDTO.Response create(RoleDTO.CreateRequest req) {
        if (roleRepository.existsByCodeRole(req.codeRole())) {
            throw new BusinessException("Code rôle déjà utilisé : " + req.codeRole());
        }

        Role r = new Role();
        r.setCodeRole(req.codeRole());
        r.setLibelle(req.libelle());
        r.setDescription(req.description());

        return RoleDTO.Response.from(roleRepository.save(r));
    }

    /**
     * Met à jour partiellement un rôle (patch — seuls les champs non-null sont appliqués).
     *
     * @param id  identifiant technique
     * @param req champs à mettre à jour
     * @return DTO Response mis à jour
     * @throws ResourceNotFoundException si le rôle est introuvable
     */
    @Transactional
    public RoleDTO.Response update(Long id, RoleDTO.UpdateRequest req) {
        Role r = roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role", id));

        if (req.libelle() != null)     r.setLibelle(req.libelle());
        if (req.description() != null) r.setDescription(req.description());

        return RoleDTO.Response.from(roleRepository.save(r));
    }

    /**
     * Supprime un rôle.
     *
     * @param id identifiant technique
     * @throws ResourceNotFoundException si le rôle est introuvable
     */
    @Transactional
    public void delete(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role", id);
        }
        roleRepository.deleteById(id);
    }
}

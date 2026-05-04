package com.pfe.backend.service;

import com.microfina.entity.Privilege;
import com.pfe.backend.dto.PrivilegeDTO;
import com.pfe.backend.exception.BusinessException;
import com.pfe.backend.exception.ResourceNotFoundException;
import com.pfe.backend.repository.PrivilegeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PrivilegeService — gestion des privilèges fonctionnels granulaires.
 */
@Service
@Transactional(readOnly = true)
public class PrivilegeService {

    private final PrivilegeRepository privilegeRepository;

    public PrivilegeService(PrivilegeRepository privilegeRepository) {
        this.privilegeRepository = privilegeRepository;
    }

    /**
     * Retourne la liste de tous les privilèges.
     */
    public List<PrivilegeDTO.Response> findAll() {
        return privilegeRepository.findAll()
            .stream()
            .map(PrivilegeDTO.Response::from)
            .toList();
    }

    /**
     * Retourne un privilège par son identifiant technique.
     *
     * @param id identifiant technique
     * @return DTO Response
     * @throws ResourceNotFoundException si introuvable
     */
    public PrivilegeDTO.Response findById(Long id) {
        Privilege p = privilegeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Privilege", id));
        return PrivilegeDTO.Response.from(p);
    }

    /**
     * Retourne les privilèges appartenant à un module donné.
     *
     * @param module code du module fonctionnel
     * @return liste de DTOs Response
     */
    public List<PrivilegeDTO.Response> findByModule(String module) {
        return privilegeRepository.findByModule(module)
            .stream()
            .map(PrivilegeDTO.Response::from)
            .toList();
    }

    /**
     * Crée un nouveau privilège.
     *
     * @param req données de création
     * @return DTO Response du privilège créé
     * @throws BusinessException si le code privilège est déjà utilisé
     */
    @Transactional
    public PrivilegeDTO.Response create(PrivilegeDTO.CreateRequest req) {
        if (privilegeRepository.existsByCodePrivilege(req.codePrivilege())) {
            throw new BusinessException("Code privilège déjà utilisé : " + req.codePrivilege());
        }

        Privilege p = new Privilege();
        p.setCodePrivilege(req.codePrivilege());
        p.setLibelle(req.libelle());
        p.setModule(req.module());

        return PrivilegeDTO.Response.from(privilegeRepository.save(p));
    }

    /**
     * Met à jour partiellement un privilège (patch — seuls les champs non-null sont appliqués).
     *
     * @param id  identifiant technique
     * @param req champs à mettre à jour
     * @return DTO Response mis à jour
     * @throws ResourceNotFoundException si le privilège est introuvable
     */
    @Transactional
    public PrivilegeDTO.Response update(Long id, PrivilegeDTO.UpdateRequest req) {
        Privilege p = privilegeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Privilege", id));

        if (req.libelle() != null) p.setLibelle(req.libelle());
        if (req.module() != null)  p.setModule(req.module());

        return PrivilegeDTO.Response.from(privilegeRepository.save(p));
    }

    /**
     * Supprime un privilège.
     *
     * @param id identifiant technique
     * @throws ResourceNotFoundException si le privilège est introuvable
     */
    @Transactional
    public void delete(Long id) {
        if (!privilegeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Privilege", id);
        }
        privilegeRepository.deleteById(id);
    }
}

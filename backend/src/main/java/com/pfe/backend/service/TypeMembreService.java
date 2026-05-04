package com.pfe.backend.service;

import com.pfe.backend.dto.TypeMembreDTO;
import com.pfe.backend.repository.TypeMembreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TypeMembreService — consultation des types membres éligibles aux produits crédit.
 */
@Service
@Transactional(readOnly = true)
public class TypeMembreService {

    private final TypeMembreRepository typeMembreRepository;

    public TypeMembreService(TypeMembreRepository typeMembreRepository) {
        this.typeMembreRepository = typeMembreRepository;
    }

    /**
     * Retourne la liste distincte des codes types membres.
     *
     * @return liste de codes types membres (ex : "PP", "PM", "GS")
     */
    public List<String> findAll() {
        return typeMembreRepository.findDistinctTypesMembre();
    }

    /**
     * Retourne les associations produit-type membre pour un type membre donné.
     *
     * @param typeMembre code du type membre
     * @return liste de DTOs Response
     */
    public List<TypeMembreDTO.Response> findByType(String typeMembre) {
        return typeMembreRepository.findById_TypeMembre(typeMembre)
            .stream()
            .map(TypeMembreDTO.Response::from)
            .toList();
    }
}

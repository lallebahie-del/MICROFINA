package com.pfe.backend.mapper;

import com.microfina.entity.Banque;
import com.pfe.backend.dto.BanqueDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * BanqueMapper — conversion MapStruct entre {@link Banque} et {@link BanqueDTO}.
 *
 * <p>Les noms de champs sont identiques entre l'entité et le DTO,
 * donc aucun {@code @Mapping} n'est nécessaire pour la plupart des champs.
 * Le champ {@code version} de l'entité est ignoré car absent du DTO.</p>
 */
@Mapper(componentModel = "spring")
public interface BanqueMapper {

    /**
     * Convertit une {@link BanqueDTO.CreateRequest} en entité {@link Banque}.
     * Le champ {@code version} est géré par Hibernate et doit être ignoré.
     */
    @Mapping(target = "version", ignore = true)
    Banque toEntity(BanqueDTO.CreateRequest req);

    /**
     * Convertit une entité {@link Banque} en {@link BanqueDTO.Response}.
     * Le champ {@code version} de l'entité n'est pas exposé dans le DTO.
     */
    BanqueDTO.Response toDto(Banque banque);
}

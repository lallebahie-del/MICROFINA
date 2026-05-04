package com.pfe.backend.mapper;

import com.microfina.entity.Parametre;
import com.pfe.backend.dto.ParametreDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * ParametreMapper — conversion MapStruct entre {@link Parametre}
 * et {@link ParametreDTO}.
 */
@Mapper(componentModel = "spring")
public interface ParametreMapper {

    @Mapping(target = "idParametre", ignore = true)
    @Mapping(target = "version",     ignore = true)
    Parametre toEntity(ParametreDTO.CreateRequest req);

    ParametreDTO.Response toDto(Parametre parametre);
}

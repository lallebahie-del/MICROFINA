package com.pfe.backend.mapper;

import com.microfina.entity.Agence;
import com.pfe.backend.dto.GeoJsonDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * GeoMapper — conversion MapStruct entre {@link Agence} et
 * {@link GeoJsonDto.Feature} pour l'export GeoJSON de cartographie.
 */
@Mapper(componentModel = "spring")
public interface GeoMapper {

    @Mapping(target = "type",       constant = "Feature")
    @Mapping(target = "geometry",   ignore = true)
    @Mapping(target = "properties", ignore = true)
    GeoJsonDto.Feature toFeature(Agence agence);
}

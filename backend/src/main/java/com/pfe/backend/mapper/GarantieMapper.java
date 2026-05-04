package com.pfe.backend.mapper;

import com.microfina.entity.Garantie;
import com.pfe.backend.dto.GarantieDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * GarantieMapper — conversion MapStruct entre l'entité {@link Garantie}
 * et le DTO {@link GarantieDTO}.
 *
 * <p>L'association {@code typeGarantie} est aplatie :
 * {@code typeGarantie.code} → {@code codeTypeGarantie},
 * {@code typeGarantie.libelle} → {@code libelleTypeGarantie}.</p>
 *
 * <p>L'association {@code credit} est aplatie :
 * {@code credit.idCredit} → {@code idCredit},
 * {@code credit.numCredit} → {@code numCredit}.</p>
 *
 * <p>L'association {@code membreGarant} est aplatie :
 * {@code membreGarant.numMembre} → {@code numMembreGarant} (nullable).</p>
 *
 * <p>{@code componentModel = "spring"} : MapStruct génère un bean Spring
 * {@code @Component} injectables via {@code @Autowired}.</p>
 */
@Mapper(componentModel = "spring")
public interface GarantieMapper {

    @Mapping(source = "typeGarantie.code",      target = "codeTypeGarantie")
    @Mapping(source = "typeGarantie.libelle",   target = "libelleTypeGarantie")
    @Mapping(source = "credit.idCredit",        target = "idCredit")
    @Mapping(source = "credit.numCredit",       target = "numCredit")
    @Mapping(source = "membreGarant.numMembre", target = "numMembreGarant")
    GarantieDTO toDto(Garantie garantie);

    List<GarantieDTO> toDtoList(List<Garantie> garanties);
}

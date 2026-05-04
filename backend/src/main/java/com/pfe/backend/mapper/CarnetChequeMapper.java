package com.pfe.backend.mapper;

import com.microfina.entity.CarnetCheque;
import com.pfe.backend.dto.CarnetChequeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * CarnetChequeMapper — conversion MapStruct entre {@link CarnetCheque} et {@link CarnetChequeDTO}.
 *
 * <p>Les associations sont aplaties :
 * <ul>
 *   <li>{@code compteBanque.id} → {@code compteBanqueId}</li>
 *   <li>{@code membre.numMembre} → {@code numMembre}</li>
 * </ul>
 * L'énumération {@link com.microfina.entity.StatutCarnetCheque} est convertie
 * en {@code String} via {@code name()} implicitement par MapStruct.</p>
 */
@Mapper(componentModel = "spring")
public interface CarnetChequeMapper {

    /**
     * Convertit une {@link CarnetChequeDTO.CreateRequest} en entité {@link CarnetCheque}.
     * Les associations membre/compteBanque sont résolues dans le service.
     * L'identifiant est généré par la base de données.
     */
    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "membre",       ignore = true)
    @Mapping(target = "compteBanque", ignore = true)
    @Mapping(target = "version",      ignore = true)
    @Mapping(target = "statut",       ignore = true)
    @Mapping(target = "dateRemise",   ignore = true)
    CarnetCheque toEntity(CarnetChequeDTO.CreateRequest req);

    /**
     * Convertit une entité {@link CarnetCheque} en {@link CarnetChequeDTO.Response}.
     * Le statut (enum) est converti en String par MapStruct automatiquement.
     */
    @Mapping(source = "compteBanque.id",   target = "compteBanqueId")
    @Mapping(source = "membre.numMembre",  target = "numMembre")
    CarnetChequeDTO.Response toDto(CarnetCheque carnetCheque);
}

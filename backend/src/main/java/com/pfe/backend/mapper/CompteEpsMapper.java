package com.pfe.backend.mapper;

import com.microfina.entity.CompteEps;
import com.pfe.backend.dto.CompteEpsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * CompteEpsMapper — conversion MapStruct entre {@link CompteEps} et {@link CompteEpsDTO}.
 *
 * <p>Les associations {@code membre} et {@code agence} sont aplaties :
 * {@code membre.numMembre} → {@code numMembre},
 * {@code agence.codeAgence} → {@code codeAgence}.</p>
 *
 * <p>La {@link CompteEpsDTO.CreateRequest} ne contient pas les associations
 * (elles sont résolues via les repositories dans le service), donc les champs
 * {@code membre} et {@code agence} de l'entité sont ignorés lors du mapping
 * depuis la request.</p>
 */
@Mapper(componentModel = "spring")
public interface CompteEpsMapper {

    /**
     * Convertit une {@link CompteEpsDTO.CreateRequest} en entité {@link CompteEps}.
     * Les associations membre/agence et les flags sont gérés dans le service.
     */
    @Mapping(target = "membre",          ignore = true)
    @Mapping(target = "agence",          ignore = true)
    @Mapping(target = "version",         ignore = true)
    @Mapping(target = "bloque",          ignore = true)
    @Mapping(target = "ferme",           ignore = true)
    @Mapping(target = "exonere",         ignore = true)
    @Mapping(target = "montantBloque",   ignore = true)
    @Mapping(target = "montantDepot",    ignore = true)
    @Mapping(target = "smsDepotCheque",  ignore = true)
    @Mapping(target = "smsPrelevement",  ignore = true)
    @Mapping(target = "smsRetrait",      ignore = true)
    @Mapping(target = "smsVirementSalaire", ignore = true)
    @Mapping(target = "codeSms",         ignore = true)
    @Mapping(target = "encaisse",        ignore = true)
    @Mapping(target = "optionEpargne",   ignore = true)
    @Mapping(target = "prioritaire",     ignore = true)
    @Mapping(target = "detailDepot",     ignore = true)
    @Mapping(target = "numFraisSms",     ignore = true)
    @Mapping(target = "numComptabilite", ignore = true)
    @Mapping(target = "agios",           ignore = true)
    @Mapping(target = "montantMg",       ignore = true)
    @Mapping(target = "montantInteret",  ignore = true)
    @Mapping(target = "interets",        ignore = true)
    @Mapping(target = "taux",            ignore = true)
    @Mapping(target = "dateBloque",      ignore = true)
    @Mapping(target = "dateDebloquage",  ignore = true)
    @Mapping(target = "dateFermee",      ignore = true)
    @Mapping(target = "numCompteOld",    ignore = true)
    @Mapping(target = "rangCompte",      ignore = true)
    @Mapping(target = "codeMembre",      ignore = true)
    @Mapping(target = "compteEpsNew",    ignore = true)
    @Mapping(target = "numCompteBis",    ignore = true)
    @Mapping(target = "compteManuelle",  ignore = true)
    @Mapping(target = "codeProd",        ignore = true)
    @Mapping(target = "produitPartSociale", ignore = true)
    CompteEps toEntity(CompteEpsDTO.CreateRequest req);

    /**
     * Convertit une entité {@link CompteEps} en {@link CompteEpsDTO.Response}.
     * Les associations sont aplaties vers leurs clés naturelles.
     */
    @Mapping(source = "membre.numMembre",  target = "numMembre")
    @Mapping(source = "agence.codeAgence", target = "codeAgence")
    CompteEpsDTO.Response toDto(CompteEps compteEps);
}

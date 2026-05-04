package com.pfe.backend.mapper;

import com.microfina.entity.Budget;
import com.pfe.backend.dto.BudgetDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * BudgetMapper – conversion MapStruct entre l'entité {@link Budget}
 * et le DTO {@link BudgetDTO.Response}.
 *
 * <p>L'association {@code agence} est aplatie :
 * {@code agence.codeAgence} → {@code codeAgence}.</p>
 *
 * <p>{@code componentModel = "spring"} : MapStruct génère un bean Spring
 * injectable via le constructeur ou l'injection de type.</p>
 */
@Mapper(componentModel = "spring")
public interface BudgetMapper {

    /**
     * Convertit une entité {@link Budget} en {@link BudgetDTO.Response}.
     *
     * @param budget l'entité source
     * @return le DTO correspondant
     */
    @Mapping(source = "agence.codeAgence", target = "codeAgence")
    BudgetDTO.Response toResponse(Budget budget);

    /**
     * Convertit une liste d'entités {@link Budget} en liste de {@link BudgetDTO.Response}.
     *
     * @param budgets la liste des entités
     * @return la liste des DTOs
     */
    List<BudgetDTO.Response> toResponseList(List<Budget> budgets);
}

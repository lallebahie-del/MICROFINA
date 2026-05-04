package com.pfe.backend.mapper;

import com.microfina.entity.JournalAudit;
import com.pfe.backend.dto.JournalAuditDTO;
import org.mapstruct.Mapper;

/**
 * JournalAuditMapper — conversion MapStruct entre {@link JournalAudit} et
 * {@link JournalAuditDTO.Response} (lecture seule, pas de toEntity nécessaire).
 */
@Mapper(componentModel = "spring")
public interface JournalAuditMapper {

    JournalAuditDTO.Response toDto(JournalAudit audit);
}

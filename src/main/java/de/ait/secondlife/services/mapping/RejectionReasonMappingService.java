package de.ait.secondlife.services.mapping;

import de.ait.secondlife.domain.dto.RejectionReasonDto;
import de.ait.secondlife.domain.entity.RejectionReason;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface RejectionReasonMappingService {

    RejectionReasonDto toDto(RejectionReason entity);
}

package de.ait.secondlife.services.mapping;

import de.ait.secondlife.domain.dto.BidCreationDto;
import de.ait.secondlife.domain.entity.Bid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface BidMappingService {

    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    Bid toEntity(BidCreationDto dto);
}




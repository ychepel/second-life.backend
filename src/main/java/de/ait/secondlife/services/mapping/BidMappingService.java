package de.ait.secondlife.services.mapping;

import de.ait.secondlife.domain.dto.BidCreationDto;
import de.ait.secondlife.domain.dto.BidResponseDto;
import de.ait.secondlife.domain.entity.Bid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface BidMappingService {

    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    Bid toEntity(BidCreationDto dto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "offerId", source = "offer.id")
    @Mapping(target = "userNameShorted", expression = "java(java.lang.String.format(\"%s %s.\", bid.getUser().getFirstName(), bid.getUser().getLastName().substring(0, 1)))")
    @Mapping(target = "userEmail", source = "user.email")
    BidResponseDto toDto(Bid bid);
}





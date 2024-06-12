package de.ait.secondlife.services.mapping;

import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.dto.OfferResponseDto;
import de.ait.secondlife.domain.entity.Offer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OfferMappingService {

    @Mapping(target = "ownerId", source = "user.id")
    @Mapping(target = "winnerBidId", source = "winnerBid.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "status", expression = "java(offer.getStatus().getName().toString())")
    OfferResponseDto toDto(Offer offer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "category", ignore = true)
    Offer toEntity(OfferCreationDto dto);

}





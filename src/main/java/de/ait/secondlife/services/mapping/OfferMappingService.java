package de.ait.secondlife.services.mapping;

import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.dto.OfferResponseDto;
import de.ait.secondlife.domain.entity.Offer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface OfferMappingService {


    @Mapping(source = "user.id", target = "ownerId")
    @Mapping(source = "status.id", target = "statusId")
    @Mapping(source = "winnerBid.id", target = "winnerBidId")
    //TODO change the logic after creating the order status processing
    @Mapping(target = "endAt", expression = "java(offer.getCreatedAt().plusDays(offer.getAuctionDurationDays()))")
    OfferResponseDto toRequestDto(Offer offer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "status", ignore = true)
    Offer toOffer(OfferCreationDto dto);

}





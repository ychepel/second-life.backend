package de.ait.secondlife.services.mapping;

import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.dto.OfferResponseDto;
import de.ait.secondlife.domain.entity.Offer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public abstract class OfferMappingService extends EntityWIthImageMappingService {

    @Mapping(target = "ownerId", source = "user.id")
    @Mapping(target = "winnerBidId", source = "winnerBid.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "locationId", source = "location.id")
    //TODO change the logic after creating the order status processing
    @Mapping(target = "images", expression = "java(getImages(offer))")
    @Mapping(target = "status", expression = "java(offer.getOfferStatus().toString())")
    @Mapping(
            target = "auctionStartAt",
            expression = "java(offer.getAuctionFinishedAt() != null ? offer.getAuctionFinishedAt().minusDays(offer.getAuctionDurationDays()) : null)"
    )
    @Mapping(target = "auctionEndAt", source = "auctionFinishedAt")
    @Mapping(
            target = "ownerFullName",
            expression = "java(offer.getUser().getFirstName() + ' ' + offer.getUser().getLastName())"
    )
    @Mapping(target = "maxBidValue", expression = "java(offer.getMaxBidValue())")
    @Mapping(target = "bidsCount", expression = "java(offer.getBidsCount())")
    @Mapping(target = "isCurrentUserAuctionParticipant", expression = "java(offer.isCurrentUserAuctionParticipant())")
    public abstract OfferResponseDto toDto(Offer offer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "category", ignore = true)
    public abstract Offer toEntity(OfferCreationDto dto);

}





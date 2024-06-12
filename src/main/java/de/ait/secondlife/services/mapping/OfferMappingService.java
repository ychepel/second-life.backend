package de.ait.secondlife.services.mapping;

import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.dto.OfferResponseDto;
import de.ait.secondlife.domain.entity.Offer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class OfferMappingService extends EntityWIthImageMappingService {

    @Mapping(source = "user.id", target = "ownerId")
    @Mapping(source = "status.id", target = "statusId")
    @Mapping(source = "winnerBid.id", target = "winnerBidId")
    @Mapping(source = "category.id", target = "categoryId")
    //TODO change the logic after creating the order status processing
    @Mapping(target = "endAt", expression = "java(offer.getCreatedAt().plusDays(offer.getAuctionDurationDays()))")
    @Mapping(target = "images", expression = "java(getImages(offer))")
    @Mapping(source = "location.id", target = "locationId")
    @Mapping(target = "status", expression = "java(offer.getStatus().getName().toString())")
    @Mapping(
            target = "auctionStartAt",
            expression = "java(offer.getAuctionFinishedAt() != null ? offer.getAuctionFinishedAt().minusDays(offer.getAuctionDurationDays()) : null)"
    )
    @Mapping(target = "auctionEndAt", source = "auctionFinishedAt")
    @Mapping(
            target = "ownerFullName",
            expression = "java(offer.getUser().getFirstName() + ' ' + offer.getUser().getLastName())"
    )
    public abstract OfferResponseDto toDto(Offer offer);



    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "category", ignore = true)
    public abstract Offer toEntity(OfferCreationDto dto);

}





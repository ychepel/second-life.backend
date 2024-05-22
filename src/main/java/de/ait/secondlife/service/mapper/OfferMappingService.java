package de.ait.secondlife.service.mapper;

import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.dto.OfferRequestDto;
import de.ait.secondlife.domain.entity.Bid;
import de.ait.secondlife.domain.entity.Category;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.domain.entity.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OfferMappingService {

    @Mapping(source = "userId", target = "ownerId")
    @Mapping(source = "status.id", target = "statusId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "winnerBid.id", target = "winnerBidId")
    OfferRequestDto offertoOfferRequestDto(Offer offer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "winnerBid", ignore = true)
    Offer offerCreationDtotoOffer(OfferCreationDto dto);

}





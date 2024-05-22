package de.ait.secondlife.service.interfaces;

import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.dto.OfferRequestDto;
import de.ait.secondlife.domain.dto.OfferRequestWithPaginationDto;
import de.ait.secondlife.domain.dto.OfferUpdateDto;
import de.ait.secondlife.domain.entity.Offer;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OfferService {

OfferRequestDto createOffer(OfferCreationDto dto);

OfferRequestWithPaginationDto findOffers(Pageable pageable);

OfferRequestDto findOfferById(UUID id);

void updateOffer(OfferUpdateDto dto);

void removeOffer(UUID id);

void recoverOffer(UUID id);
}

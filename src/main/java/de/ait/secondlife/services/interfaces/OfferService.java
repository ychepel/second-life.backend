package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.dto.OfferResponseDto;
import de.ait.secondlife.domain.dto.OfferResponseWithPaginationDto;
import de.ait.secondlife.domain.dto.OfferUpdateDto;
import org.springframework.data.domain.Pageable;

public interface OfferService {

    OfferResponseDto createOffer(OfferCreationDto dto);

    OfferResponseWithPaginationDto findOffers(Pageable pageable);

    OfferResponseDto findOfferById(Long id);

    OfferResponseWithPaginationDto findOffersByUserId(Long id, Pageable pageable);

    void updateOffer(OfferUpdateDto dto);

    void removeOffer(Long id);

    void recoverOffer(Long id);

}

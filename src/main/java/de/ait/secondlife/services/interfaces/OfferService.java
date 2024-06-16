package de.ait.secondlife.services.interfaces;


import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.dto.*;
import de.ait.secondlife.domain.entity.Offer;
import org.springframework.data.domain.Pageable;

import javax.security.auth.login.CredentialException;
import java.util.List;
import java.util.Set;


public interface OfferService extends CheckEntityExistsService{

    OfferResponseDto createOffer(OfferCreationDto dto) throws CredentialException;

    OfferResponseWithPaginationDto findOffers(Pageable pageable, Long categoryId, String status, Boolean isFree);

    Offer findById(Long id);

    OfferResponseDto getDto(Long id);

    OfferResponseWithPaginationDto findOffersByUserId(Long id, Pageable pageable, Long categoryId, String status, Boolean isFree);

    OfferResponseDto updateOffer(OfferUpdateDto dto) throws CredentialException;

    void removeOffer(Long id);

    void recoverOffer(Long id);

    void setStatus(Offer offer, OfferStatus offerStatus);

    void setStatus(Offer offer, OfferStatus offerStatus, Long rejectionReasonId);

    List<Offer> findUnfinishedAuctions();

    void draftOffer(Offer offer);

    void rejectOffer(Long id, OfferRejectionDto offerRejectionDto);

    void verifyOffer(Offer offer);

    void startAuction(Long id);

    void finishAuction(Offer offer);

    void qualifyAuction(Long id);

    OfferResponseDto completeOffer(Long id, OfferCompletionDto offerCompletionDto);

    void cancelOffer(Long id);

    void blockOfferByAdmin(Long id);

    OfferResponseWithPaginationDto searchOffers(Pageable pageable, Long locationId, String pattern);

    OfferResponseWithPaginationDto findAllByUserBidByUserId(Long id, Pageable pageable, Long categoryId, String status, Boolean isFree, Set<OfferStatus> statuses);
}

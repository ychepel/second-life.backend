package de.ait.secondlife.services;

import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.dto.BidCreationDto;
import de.ait.secondlife.domain.dto.BidResponseDto;
import de.ait.secondlife.domain.dto.BidsResponseDto;
import de.ait.secondlife.domain.entity.Bid;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.exception_handling.exceptions.UserIsNotAuthorizedException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.BidCreationException;
import de.ait.secondlife.repositories.BidRepository;
import de.ait.secondlife.security.services.AuthService;
import de.ait.secondlife.services.interfaces.BidService;
import de.ait.secondlife.services.interfaces.OfferService;
import de.ait.secondlife.services.mapping.BidMappingService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.security.auth.login.CredentialException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for managing bids on offers.
 * This service provides methods for saving bids, retrieving bids by offer ID,
 * and various checks and validations related to bids and offers.
 *
 * <p>
 * This service interacts with the BidRepository, BidMappingService, OfferService,
 * and uses an EntityManager for transaction management.
 * </p>
 *
 * <p>
 * Exceptions that may be thrown by this class include:
 * <ul>
 *     <li>{@link CredentialException} - if there is an issue with user credentials</li>
 *     <li>{@link UserIsNotAuthorizedException} - if the user is not authorized to view bids</li>
 *     <li>{@link BidCreationException} - if there is an issue with creating a bid</li>
 * </ul>
 * </p>
 *
 * <p>
 * Author: Second Life Team
 * </p>
 *
 * @version 1.0
 * @author: Second Life Team
 */
@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final BidMappingService mappingService;
    private final OfferService offerService;
    private final EntityManager entityManager;

    /**
     * Retrieves a bid by its ID.
     *
     * @param id the ID of the bid to be retrieved.
     * @return the Bid object if found, or null if not found.
     */
    @Override
    public Bid getById(Long id) {
        return bidRepository.findById(id).orElse(null);
    }

    /**
     * Saves a new bid based on the provided BidCreationDto.
     *
     * @param dto the data transfer object containing bid creation details.
     * @throws CredentialException if there is an issue with user credentials.
     */
    @Transactional
    @Override
    public void save(BidCreationDto dto) throws CredentialException {
        Offer offer = offerService.findById(dto.getOfferId());

        checkOfferStatus(offer);

        BigDecimal newBidValue = dto.getBidValue();

        if (offer.getIsFree()) {
            checkFreeAuction(newBidValue);
        } else {
            checkNotFreeAuction(offer, newBidValue);
        }

        User user = AuthService.getCurrentUser();
        checkAuthentication(offer, user);

        Bid newBid = mappingService.toEntity(dto);
        newBid.setUser(user);
        newBid.setOffer(offer);
        bidRepository.save(newBid);

        if (isWinningBid(offer, newBidValue)) {
            entityManager.refresh(offer);
            offerService.finishAuction(offer);
        }
    }

    /**
     * Finds all bids for a specific offer by its ID and returns them as a BidsResponseDto.
     *
     * @param id the ID of the offer.
     * @return the BidsResponseDto containing the list of bids.
     * @throws CredentialException          if there is an issue with user credentials.
     * @throws UserIsNotAuthorizedException if the user is not authorized to view the list of bids.
     */
    @Override
    public BidsResponseDto findAllByOfferId(Long id) throws CredentialException {
        Offer offer = offerService.findById(id);
        User user = AuthService.getCurrentUser();
        if (!Objects.equals(offer.getUser().getId(), user.getId())) {
            throw new UserIsNotAuthorizedException("The non-owner is not authorized to view list of bids");
        }
        List<BidResponseDto> bids = offer.getBids()
                .stream()
                .collect(Collectors.groupingBy(Bid::getUser, Collectors.maxBy(Comparator.comparing(Bid::getId))))
                .values()
                .stream()
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(Bid::getId).reversed())
                .map(mappingService::toDto)
                .toList();
        BidsResponseDto response = new BidsResponseDto();
        response.setBids(bids);
        return response;
    }

    /**
     * Checks if the provided bid value is a winning bid for the specified offer.
     *
     * @param offer       the Offer object.
     * @param newBidValue the new bid value.
     * @return true if the bid is a winning bid, false otherwise.
     */
    private boolean isWinningBid(Offer offer, BigDecimal newBidValue) {
        if (offer.getIsFree()) {
            return false;
        }

        BigDecimal offerWinBid = offer.getWinBid();
        if (offerWinBid == null) {
            return false;
        }
        return newBidValue.compareTo(offerWinBid) == 0;
    }

    /**
     * Checks if the user is authenticated and authorized to place a bid on the specified offer.
     *
     * @param offer the Offer object.
     * @param user  the User object.
     * @throws BidCreationException if the user is the offer owner or has already placed a bid on a free offer.
     */
    private void checkAuthentication(Offer offer, User user) {
        if (Objects.equals(offer.getUser().getId(), user.getId())) {
            throw new BidCreationException("Bid cannot be created by offer owner");
        }
        if (offer.getIsFree()) {
            List<Long> auctionParticipantIds = offer.getBids()
                    .stream()
                    .map(bid -> bid.getUser().getId())
                    .toList();
            if (auctionParticipantIds.contains(user.getId())) {
                throw new BidCreationException("Bid cannot be created twice by one user in free auction");
            }
        }
    }

    /**
     * Checks the status of the offer to ensure it is in the AUCTION_STARTED state.
     *
     * @param offer the Offer object.
     * @throws BidCreationException if the offer is not in the AUCTION_STARTED state.
     */
    private void checkOfferStatus(Offer offer) {
        if (offer.getOfferStatus() != OfferStatus.AUCTION_STARTED) {
            throw new BidCreationException("Bid cannot be created for offer not in status AUCTION_STARTED");
        }
    }

    /**
     * Validates a bid for a free auction to ensure the bid value is zero.
     *
     * @param newBidValue the new bid value.
     * @throws BidCreationException if the bid value is greater than zero.
     */
    private void checkFreeAuction(BigDecimal newBidValue) {
        if (newBidValue.compareTo(BigDecimal.ZERO) > 0) {
            throw new BidCreationException("Bid with value greater than 0 cannot be created for the free offer");
        }
    }

    /**
     * Validates a bid for a non-free auction based on the current maximum bid and starting price.
     *
     * @param offer       the Offer object.
     * @param newBidValue the new bid value.
     * @throws BidCreationException if the bid value is invalid according to the auction rules.
     */
    private void checkNotFreeAuction(Offer offer, BigDecimal newBidValue) {
        BigDecimal existingMaxBidValue = offer.getMaxBidValue();
        if (existingMaxBidValue == null) {
            if (newBidValue.compareTo(offer.getStartPrice()) < 0) {
                throw new BidCreationException("Bid cannot be less than auction start price");
            }
        } else if (newBidValue.compareTo(existingMaxBidValue) <= 0) {
            throw new BidCreationException("Bid cannot be less or equal to current maximum bid value");
        }

        BigDecimal winBid = offer.getWinBid();
        if (winBid != null && newBidValue.compareTo(winBid) > 0) {
            throw new BidCreationException("Bid cannot be greater than buyout price (win bid value)");
        }
    }
}

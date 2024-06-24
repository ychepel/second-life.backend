package de.ait.secondlife.services;

import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.dto.*;
import de.ait.secondlife.domain.entity.Bid;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.exception_handling.exceptions.NoRightsException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.CreateOfferConstraintViolationException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.WrongAuctionParameterException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.WrongAuctionPriceParameterException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.is_null_exceptions.IdIsNullException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.OfferNotFoundException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.UserNotFoundException;
import de.ait.secondlife.repositories.OfferRepository;
import de.ait.secondlife.security.services.AuthService;
import de.ait.secondlife.services.interfaces.*;
import de.ait.secondlife.services.mapping.OfferMappingService;
import de.ait.secondlife.services.utilities.UserPermissionsUtilities;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.security.auth.login.CredentialException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the OfferService interface. (Version 1.0)
 * This service provides methods to manage offers including creation, retrieval,
 * update, and status management. It interacts with various services such as
 * OfferMappingService for mapping entities to DTOs, StatusService for managing
 * offer statuses, UserService for user-related operations, CategoryService for
 * category-related operations, LocationService for location-related operations,
 * and OfferStatusHistoryService for recording offer status changes.
 *
 * <p>
 * The service includes methods for creating new offers, updating existing offers,
 * managing offer statuses (draft, verify, start auction, complete, etc.), searching
 * for offers based on various criteria, and retrieving details such as winner information
 * and current user participation details in auctions.
 * </p>
 *
 * <p>
 * This class manages exceptions including:
 * <ul>
 *     <li>{@link IdIsNullException} - if ID is null</li>
 *     <li>{@link OfferNotFoundException} - if an offer with the specified ID is not found</li>
 *     <li>{@link CredentialException} - if there is an issue with user credentials</li>
 *     <li>{@link WrongAuctionParameterException} - if there is an issue with auction parameters</li>
 *     <li>{@link NoRightsException} - if the user does not have rights to perform the operation</li>
 * </ul>
 * to handle various error conditions during offer operations.
 * </p>
 *
 * <p>
 * Note: This service assumes proper configuration of dependencies such as {@link ImageService}
 * for managing images associated with offers, and {@link OfferContext} for handling offer state
 * transitions and business logic.
 * </p>
 *
 * <p>
 * Author: Second Life Team
 * </p>
 *
 * @author Second Life Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final OfferMappingService mappingService;
    private final StatusService statusService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final LocationService locationService;
    private final OfferStatusHistoryService offerStatusHistoryService;
    private final UserPermissionsUtilities utilities;
    @Lazy
    @Autowired
    private ImageService imageService;
    private final OfferContext offerContext;

    private Set<OfferStatus> STATUSES_FOR_BID_SEARCH = Set.of(
            OfferStatus.AUCTION_STARTED,
            OfferStatus.QUALIFICATION,
            OfferStatus.COMPLETED,
            OfferStatus.CANCELED,
            OfferStatus.BLOCKED_BY_ADMIN);

    /**
     * Finds offers based on specified criteria, such as category, status, and free status,
     * and returns them in paginated format.
     *
     * @param pageable   pagination information
     * @param categoryId category ID to filter offers
     * @param status     status to filter offers
     * @param isFree     whether the offers are free or not
     * @return OfferResponseWithPaginationDto containing offers matching the criteria
     */
    @Override
    public OfferResponseWithPaginationDto findOffers(
            Pageable pageable,
            Long categoryId,
            String status,
            Boolean isFree
    ) {
        OfferStatus offerStatus = status != null ? OfferStatus.get(status) : null;
        Page<Offer> pageOfOffer = offerRepository
                .findAll(categoryId, offerStatus, isFree, pageable);
        return offersToOfferRequestWithPaginationDto(pageOfOffer);
    }

    /**
     * Retrieves an offer by its ID.
     *
     * @param id ID of the offer to retrieve
     * @return Offer entity matching the ID
     * @throws IdIsNullException      if ID is null
     * @throws OfferNotFoundException if no offer found with the given ID
     */
    @Override
    public Offer findById(Long id) {
        if (id == null) throw new IdIsNullException();
        return offerRepository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException(id));
    }

    /**
     * Retrieves an offer DTO by its ID.
     *
     * @param id ID of the offer to retrieve
     * @return OfferResponseDto containing DTO representation of the offer
     * @throws IdIsNullException      if ID is null
     * @throws OfferNotFoundException if no offer found with the given ID
     */
    @Override
    public OfferResponseDto getDto(Long id) {
        Offer offer = findById(id);
        return mappingService.toDto(offer);
    }

    /**
     * Finds offers created by a specific user based on criteria such as category, status, and free status,
     * and returns them in paginated format.
     *
     * @param id         ID of the user who created the offers
     * @param pageable   pagination information
     * @param categoryId category ID to filter offers
     * @param status     status to filter offers
     * @param isFree     whether the offers are free or not
     * @return OfferResponseWithPaginationDto containing offers created by the user matching the criteria
     * @throws IdIsNullException if ID is null
     */
    @Override
    public OfferResponseWithPaginationDto findOffersByUserId(
            Long id,
            Pageable pageable,
            Long categoryId,
            String status,
            Boolean isFree) {
        checkUserId(id);
        OfferStatus offerStatus = status != null ? OfferStatus.get(status) : null;
        Page<Offer> pageOfOffer = offerRepository.findByUserId(id, categoryId, offerStatus, isFree, pageable);
        return offersToOfferRequestWithPaginationDto(pageOfOffer);
    }

    /**
     * Creates a new offer based on the provided DTO.
     *
     * @param dto DTO containing offer creation details
     * @return OfferResponseDto containing DTO representation of the created offer
     * @throws CredentialException                     if there is an issue with user credentials
     * @throws WrongAuctionParameterException          if there is an issue with auction parameters
     * @throws CreateOfferConstraintViolationException if there is a constraint violation during offer creation
     */
    @Transactional
    @Override
    public OfferResponseDto createOffer(OfferCreationDto dto) throws CredentialException {
        if (dto.getBaseNameOfImages() != null) {
            utilities.checkUserPermissionsForImageByBaseName(dto.getBaseNameOfImages());
        }

        User user = AuthService.getCurrentUser();
        try {
            Offer newOffer = mappingService.toEntity(dto);
            newOffer.setUser(user);
            newOffer.setCategory(categoryService.getCategoryById(dto.getCategoryId()));
            newOffer.setLocation(locationService.getLocationById(dto.getLocationId()));
            newOffer.setId(null);
            newOffer.setAuctionDurationDays(newOffer.getAuctionDurationDays() <= 0 ? 3 : newOffer.getAuctionDurationDays());

            if (Boolean.TRUE.equals(newOffer.getIsFree())) {
                checkOfferIfIsFree(dto.getStartPrice(), dto.getWinBid());
            } else {
                if (dto.getStartPrice() == null || dto.getStartPrice().compareTo(BigDecimal.ZERO) == 0)
                    throw new WrongAuctionParameterException("start price");
                if (dto.getWinBid() != null && dto.getWinBid().compareTo(BigDecimal.ZERO) == 0) {
                    throw new WrongAuctionParameterException("winBid");
                }
                if (dto.getWinBid() != null && dto.getWinBid().compareTo(dto.getStartPrice()) <= 0) {
                    throw new WrongAuctionPriceParameterException();
                }
            }
            offerRepository.save(newOffer);
            if (Boolean.TRUE.equals(dto.getSendToVerification())) {
                verifyOffer(newOffer);
            } else {
                draftOffer(newOffer);
            }
            imageService.connectTempImagesToEntity(
                    dto.getBaseNameOfImages(),
                    EntityTypeWithImages.OFFER.getType(),
                    newOffer.getId());
            return mappingService.toDto(newOffer);
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            throw new CreateOfferConstraintViolationException("Constraint violation: " + e.getMessage());
        }
    }

    /**
     * Updates an existing offer based on the provided DTO.
     *
     * @param dto DTO containing offer update details
     * @return OfferResponseDto containing DTO representation of the updated offer
     * @throws CredentialException            if there is an issue with user credentials
     * @throws NoRightsException              if the user does not have rights to update the offer
     * @throws OfferNotFoundException         if no offer found with the given ID
     * @throws WrongAuctionParameterException if there is an issue with auction parameters
     */
    @Transactional
    @Override
    public OfferResponseDto updateOffer(OfferUpdateDto dto) throws CredentialException {
        if (dto.getBaseNameOfImages() != null)
            utilities.checkUserPermissionsForImageByBaseName(dto.getBaseNameOfImages());
        User user = AuthService.getCurrentUser();
        Offer offer = offerRepository.findById(dto.getId())
                .orElseThrow(() -> new OfferNotFoundException(dto.getId()));
        if (!user.equals(offer.getUser()))
            throw new NoRightsException(String.format("User <%d> can't change this offer", user.getId()));

        offer.setTitle(dto.getTitle() == null ? offer.getTitle() : dto.getTitle());
        offer.setDescription(dto.getDescription() == null ? offer.getDescription() : dto.getDescription());
        offer.setAuctionDurationDays(dto.getAuctionDurationDays() == null || dto.getAuctionDurationDays() <= 0 ?
                offer.getAuctionDurationDays() : dto.getAuctionDurationDays());
        offer.setIsFree(dto.getIsFree() == null ? offer.getIsFree() : dto.getIsFree());
        if (offer.getIsFree()) {
            checkOfferIfIsFree(dto.getStartPrice(), dto.getWinBid());
            offer.setStartPrice(null);
            offer.setWinBid(null);
        } else {
            offer.setStartPrice(dto.getStartPrice() == null || dto.getStartPrice().compareTo(BigDecimal.ZERO) == 0 ?
                    offer.getStartPrice() : dto.getStartPrice());
            if (dto.getWinBid() != null && dto.getWinBid().compareTo(offer.getStartPrice()) <= 0) {
                throw new WrongAuctionPriceParameterException();
            }
            offer.setWinBid(dto.getWinBid() == null || dto.getWinBid().compareTo(BigDecimal.ZERO) == 0 ?
                    offer.getWinBid() : dto.getWinBid());
        }
        offer.setCategory(dto.getCategoryId() == null ?
                offer.getCategory() :
                categoryService.getCategoryById(dto.getCategoryId()));
        offer.setLocation(dto.getLocationId() == null ?
                offer.getLocation() :
                locationService.getLocationById(dto.getLocationId()));
        offer.setUpdatedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(dto.getSendToVerification())) {
            verifyOffer(offer);
        } else {
            draftOffer(offer);
        }
        imageService.connectTempImagesToEntity(
                dto.getBaseNameOfImages(),
                EntityTypeWithImages.OFFER.getType(),
                dto.getId());
        return mappingService.toDto(offer);
    }

    /**
     * Transitions an offer to the draft state.
     *
     * @param offer offer to transition to draft state
     */
    @Transactional
    @Override
    public void draftOffer(Offer offer) {
        offerContext.setOffer(offer);
        offerContext.draft();
    }

    /**
     * Rejects an offer based on the provided rejection details.
     *
     * @param id                ID of the offer to reject
     * @param offerRejectionDto DTO containing rejection details
     */
    @Transactional
    @Override
    public void rejectOffer(Long id, OfferRejectionDto offerRejectionDto) {
        offerContext.setOffer(getOfferById(id));
        offerContext.reject(offerRejectionDto.getRejectionReasonId());
    }

    /**
     * Verifies an offer, preparing it for auction or completion.
     *
     * @param offer offer to verify
     */
    @Transactional
    @Override
    public void verifyOffer(Offer offer) {
        offerContext.setOffer(offer);
        offerContext.verify();
    }

    /**
     * Starts an auction for the specified offer.
     *
     * @param id ID of the offer to start the auction for
     */
    @Transactional
    @Override
    public void startAuction(Long id) {
        offerContext.setOffer(getOfferById(id));
        offerContext.startAuction();
    }

    /**
     * Finishes an auction for the specified offer.
     *
     * @param offer offer to finish the auction for
     */
    @Transactional
    @Override
    public void finishAuction(Offer offer) {
        offerContext.setOffer(offer);
        offerContext.finishAuction();
    }

    /**
     * Completes an offer based on the provided completion details.
     *
     * @param id                 ID of the offer to complete
     * @param offerCompletionDto DTO containing completion details
     * @return OfferResponseDto containing DTO representation of the completed offer
     */
    @Transactional
    @Override
    public OfferResponseDto completeOffer(Long id, OfferCompletionDto offerCompletionDto) {
        Offer offer = getOfferById(id);
        utilities.checkUserPermissions(offer.getUser().getId());
        offerContext.setOffer(offer);
        offerContext.complete(offerCompletionDto.getWinnerBidId());
        return mappingService.toDto(offer);
    }

    /**
     * Cancels an offer based on the provided ID.
     *
     * @param id ID of the offer to cancel
     */
    @Transactional
    @Override
    public void cancelOffer(Long id) {
        offerContext.setOffer(getOfferById(id));
        offerContext.cancel();
    }

    /**
     * Blocks an offer by an admin based on the provided ID.
     *
     * @param id ID of the offer to block
     */
    @Transactional
    @Override
    public void blockOfferByAdmin(Long id) {
        offerContext.setOffer(getOfferById(id));
        offerContext.blockByAdmin();
    }

    /**
     * Searches offers based on location ID and search pattern.
     *
     * @param pageable   pagination information
     * @param locationId location ID to filter offers
     * @param pattern    search pattern to filter offers
     * @return OfferResponseWithPaginationDto containing offers matching the criteria
     */
    @Override
    public OfferResponseWithPaginationDto searchOffers(Pageable pageable, Long locationId, String pattern) {
        if (locationId != null) {
            try {
                User authenticatedUser = AuthService.getCurrentUser();
                userService.setLocation(authenticatedUser.getId(), locationId);
            } catch (CredentialException ignored) {
            }
        }

        Page<Offer> pageOfOffer = offerRepository.searchAll(OfferStatus.AUCTION_STARTED, pageable, locationId, pattern);
        return offersToOfferRequestWithPaginationDto(pageOfOffer);
    }

    /**
     * Finds offers in which the specified user is participating as a bidder,
     * based on criteria such as category, status, and free status,
     * and returns them in paginated format.
     *
     * @param id         ID of the user who is participating in offers
     * @param pageable   pagination information
     * @param categoryId category ID to filter offers
     * @param status     status to filter offers
     * @param isFree     whether the offers are free or not
     * @return OfferResponseWithPaginationDto containing offers in which the user is participating
     * @throws IdIsNullException if ID is null
     */
    @Override
    public OfferResponseWithPaginationDto findUserAuctionParticipations(
            Long id,
            Pageable pageable,
            Long categoryId,
            String status,
            Boolean isFree) {

        checkUserId(id);
        utilities.checkUserPermissions(id);
        OfferStatus offerStatus = status != null ? OfferStatus.get(status) : null;

        Page<Offer> pageOfOffer = offerRepository.findUserAuctionParticipations(
                id, categoryId, offerStatus, isFree,
                STATUSES_FOR_BID_SEARCH, pageable);

        return offersToOfferRequestWithPaginationDto(pageOfOffer);
    }

    /**
     * Finds the owner ID of the offer with the specified ID.
     *
     * @param id ID of the offer
     * @return ID of the owner of the offer
     * @throws IdIsNullException if ID is null
     */
    @Override
    public Long findOwnerIdByOfferId(Long id) {
        return findById(id).getUser().getId();
    }

    /**
     * Sets the status of the specified offer.
     *
     * @param offer       offer to set the status for
     * @param offerStatus status to set
     */
    @Transactional
    @Override
    public void setStatus(Offer offer, OfferStatus offerStatus) {
        offerStatusHistoryService.create(offer, offerStatus);
        offer.setStatus(statusService.getByOfferStatus(offerStatus));
        offer.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Sets the status of the specified offer with rejection reason.
     *
     * @param offer             offer to set the status for
     * @param offerStatus       status to set
     * @param rejectionReasonId ID of the rejection reason
     */
    @Transactional
    @Override
    public void setStatus(Offer offer, OfferStatus offerStatus, Long rejectionReasonId) {
        offerStatusHistoryService.create(offer, offerStatus, rejectionReasonId);
        offer.setStatus(statusService.getByOfferStatus(offerStatus));
        offer.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Finds unfinished auctions based on the current date and auction started status.
     *
     * @return List of offers that are unfinished auctions
     */
    @Override
    public List<Offer> findUnfinishedAuctions() {
        return offerRepository.findFinishedAuctions(
                LocalDateTime.now(),
                OfferStatus.AUCTION_STARTED
        );
    }

    /**
     * Checks if an entity with the specified ID exists.
     *
     * @param id ID of the entity to check
     * @return true if the entity exists, false otherwise
     * @throws IdIsNullException if ID is null
     */
    @Override
    public boolean checkEntityExistsById(Long id) {
        if (id == null) throw new IdIsNullException();
        return offerRepository.existsById(id);
    }

    /**
     * Checks if the current user is a participant in the specified offer's auction.
     *
     * @param offer offer to check participation for
     * @return true if the current user is a participant, false otherwise
     */
    @Override
    public boolean isCurrentUserAuctionParticipant(Offer offer) {
        try {
            User user = AuthService.getCurrentUser();
            List<Bid> bids = offer.getBids();
            if (bids == null) {
                return false;
            }
            List<Long> participantIds = bids.stream()
                    .map(Bid::getUser)
                    .map(User::getId)
                    .toList();
            return participantIds.contains(user.getId());
        } catch (CredentialException e) {
            return false;
        }
    }

    /**
     * Retrieves a list of users who did not win the auction for the specified offer.
     *
     * @param offer offer to retrieve non-winners for
     * @return List of users who did not win the auction
     */
    @Override
    public List<User> getNotWinners(Offer offer) {

        List<Bid> bidList = offer.getBids();

        if (bidList != null) {
            return offer.getBids()
                    .stream()
                    .filter(bid -> !Objects.equals(bid.getId(), offer.getWinnerBid().getId()))
                    .map(Bid::getUser)
                    .toList();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves a list of users who participated in the auction for the specified offer.
     *
     * @param offer offer to retrieve participants for
     * @return List of users who participated in the auction
     */
    @Override
    public List<User> getParticipants(Offer offer) {

        List<Bid> bidList = offer.getBids();

        if (bidList != null) {
            return offer.getBids()
                    .stream()
                    .map(Bid::getUser)
                    .toList();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves details of the winner of the auction for the specified offer.
     *
     * @param offer offer to retrieve winner details for
     * @return OfferWinnerDto containing details of the auction winner
     */
    @Override
    public OfferWinnerDto getWinnerDetails(Offer offer) {
        OfferWinnerDto offerWinnerDto = new OfferWinnerDto();
        Bid winnerBid = offer.getWinnerBid();
        if (winnerBid == null) {
            return offerWinnerDto;
        }

        offerWinnerDto.setBidId(winnerBid.getId());
        offerWinnerDto.setBidValue(winnerBid.getBidValue());

        User winner = winnerBid.getUser();
        try {
            utilities.checkUserPermissions(offer.getUser().getId());
            offerWinnerDto.setNameShorted(winner.getShortedName());
            offerWinnerDto.setEmail(winner.getEmail());
        } catch (NoRightsException ignored) {
        }

        return offerWinnerDto;
    }

    /**
     * Retrieves details of the current user's participation in the auction for the specified offer.
     *
     * @param offer offer to retrieve current user details for
     * @return OfferForUserDto containing details of the current user's participation
     */
    @Override
    public OfferForUserDto getCurrentUserDetails(Offer offer) {
        OfferForUserDto userDto = new OfferForUserDto();
        userDto.setIsAuctionParticipant(false);
        userDto.setIsWinner(false);

        List<Bid> bids = offer.getBids();
        if (bids != null) {
            try {
                User user = AuthService.getCurrentUser();
                BigDecimal maxBidValue = bids.stream()
                        .filter(bid -> Objects.equals(bid.getUser().getId(), user.getId()))
                        .max(Comparator.comparing(Bid::getBidValue))
                        .map(Bid::getBidValue)
                        .orElse(null);
                if (maxBidValue != null) {
                    userDto.setIsAuctionParticipant(true);
                    userDto.setMaxBidValue(maxBidValue);
                }
                if (offer.getOfferStatus() == OfferStatus.COMPLETED
                        && maxBidValue != null
                        && offer.getWinnerBid() != null
                        && offer.getWinnerBid().getBidValue().compareTo(maxBidValue) == 0) {
                    userDto.setIsWinner(true);
                }
            } catch (CredentialException ignored) {
            }
        }
        return userDto;
    }

    /**
     * Retrieves the offer specified by the ID from the repository.
     *
     * @param id ID of the offer to retrieve
     * @return Offer entity corresponding to the ID
     * @throws OfferNotFoundException if no offer found with the given ID
     * @throws IdIsNullException      if ID is null
     */
    private Offer getOfferById(Long id) {
        return offerRepository.findById(id).orElseThrow(() -> new OfferNotFoundException(id));
    }

    /**
     * Converts a Page of Offer entities into OfferResponseWithPaginationDto.
     *
     * @param pageOfOffer Page containing offers to convert
     * @return OfferResponseWithPaginationDto containing converted offers
     */
    private OfferResponseWithPaginationDto offersToOfferRequestWithPaginationDto(Page<Offer> pageOfOffer) {
        Set<OfferResponseDto> offers;
        offers = pageOfOffer.stream()
                .map(mappingService::toDto)
                .collect(Collectors.toSet());
        return OfferResponseWithPaginationDto.builder()
                .offers(offers)
                .pageNumber(pageOfOffer.getNumber())
                .pageSize(pageOfOffer.getSize())
                .totalPages(pageOfOffer.getTotalPages())
                .totalElements(pageOfOffer.getTotalElements())
                .isFirstPage(pageOfOffer.isFirst())
                .isLastPage(pageOfOffer.isLast())
                .build();
    }

    /**
     * Helper method to validate auction parameters when the offer is free.
     *
     * @param startPrice start price of the offer
     * @param winBin     winning bid value
     * @throws WrongAuctionParameterException if there is an issue with auction parameters
     */
    private void checkOfferIfIsFree(BigDecimal startPrice, BigDecimal winBin) {
        if (startPrice != null && startPrice.compareTo(BigDecimal.ZERO) > 0)
            throw new WrongAuctionParameterException("start prise");
        if (winBin != null && winBin.compareTo(BigDecimal.ZERO) > 0) {
            throw new WrongAuctionParameterException("winBid");
        }
    }

    /**
     * Checks if a user with the specified ID exists.
     *
     * @param id ID of the user to check
     * @throws IdIsNullException     if ID is null
     * @throws UserNotFoundException if no user found with the given ID
     */
    private void checkUserId(Long id) {
        if (id == null) throw new IdIsNullException();
        if (!userService.checkEntityExistsById(id)) throw new UserNotFoundException(id);
    }
}

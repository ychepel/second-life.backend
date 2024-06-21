package de.ait.secondlife.services;

import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.dto.OfferResponseDto;
import de.ait.secondlife.domain.dto.OfferResponseWithPaginationDto;
import de.ait.secondlife.domain.dto.OfferUpdateDto;

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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private  ImageService imageService;
    private final OfferContext offerContext;

    private Set<OfferStatus> STATUSES_FOR_BID_SEARCH = Set.of(
            OfferStatus.AUCTION_STARTED,
            OfferStatus.QUALIFICATION,
            OfferStatus.COMPLETED,
            OfferStatus.CANCELED,
            OfferStatus.BLOCKED_BY_ADMIN);

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

    @Override
    public Offer findById(Long id) {
        if (id == null) throw new IdIsNullException();
        return offerRepository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException(id));
    }

    @Override
    public OfferResponseDto getDto(Long id) {
        Offer offer = findById(id);
        return mappingService.toDto(offer);
    }

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

    @Transactional
    @Override
    public OfferResponseDto createOffer(OfferCreationDto dto) throws CredentialException {
        utilities.checkUserPermissionsForImageByBaseName(dto.getBaseNameOfImages());

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

    @Transactional
    @Override
    public OfferResponseDto updateOffer(OfferUpdateDto dto) throws CredentialException {
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

    @Transactional
    @Override
    public void draftOffer(Offer offer) {
        offerContext.setOffer(offer);
        offerContext.draft();
    }

    @Transactional
    @Override
    public void rejectOffer(Long id, OfferRejectionDto offerRejectionDto) {
        offerContext.setOffer(getOfferById(id));
        offerContext.reject(offerRejectionDto.getRejectionReasonId());
    }

    @Transactional
    @Override
    public void verifyOffer(Offer offer) {
        offerContext.setOffer(offer);
        offerContext.verify();
    }

    @Transactional
    @Override
    public void startAuction(Long id) {
        offerContext.setOffer(getOfferById(id));
        offerContext.startAuction();
    }

    @Transactional
    @Override
    public void finishAuction(Offer offer) {
        offerContext.setOffer(offer);
        offerContext.finishAuction();
    }

    @Transactional
    @Override
    public OfferResponseDto completeOffer(Long id, OfferCompletionDto offerCompletionDto) {
        Offer offer = getOfferById(id);
        offerContext.setOffer(offer);
        offerContext.complete(offerCompletionDto.getWinnerBidId());
        return mappingService.toDto(offer);
    }

    @Transactional
    @Override
    public void cancelOffer(Long id) {
        offerContext.setOffer(getOfferById(id));
        offerContext.cancel();
    }

    @Transactional
    @Override
    public void blockOfferByAdmin(Long id) {
        offerContext.setOffer(getOfferById(id));
        offerContext.blockByAdmin();
    }

    @Override
    public OfferResponseWithPaginationDto searchOffers(Pageable pageable, Long locationId, String pattern) {
        if (locationId != null) {
            try {
                User authenticatedUser = AuthService.getCurrentUser();
                userService.setLocation(authenticatedUser.getId(), locationId);
            } catch (CredentialException ignored) {}
        }

        Page<Offer> pageOfOffer = offerRepository.searchAll(OfferStatus.AUCTION_STARTED, pageable, locationId, pattern);
        return offersToOfferRequestWithPaginationDto(pageOfOffer);
    }

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

    @Override
    public Long findOwnerIdByOfferId(Long id) {
        return findById(id).getUser().getId();
    }

    @Transactional
    @Override
    public void setStatus(Offer offer, OfferStatus offerStatus) {
        offerStatusHistoryService.create(offer, offerStatus);
        offer.setStatus(statusService.getByOfferStatus(offerStatus));
        offer.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    @Override
    public void setStatus(Offer offer, OfferStatus offerStatus, Long rejectionReasonId) {
        offerStatusHistoryService.create(offer, offerStatus, rejectionReasonId);
        offer.setStatus(statusService.getByOfferStatus(offerStatus));
        offer.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    public List<Offer> findUnfinishedAuctions() {
        return offerRepository.findFinishedAuctions(
                LocalDateTime.now(),
                OfferStatus.AUCTION_STARTED
        );
    }

    @Override
    public boolean checkEntityExistsById(Long id) {
        if (id == null) throw new IdIsNullException();
        return offerRepository.existsById(id);
    }

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

    private Offer getOfferById(Long id) {
        return offerRepository.findById(id).orElseThrow(() -> new OfferNotFoundException(id));
    }

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

    private void checkOfferIfIsFree(BigDecimal startPrice, BigDecimal winBin) {
        if (startPrice != null && startPrice.compareTo(BigDecimal.ZERO) > 0)
            throw new WrongAuctionParameterException("start prise");
        if (winBin != null && winBin.compareTo(BigDecimal.ZERO) > 0) {
            throw new WrongAuctionParameterException("winBid");
        }
    }

    private void checkUserId(Long id) {
        if (id == null) throw new IdIsNullException();
        if (!userService.checkEntityExistsById(id)) throw new UserNotFoundException(id);
    }
}

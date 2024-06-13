package de.ait.secondlife.services;

import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.dto.OfferResponseDto;
import de.ait.secondlife.domain.dto.OfferResponseWithPaginationDto;
import de.ait.secondlife.domain.dto.OfferUpdateDto;

import de.ait.secondlife.domain.dto.*;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.exception_handling.exceptions.NoRightToChangeException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.CreateOfferConstraintViolationException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.WrongAuctionParameterException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.is_null_exceptions.IdIsNullException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.OfferNotFoundException;
import de.ait.secondlife.repositories.OfferRepository;
import de.ait.secondlife.repositories.StatusRepository;
import de.ait.secondlife.services.interfaces.*;
import de.ait.secondlife.services.mapping.OfferMappingService;
import de.ait.secondlife.services.offer_status.OfferContext;
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

    private OfferContext offerContext;

    @Autowired
    public void setOfferContext(@Lazy OfferContext offerContext) {
        this.offerContext = offerContext;
    }

    private final ImageService imageService;

    @Override
    public OfferResponseWithPaginationDto findOffers(Pageable pageable) {
        Page<Offer> pageOfOffer = offerRepository.findAllByIsActiveTrue(pageable);
        return offersToOfferRequestWithPaginationDto(pageOfOffer);
    }

    @Override
    public OfferResponseDto findOfferById(Long id) {
        if (id == null) throw new IdIsNullException();
        Offer offer = offerRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new OfferNotFoundException(id));
        return mappingService.toDto(offer);
    }

    @Override
    public OfferResponseWithPaginationDto findOffersByUserId(Long id, Pageable pageable) {
        if (id == null) throw new IdIsNullException();
        Page<Offer> pageOfOffer = offerRepository.findByUserIdAndIsActiveTrue(id, pageable);
        return offersToOfferRequestWithPaginationDto(pageOfOffer);
    }

    @Transactional
    @Override
    public OfferResponseDto createOffer(OfferCreationDto dto) throws CredentialException {
        User user = userService.getAuthenticatedUser();
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
                    throw new WrongAuctionParameterException("start prise");
                if (dto.getWinBid() != null && dto.getWinBid().compareTo(BigDecimal.ZERO) == 0) {
                    throw new WrongAuctionParameterException("winBid");
                }
            }
            newOffer = offerRepository.save(newOffer);
            if (Boolean.TRUE.equals(dto.getSendToVerification())) {
                verifyOffer(newOffer);
            } else {
                draftOffer(newOffer);
            }
            String message = imageService
                    .connectTempImagesToEntity(
                            dto.getBaseNameOfImages(),
                            EntityTypeWithImages.OFFER.getType(),
                            newOffer.getId());
            OfferResponseDto offerResponseDto = mappingService.toDto(newOffer);
            offerResponseDto.setImageUploadInfo(message);
            return offerResponseDto;
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            throw new CreateOfferConstraintViolationException("Constraint violation: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public OfferResponseDto updateOffer(OfferUpdateDto dto) throws CredentialException {
        User user = userService.getAuthenticatedUser();
        Offer offer = offerRepository.findById(dto.getId())
                .orElseThrow(() -> new OfferNotFoundException(dto.getId()));
        if (!user.equals(offer.getUser()))
            throw new NoRightToChangeException(String.format("User <%d> can't change this offer", user.getId()));

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
        String message = imageService
                .connectTempImagesToEntity(
                        dto.getBaseNameOfImages(),
                        EntityTypeWithImages.OFFER.getType(),
                        dto.getId());
        OfferResponseDto offerResponseDto = mappingService.toDto(offer);
        offerResponseDto.setImageUploadInfo(message);
        return offerResponseDto;
    }

    @Transactional
    @Override
    public void removeOffer(Long id) {
        //TODO   Only the owner and admin has the right to make changes
        if (id == null) throw new IdIsNullException();
        Offer offer = offerRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new OfferNotFoundException(id));
        offer.setIsActive(false);
    }

    @Transactional
    @Override
    public void recoverOffer(Long id) {
        //TODO   Only the owner and admin has the right to make changes
        if (id == null) throw new IdIsNullException();
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException(id));
        offer.setIsActive(true);
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
    public void qualifyAuction(Long id) {
        offerContext.setOffer(getOfferById(id));
        offerContext.qualify();
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
        return offerRepository.findFinishedActiveAuctions(
                LocalDateTime.now(),
                OfferStatus.AUCTION_STARTED
        );
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

    @Override
    public boolean checkEntityExistsById(Long id) {
        if (id == null) throw new IdIsNullException();
        return offerRepository.existsByIdAndIsActiveTrue(id);
    }
}

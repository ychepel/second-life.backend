package de.ait.secondlife.services;

import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.dto.OfferResponseDto;
import de.ait.secondlife.domain.dto.OfferResponseWithPaginationDto;
import de.ait.secondlife.domain.dto.OfferUpdateDto;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.exception_handling.exceptions.NoRightToChangeException;
import de.ait.secondlife.exception_handling.exceptions.UserIsNotAuthorizedException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.CreateOfferConstraintViolationException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.WrongAuctionParameterException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.is_null_exceptions.IdIsNullException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.OfferNotFoundException;
import de.ait.secondlife.repositories.OfferRepository;
import de.ait.secondlife.services.interfaces.*;
import de.ait.secondlife.services.mapping.OfferMappingService;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        return mappingService.toRequestDto(offer);
    }

    @Override
    public OfferResponseWithPaginationDto findOffersByUserId(Long id, Pageable pageable) {
        if (id == null) throw new IdIsNullException();
        Page<Offer> pageOfOffer = offerRepository.findByUserIdAndIsActiveTrue(id, pageable);
        return offersToOfferRequestWithPaginationDto(pageOfOffer);
    }

    @Override
    public OfferResponseDto createOffer(OfferCreationDto dto) {
        User user = getUserFromAuthContext();
        try {
            Offer newOffer = mappingService.toOffer(dto);
            newOffer.setUser(user);
            newOffer.setCategory(categoryService.getCategoryById(dto.getCategoryId()));
            newOffer.setId(null);
            newOffer.setStatus(statusService.getStatusByName(OfferStatus.DRAFT.name()));
            newOffer.setAuctionDurationDays(newOffer.getAuctionDurationDays() <= 0 ? 3 : newOffer.getAuctionDurationDays());

            if (Boolean.TRUE.equals(newOffer.getIsFree())) {
                checkOfferIfIsFree(dto.getStartPrice(), dto.getStep(), dto.getWinBid());
            } else {
                if (dto.getStartPrice() == null || dto.getStartPrice().compareTo(BigDecimal.ZERO) == 0)
                    throw new WrongAuctionParameterException("start prise");
                if (dto.getStep() == null || dto.getStep().compareTo(BigDecimal.ZERO) == 0)
                    throw new WrongAuctionParameterException("step");
                if (dto.getWinBid() != null && dto.getWinBid().compareTo(BigDecimal.ZERO) == 0) {
                    throw new WrongAuctionParameterException("winBid");
                }
            }
            newOffer = offerRepository.save(newOffer);
            return mappingService.toRequestDto(newOffer);
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            throw new CreateOfferConstraintViolationException("Constraint violation: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void updateOffer(OfferUpdateDto dto) {
        User user = getUserFromAuthContext();
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
            checkOfferIfIsFree(dto.getStartPrice(), dto.getStep(), dto.getWinBid());
            offer.setStartPrice(null);
            offer.setStep(null);
            offer.setWinBid(null);
        } else {
            offer.setStartPrice(dto.getStartPrice() == null || dto.getStartPrice().compareTo(BigDecimal.ZERO) == 0 ?
                    offer.getStartPrice() : dto.getStartPrice());
            offer.setStep(dto.getStep() == null || dto.getStep().compareTo(BigDecimal.ZERO) == 0 ?
                    offer.getStep() : dto.getStep());
            offer.setWinBid(dto.getWinBid() == null || dto.getWinBid().compareTo(BigDecimal.ZERO) == 0 ?
                    offer.getWinBid() : dto.getWinBid());
        }
        offer.setCategory(dto.getCategoryId() == null ?
                offer.getCategory() :
                categoryService.getCategoryById(dto.getCategoryId()));
        offer.setLocation(dto.getLocationId() == null ?
                offer.getLocation() :
                locationService.getLocationById(dto.getLocationId()));
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

    private OfferResponseWithPaginationDto offersToOfferRequestWithPaginationDto(Page<Offer> pageOfOffer) {
        Set<OfferResponseDto> offers;
        offers = pageOfOffer.stream()
                .map(mappingService::toRequestDto)
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

    private void checkOfferIfIsFree(BigDecimal startPrice, BigDecimal step, BigDecimal winBin) {
        if (startPrice != null && startPrice.compareTo(BigDecimal.ZERO) > 0)
            throw new WrongAuctionParameterException("start prise");
        if (step != null && step.compareTo(BigDecimal.ZERO) > 0)
            throw new WrongAuctionParameterException("step");
        if (winBin != null && winBin.compareTo(BigDecimal.ZERO) > 0) {
            throw new WrongAuctionParameterException("winBid");
        }
    }

    @SneakyThrows
    private User getUserFromAuthContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal().equals("anonymousUser")) throw new UserIsNotAuthorizedException();
        String username = authentication.getName();
        return (User) userService.loadUserByUsername(username);
    }
}
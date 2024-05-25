package de.ait.secondlife.service;

import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.dto.OfferResponseDto;
import de.ait.secondlife.domain.dto.OfferResponseWithPaginationDto;
import de.ait.secondlife.domain.dto.OfferUpdateDto;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.exceptionHandler.exeptions.*;
import de.ait.secondlife.repository.OfferRepository;
import de.ait.secondlife.service.interfaces.OfferService;
import de.ait.secondlife.service.interfaces.StatusSevice;
import de.ait.secondlife.service.mapper.OfferMappingService;
import de.ait.secondlife.service.validator.EntityValidator;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.ait.secondlife.constans.StatusConstans.DRAFT_STATUS;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final OfferMappingService mappingService;
    private final StatusSevice statusSevice;
    private final EntityValidator validator;


    @Override
    public OfferResponseWithPaginationDto findOffers(Pageable pageable) {
        if (pageable == null) throw new PageableIsNullException();

        Page<Offer> pageOfOffer = offerRepository.findAllByIsActiveTrue(pageable);

        return offersToOfferRequestWithPaginationDto(pageOfOffer);
    }


    @Override
    public OfferResponseDto findOfferById(UUID id) {
        if (id == null) throw new IdIsNullException();

        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException(id));

        try {
            return mappingService.toRequestDto(offer);
        } catch (Exception e) {
            throw new MappingException(offer, e.getMessage());
        }
    }

    @Override
    public OfferResponseWithPaginationDto findOffersByUserId(Long id, Pageable pageable) {
        if (id == null) throw new IdIsNullException();
        Page<Offer> pageOfOffer = offerRepository.findByUserIdAndIsActiveTrue(id, pageable);

        return offersToOfferRequestWithPaginationDto(pageOfOffer);
    }


    @Override
    public OfferResponseDto createOffer(OfferCreationDto dto) {

        if (dto == null) throw new OfferCreationDtoIsNullException();

        try {
            Offer newOffer;
            try {
                newOffer = mappingService.toOffer(dto);
            } catch (Exception e) {
                throw new MappingException(dto, e.getMessage());
            }
            newOffer.setId(null);
            newOffer.setStatus(statusSevice.getStatusByName(DRAFT_STATUS));
            newOffer.setAuctionDurationDays(newOffer.getAuctionDurationDays() <= 0 ? 3 : newOffer.getAuctionDurationDays());

            if (dto.getIsFree() == null) {
                newOffer.setIsFree(dto.getStartPrice() == null || dto.getStartPrice().compareTo(BigDecimal.ZERO) <= 0);
            }


            if (Boolean.TRUE.equals(newOffer.getIsFree())) {
                newOffer.setStartPrice(null);
                newOffer.setStep(null);
                newOffer.setWinBid(null);
            } else {
                if (dto.getStartPrice() == null || dto.getStartPrice().compareTo(BigDecimal.ZERO) <= 0)
                    throw new WrongAuctionParameterException("start prise");
                if (dto.getStep() == null || dto.getStep().compareTo(BigDecimal.ZERO) <= 0)
                    throw new WrongAuctionParameterException("step");
                if (dto.getWinBid() != null && dto.getWinBid().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new WrongAuctionParameterException("winBid");
                }
            }

            validator.validateEntity(newOffer);

            newOffer = offerRepository.save(newOffer);
            try {
                return mappingService.toRequestDto(newOffer);
            } catch (Exception e) {
                throw new MappingException(newOffer, e.getMessage());
            }

        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            throw new CreateOfferConstraintViolationException("Constraint violation: " + e.getMessage());
        } catch (WrongAuctionParameterException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new DataBaseException("Error when accessing the database : " + e.getClass() + "  " + e.getMessage());
        }

    }

    @Transactional
    @Override
    public void updateOffer(OfferUpdateDto dto) {
        if (dto == null) throw new OfferUpdateDtoIsNullException();
        Offer offer = offerRepository.findById(dto.getId())
                .orElseThrow(() -> new OfferNotFoundException(dto.getId()));


        offer.setTitle(dto.getTitle() == null ? offer.getTitle() : dto.getTitle());
        offer.setDescription(dto.getDescription() == null ? offer.getDescription() : dto.getDescription());

        offer.setAuctionDurationDays(dto.getAuctionDurationDays() == null || dto.getAuctionDurationDays() <= 0 ?
                offer.getAuctionDurationDays() : dto.getAuctionDurationDays());

        offer.setIsFree(dto.getIsFree() == null ? offer.getIsFree() : dto.getIsFree());

        if (offer.getIsFree()) {
            offer.setStartPrice(null);
            offer.setStep(null);
            offer.setWinBid(null);
        } else {
            offer.setStartPrice(dto.getStartPrice() == null || dto.getStartPrice().compareTo(BigDecimal.ZERO) <= 0 ?
                    offer.getStartPrice() : dto.getStartPrice());

            offer.setStep(dto.getStep() == null || dto.getStep().compareTo(BigDecimal.ZERO) <= 0 ?
                    offer.getStep() : dto.getStep());

            offer.setWinBid(dto.getWinBid() == null || dto.getWinBid().compareTo(BigDecimal.ZERO) <= 0 ?
                    offer.getWinBid() : dto.getWinBid());
        }

        validator.validateEntity(offer);
    }



    @Transactional
    @Override
    public void removeOffer(UUID id) {
        if (id == null) throw new IdIsNullException();

        Offer offer = offerRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new OfferNotFoundException(id));
        offer.setIsActive(false);
    }

    @Transactional
    @Override
    public void recoverOffer(UUID id) {
        if (id == null) throw new IdIsNullException();

        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException(id));
        offer.setIsActive(true);

    }

    private OfferResponseWithPaginationDto offersToOfferRequestWithPaginationDto(Page<Offer> pageOfOffer) {
        if (pageOfOffer.isEmpty()) throw new OffersNotFoundException();

        Set<OfferResponseDto> offers;
        try {
            offers = pageOfOffer.stream()
                    .map(mappingService::toRequestDto)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            throw new MappingException(e.getMessage());
        }

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


}

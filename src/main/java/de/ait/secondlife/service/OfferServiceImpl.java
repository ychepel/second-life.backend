package de.ait.secondlife.service;

import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.dto.OfferRequestDto;
import de.ait.secondlife.domain.dto.OfferRequestWithPaginationDto;
import de.ait.secondlife.domain.dto.OfferUpdateDto;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.exceptionHandler.exeptions.*;
import de.ait.secondlife.repository.OfferRepository;
import de.ait.secondlife.service.interfaces.OfferService;
import de.ait.secondlife.service.interfaces.StatusSevice;
import de.ait.secondlife.service.mapper.OfferMappingService;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static de.ait.secondlife.constans.StatusConstans.DRAFT_STATUS;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final OfferMappingService mappingService;
    private final StatusSevice statusSevice;


    @Override
    public OfferRequestDto createOffer(OfferCreationDto dto) {

        if (dto == null) throw new OfferCreationDtoIsNullException();
        Offer newOffer;
        try {

            newOffer = mappingService.toOffer(dto);
            newOffer.setId(null);
            newOffer.setStatus(statusSevice.getStatusByName(DRAFT_STATUS));
            //TODO -add category by id
            newOffer.setCategory(null);
            //TODO
            newOffer.setWinBid(null);

            newOffer = offerRepository.save(newOffer);

        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            throw new CreateOfferConstraintViolationException("Constraint violation: " + e.getMessage());
        } catch (Exception e) {
            throw new DataBaseException("Error when accessing the database : " + e.getMessage());
        }
        return mappingService.toRequestDto(newOffer);
    }

    @Override
    public OfferRequestWithPaginationDto findOffers(Pageable pageable) {
        if (pageable == null) throw new PageableIsNullException();

        Page<Offer> pageOfOffers = offerRepository.findAllByIsActiveTrue(pageable);

        if (pageOfOffers.isEmpty()) throw new OffersNotFoundException();

        List<OfferRequestDto> offers = pageOfOffers
                .map(mappingService::toRequestDto)
                .getContent();

        return OfferRequestWithPaginationDto.builder()
                .offers(offers)
                .pageNumber(pageOfOffers.getNumber())
                .pageSize(pageOfOffers.getSize())
                .totalPages(pageOfOffers.getTotalPages())
                .totalElements(pageOfOffers.getTotalElements())
                .isFirstPage(pageOfOffers.isFirst())
                .isLastPage(pageOfOffers.isLast())
                .build();
    }

    @Override
    public OfferRequestDto findOfferById(UUID id) {
        if (id == null) throw new IdIsNullException();

        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new OfferNotFoundException(id));

        return mappingService.toRequestDto(offer);
    }

    @Transactional
    @Override
    public void updateOffer(OfferUpdateDto dto) {
        if (dto == null) throw new OfferUpdateDtoIsNullException();
        Offer offer = offerRepository.findById(dto.getId())
                .orElseThrow(() -> new OfferNotFoundException(dto.getId()));

        offer.setTitle(dto.getTitle() == null ? offer.getTitle() : dto.getTitle());
        offer.setDescription(dto.getDescription() == null ? offer.getDescription() : dto.getDescription());
        offer.setAuctionDurationDays(dto.getAuctionDurationDays() == null ?
                offer.getAuctionDurationDays() : dto.getAuctionDurationDays());
        offer.setStartPrice(dto.getStartPrice() == null ?
                offer.getStartPrice() : dto.getStartPrice());
        offer.setStep(dto.getStep() == null ? offer.getStep() : dto.getStep());
        offer.setWinBid(dto.getWinBid() == null ? offer.getWinBid() : dto.getWinBid());
        offer.setIsFree(dto.getIsFree() == null ? offer.getIsFree() : dto.getIsFree());

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
}

package de.ait.secondlife.service;

import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.dto.OfferRequestDto;
import de.ait.secondlife.domain.dto.OfferRequestWithPaginationDto;
import de.ait.secondlife.domain.dto.OfferUpdateDto;
import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.exception.exeptions.*;
import de.ait.secondlife.repository.OfferRepository;
import de.ait.secondlife.service.interfaces.OfferService;
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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final OfferMappingService mappingService;


    @Override
    public OfferRequestDto createOffer(OfferCreationDto dto) {

        if (dto == null) throw new OfferCreationDtoIsNullException();
        Offer newOffer;
        try {
            newOffer = offerRepository.save(mappingService.offerCreationDtotoOffer(dto));
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            throw new CreateOfferConstraintViolationException("Constraint violation: " + e.getMessage());
        } catch (Exception e) {
            throw new DataBaseException("Error when accessing the database : " + e.getMessage());
        }
        return mappingService.offertoOfferRequestDto(newOffer);
    }

    @Override
    public OfferRequestWithPaginationDto findOffers(Pageable pageable) {
        if (pageable == null) throw new PageableIsNullException();

        Page<Offer> pageOfOffers = offerRepository.findAll(pageable);

        if (pageOfOffers.isEmpty()) throw new OffersNotFoundException();

        List<OfferRequestDto> offers = pageOfOffers
                .map(mappingService::offertoOfferRequestDto)
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

        return mappingService.offertoOfferRequestDto(offer);
    }

    @Transactional
    @Override
    public void updateOffer(OfferUpdateDto dto) {
        if (dto == null) throw new OfferUpdateDtoIsNullException();

    }

    @Override
    public void removeOffer(UUID id) {

    }

    @Override
    public void recoverOffer(UUID id) {

    }
}

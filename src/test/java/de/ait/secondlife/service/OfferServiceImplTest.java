package de.ait.secondlife.service;

import de.ait.secondlife.constans.StatusConstans;
import de.ait.secondlife.domain.dto.OfferResponseDto;
import de.ait.secondlife.domain.dto.OfferResponseWithPaginationDto;

import de.ait.secondlife.domain.entity.Offer;
import de.ait.secondlife.domain.entity.Status;
import de.ait.secondlife.exceptionHandler.exeptions.IdIsNullException;
import de.ait.secondlife.exceptionHandler.exeptions.OfferNotFoundException;
import de.ait.secondlife.exceptionHandler.exeptions.PageableIsNullException;
import de.ait.secondlife.repository.OfferRepository;
import de.ait.secondlife.service.mapper.OfferMappingService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;



@ExtendWith(MockitoExtension.class)
class OfferServiceImplTest implements StatusConstans {

    @InjectMocks
    private OfferServiceImpl offerService;

    @Mock
    private OfferRepository offerRepository;
    @Mock
    private OfferMappingService mappingService;


    private final LocalDateTime now = LocalDateTime.now();

    private final Status statusDraft = Status.builder()
            .id(12L)
            .name(DRAFT_STATUS)
            .build();


    @Test
    void findOffers_ShouldReturnOfferResponseWithPaginationDto_WhenOfferExist() {
        List<Offer> offerList = new ArrayList<>();
        Pageable pageable = PageRequest.of(0, 10);
        Random random = new Random();
        for (int i = 0; i < 9; i++) {
            Offer offer = Offer.builder()
                    .id(UUID.randomUUID())
                    .title("title" + i)
                    .description("description" + i)
                    .createdAt(now)
                    .auctionDurationDays(random.nextInt(5) + 1)
                    .startPrice(BigDecimal.valueOf(100 + (1000 - 100) * random.nextDouble()))
                    .step(BigDecimal.valueOf(10 + (100 - 10) * random.nextDouble()))
                    .winBid(BigDecimal.valueOf(1000))
                    .isFree(i % 2 == 0)
                    .isActive(true)
                    .userId((long) (random.nextInt(5) + 1))
                    .status(statusDraft)
                    .categoryId((long) (random.nextInt(5) + 1))
                    .build();
            offerList.add(offer);
        }

        Page<Offer> offerPage = new PageImpl<>(offerList, pageable, offerList.size());
        Mockito.when(offerRepository.findAllByIsActiveTrue(pageable)).thenReturn(offerPage);

        Set<OfferResponseDto> dtoList = offerList.stream()
                .map(x -> OfferResponseDto.builder()
                        .id(x.getId())
                        .title(x.getTitle())
                        .description(x.getDescription())
                        .endAt(now.plusDays(x.getAuctionDurationDays()))
                        .startPrice(x.getStartPrice())
                        .step(x.getStep())
                        .winBid(x.getWinBid())
                        .isFree(x.getIsFree())
                        .ownerId(x.getUserId())
                        .statusId(x.getStatus().getId())
                        .categoryId(x.getCategoryId())
                        .build())
                .collect(Collectors.toSet());
        Mockito.when(mappingService.toRequestDto(any(Offer.class)))
                .thenAnswer(x -> {
                    Offer offer = x.getArgument(0);
                    return dtoList.stream()
                            .filter(dto -> dto.getId().equals(offer.getId()))
                            .findFirst()
                            .orElse(null);
                });

        OfferResponseWithPaginationDto result = offerService.findOffers(pageable);

        assertNotNull(result);
        assertEquals(9, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getTotalPages());
        assertEquals(9, result.getOffers().size());
        verify(offerRepository, times(1)).findAllByIsActiveTrue(pageable);

    }

    @Test
    void findOffers_ShouldReturnPageableIsNullException_WhenPageableIsNull() {
        assertThrows(PageableIsNullException.class, () -> offerService.findOffers(null));
    }



    @ParameterizedTest
    @MethodSource("offerListIndex")
    void findOfferById_ShouldReturnOfferResponseDto_WhenOfferExist(Integer index) {
        List<Offer> offerList = new ArrayList<>();

        Random random = new Random();
        for (int i = 0; i < 9; i++) {
            Offer offer = Offer.builder()
                    .id(UUID.randomUUID())
                    .title("title" + i)
                    .description("description" + i)
                    .createdAt(now)
                    .auctionDurationDays(random.nextInt(5) + 1)
                    .startPrice(BigDecimal.valueOf(100 + (1000 - 100) * random.nextDouble()))
                    .step(BigDecimal.valueOf(10 + (100 - 10) * random.nextDouble()))
                    .winBid(BigDecimal.valueOf(1000))
                    .isFree(i % 2 == 0)
                    .isActive(true)
                    .userId((long) (random.nextInt(5) + 1))
                    .status(statusDraft)
                    .categoryId((long) (random.nextInt(5) + 1))
                    .build();
            offerList.add(offer);
        }

        Mockito.when(offerRepository.findByIdAndIsActiveTrue(offerList.get(index).getId()))
                .thenReturn(Optional.ofNullable(offerList.get(index)));

        List<OfferResponseDto> dtoList = offerList.stream()
                .map(x -> OfferResponseDto.builder()
                        .id(x.getId())
                        .title(x.getTitle())
                        .description(x.getDescription())
                        .endAt(now.plusDays(x.getAuctionDurationDays()))
                        .startPrice(x.getStartPrice())
                        .step(x.getStep())
                        .winBid(x.getWinBid())
                        .isFree(x.getIsFree())
                        .ownerId(x.getUserId())
                        .statusId(x.getStatus().getId())
                        .categoryId(x.getCategoryId())
                        .build())
                .toList();
        Mockito.when(mappingService.toRequestDto(any(Offer.class)))
                .thenAnswer(x -> {
                    Offer offer = x.getArgument(0);
                    return dtoList.stream()
                            .filter(dto -> dto.getId().equals(offer.getId()))
                            .findFirst()
                            .orElse(null);
                });

        Assertions.assertEquals(dtoList.get(index), offerService.findOfferById(offerList.get(index).getId()));
    }


    private static Stream<Integer> offerListIndex(){
        return Stream.of(1,3,6,7 );
    }

    @Test
    void findOfferById_ShouldReturnIdIsNullException_WhenIdIsNull() {
        assertThrows(IdIsNullException.class, () -> offerService.findOfferById(null));
    }

    @Test
    void findOfferById_ShouldReturnOfferNotFoundException__WhenOfferNotFound() {
        Mockito.when(offerRepository.findByIdAndIsActiveTrue(UUID.fromString("96418e14-c685-42a0-b8e9-f0f86e2c974e")))
                .thenReturn(Optional.empty());
        assertThrows(OfferNotFoundException.class, () -> offerService.findOfferById(UUID.fromString("96418e14-c685-42a0-b8e9-f0f86e2c974e")));
    }


    @Test
    void findOffersByUserId() {
    }

    @Test
    void createOffer() {
    }

    @Test
    void updateOffer() {
    }

    @Test
    void removeOffer() {
    }

    @Test
    void recoverOffer() {
    }
}
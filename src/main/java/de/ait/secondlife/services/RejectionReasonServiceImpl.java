package de.ait.secondlife.services;

import de.ait.secondlife.domain.dto.RejectionReasonsDto;
import de.ait.secondlife.domain.entity.RejectionReason;
import de.ait.secondlife.repositories.RejectionReasonRepository;
import de.ait.secondlife.services.interfaces.RejectionReasonService;
import de.ait.secondlife.services.mapping.RejectionReasonMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of the RejectionReasonService interface for managing rejection reasons.(Version 1.0)
 *
 * <p>
 * This service class provides methods to retrieve rejection reasons and their DTO representations.
 * It interacts with repositories for retrieving rejection reason entities and with mapping services
 * for converting rejection reasons to DTOs.
 * </p>
 *
 * <p>
 * The {@link RejectionReasonServiceImpl#getById(Long)} method retrieves a rejection reason by its ID,
 * returning null if no reason is found.
 * </p>
 *
 * <p>
 * The {@link RejectionReasonServiceImpl#getAll()} method retrieves all rejection reasons as DTOs
 * encapsulated in a {@link RejectionReasonsDto} object.
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
public class RejectionReasonServiceImpl implements RejectionReasonService {

    private final RejectionReasonRepository reasonRepository;
    private final RejectionReasonMappingService mappingService;

    /**
     * Retrieves a rejection reason by its ID.
     *
     * @param id the ID of the rejection reason to retrieve
     * @return the rejection reason with the specified ID, or null if no such reason exists
     * @throws IllegalArgumentException if the provided ID is null
     */
    @Override
    public RejectionReason getById(Long id) {
        return reasonRepository.findById(id).orElse(null);
    }

    /**
     * Retrieves all rejection reasons as DTOs.
     *
     * @return a {@link RejectionReasonsDto} object containing all rejection reasons as DTOs
     */
    @Override
    public RejectionReasonsDto getAll() {
        RejectionReasonsDto reasonsDto = new RejectionReasonsDto();
        reasonsDto.setReasons(reasonRepository.findAll()
                .stream()
                .map(mappingService::toDto)
                .toList());

        return reasonsDto;
    }

}

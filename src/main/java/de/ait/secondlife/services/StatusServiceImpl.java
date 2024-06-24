package de.ait.secondlife.services;

import de.ait.secondlife.constants.OfferStatus;
import de.ait.secondlife.domain.entity.Status;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.is_null_exceptions.IdIsNullException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.StatusNotFoundException;
import de.ait.secondlife.repositories.StatusRepository;
import de.ait.secondlife.services.interfaces.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of the StatusService interface for retrieving status information.(Version 1.0)
 *
 * <p>
 * This service class provides methods to retrieve status entities based on their IDs or offer statuses.
 * It interacts with a repository for retrieving status entities.
 * </p>
 *
 * <p>
 * The {@link StatusServiceImpl#getStatusById(Long)} method retrieves a status entity by its ID.
 * It throws an {@link IdIsNullException} if the provided ID is null, or a {@link StatusNotFoundException}
 * if no status entity with the specified ID is found.
 * </p>
 *
 * <p>
 * The {@link StatusServiceImpl#getByOfferStatus(OfferStatus)} method retrieves a status entity by its offer status.
 * It throws a {@link StatusNotFoundException} if no status entity with the specified offer status is found.
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
public class StatusServiceImpl implements StatusService {

    private final StatusRepository statusRepository;

    /**
     * Retrieves a status entity by its ID.
     *
     * @param id the ID of the status entity to retrieve
     * @return the status entity with the specified ID
     * @throws IdIsNullException if the provided ID is null
     * @throws StatusNotFoundException if no status entity with the specified ID is found
     */
    @Override
    public Status getStatusById(Long id) {
        if (id == null) throw new IdIsNullException();
        return statusRepository.findById(id)
                .orElseThrow(() -> new StatusNotFoundException(id));
    }

    /**
     * Retrieves a status entity by its offer status.
     *
     * @param offerStatus the offer status enum value
     * @return the status entity with the specified offer status
     * @throws StatusNotFoundException if no status entity with the specified offer status is found
     */
    @Override
    public Status getByOfferStatus(OfferStatus offerStatus) {
        return statusRepository.findByName(offerStatus)
                .orElseThrow(() -> new StatusNotFoundException(offerStatus));
    }
}

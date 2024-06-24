package de.ait.secondlife.services;

import de.ait.secondlife.domain.dto.LocationDto;
import de.ait.secondlife.domain.entity.Location;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.is_null_exceptions.IdIsNullException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.LocationNotFoundException;
import de.ait.secondlife.repositories.LocationRepository;
import de.ait.secondlife.services.interfaces.LocationService;
import de.ait.secondlife.services.mapping.LocationMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of the LocationService interface. (Version 1.0)
 * This service provides methods to retrieve location information
 * and mappings between entities and their respective locations.
 * It interacts with a LocationMappingService for mapping entities
 * to DTOs and a LocationRepository for accessing location data.
 *
 * <p>
 * This class ensures that location IDs provided are valid and performs
 * necessary checks to handle exceptions such as {@link IllegalArgumentException},
 * {@link LocationNotFoundException}, and {@link IdIsNullException}.
 * </p>
 *
 * <p>
 * The methods in this service allow fetching location details by ID,
 * retrieving all available locations, and obtaining a Location entity
 * based on its ID. These methods utilize the LocationMappingService
 * to convert Location entities to DTOs for external use.
 * </p>
 *
 * <p>
 * Note: This service assumes that the LocationMappingService is properly
 * configured to handle mappings between Location entities and DTOs.
 * </p>
 *
 * <p>
 * Exceptions that may be thrown by this class include:
 * <ul>
 *     <li>{@link IllegalArgumentException} - if the provided ID is null or less than 1</li>
 *     <li>{@link LocationNotFoundException} - if no location with the given ID exists</li>
 *     <li>{@link IdIsNullException} - if the provided ID is null</li>
 * </ul>
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
public class LocationServiceImpl implements LocationService {

    private final LocationMappingService mappingService;

    private final LocationRepository repository;

    /**
     * Retrieves a LocationDto object by its ID.
     *
     * @param id the ID of the location to retrieve
     * @return LocationDto representing the location information
     * @throws IllegalArgumentException  if the provided ID is null or less than 1
     * @throws LocationNotFoundException if no location with the given ID exists
     */
    @Override
    public LocationDto getById(Long id) {

        if (id == null || id < 1) {
            throw new IllegalArgumentException("Location ID is incorrect");
        }

        Location location = repository.findById(id).orElseThrow(() -> new LocationNotFoundException(id));

        return mappingService.toDto(location);
    }

    /**
     * Retrieves a list of all LocationDto objects.
     *
     * @return List of LocationDto representing all locations
     */
    @Override
    public List<LocationDto> getAll() {
        return repository.findAll()
                .stream()
                .map(mappingService::toDto)
                .toList();
    }

    /**
     * Retrieves a Location entity by its ID.
     *
     * @param id the ID of the location entity to retrieve
     * @return Location entity corresponding to the given ID
     * @throws IdIsNullException         if the provided ID is null
     * @throws LocationNotFoundException if no location with the given ID exists
     */
    @Override
    public Location getLocationById(Long id) {
        if (id == null) throw new IdIsNullException();
        return repository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));
    }
}

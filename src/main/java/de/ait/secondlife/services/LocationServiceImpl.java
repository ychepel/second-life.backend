package de.ait.secondlife.services;

import de.ait.secondlife.domain.dto.LocationDto;
import de.ait.secondlife.domain.entity.Location;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.LocationNotFoundException;
import de.ait.secondlife.repositories.LocationsRepository;
import de.ait.secondlife.services.interfaces.LocationService;
import de.ait.secondlife.services.mapping.LocationMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationMappingService mappingService;

    private final LocationsRepository repository;
    @Override
    public LocationDto getById(Long id) {

        if (id == null || id < 1) {
            throw new IllegalArgumentException("Location ID is incorrect");
        }

        Location location = repository.findById(id).orElseThrow( ()-> new LocationNotFoundException(id));

        return mappingService.toDto(location);
    }

    @Override
    public List<LocationDto> getAll() {
        return repository.findAll()
                .stream()
                .map(mappingService::toDto)
                .toList();
    }
}

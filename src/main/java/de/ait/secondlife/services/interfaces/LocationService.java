package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.dto.LocationDto;

import java.util.List;

public interface LocationService {

    LocationDto getById(Long id);

    List<LocationDto> getAll();
}

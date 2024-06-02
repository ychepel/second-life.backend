package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.dto.LocationDto;
import de.ait.secondlife.domain.entity.Location;

import java.util.List;

public interface LocationService {

    LocationDto getById(Long id);

    List<LocationDto> getAll();

    Location getLocationById(Long id);
}

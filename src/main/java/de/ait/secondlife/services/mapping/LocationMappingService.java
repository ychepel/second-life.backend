package de.ait.secondlife.services.mapping;

import de.ait.secondlife.domain.dto.LocationDto;
import de.ait.secondlife.domain.entity.Location;
import org.mapstruct.Mapper;

@Mapper
public interface LocationMappingService {

    Location toEntity(LocationDto dto);

    LocationDto toDto(Location entity);
}

package de.ait.secondlife.services.mapping;

import de.ait.secondlife.domain.dto.UserDto;
import de.ait.secondlife.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMappingService {

    User toEntity(UserDto userDto);

    @Mapping(target = "images", ignore = true)
    UserDto toDto(User user);
}

package de.ait.secondlife.services.mapping;

import de.ait.secondlife.domain.dto.UserDto;
import de.ait.secondlife.domain.entity.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMappingService {

    User toEntity(UserDto userDto);

    UserDto toDto(User user);
}

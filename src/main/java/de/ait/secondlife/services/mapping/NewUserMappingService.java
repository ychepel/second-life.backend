package de.ait.secondlife.services.mapping;

import de.ait.secondlife.domain.dto.UserCreationDto;
import de.ait.secondlife.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface NewUserMappingService {

    User toEntity(UserCreationDto newUserDto);

    @Mapping(target = "password", ignore = true)
    UserCreationDto toDto(User user);
}

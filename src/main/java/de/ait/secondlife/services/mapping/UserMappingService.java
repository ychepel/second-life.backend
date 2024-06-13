package de.ait.secondlife.services.mapping;

import de.ait.secondlife.domain.dto.UserDto;
import de.ait.secondlife.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public abstract class UserMappingService extends EntityWIthImageMappingService {

    public abstract User toEntity(UserDto userDto);

    @Mapping(target = "images", expression = "java(getImages(user))")
    public abstract UserDto toDto(User user);
}

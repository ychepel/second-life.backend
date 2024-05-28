package de.ait.secondlife.services.mapping;

import de.ait.secondlife.domain.dto.NewUserDto;
import de.ait.secondlife.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface NewUserMappingService {

    User toEntity(NewUserDto newUserDto);

    @Mapping(target = "password", ignore = true)
    NewUserDto toDto(User user);
}

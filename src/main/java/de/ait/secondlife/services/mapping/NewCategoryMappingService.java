package de.ait.secondlife.services.mapping;

import de.ait.secondlife.domain.dto.CategoryDto;
import de.ait.secondlife.domain.dto.NewCategoryDto;
import de.ait.secondlife.domain.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface NewCategoryMappingService {

    @Mapping(target = "images", ignore = true)
    CategoryDto mapEntityToDto(Category entity);

    Category mapDtoToEntity(NewCategoryDto dto);
}

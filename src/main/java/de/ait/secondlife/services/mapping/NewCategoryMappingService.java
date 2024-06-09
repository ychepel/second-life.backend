package de.ait.secondlife.services.mapping;

import de.ait.secondlife.domain.dto.CategoryDto;
import de.ait.secondlife.domain.dto.CategoryCreationDto;
import de.ait.secondlife.domain.entity.Category;
import org.mapstruct.Mapper;

@Mapper
public interface NewCategoryMappingService {

    CategoryDto toDto(Category entity);

    Category toEntity(CategoryCreationDto dto);
}

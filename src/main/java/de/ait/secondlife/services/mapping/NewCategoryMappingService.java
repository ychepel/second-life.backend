package de.ait.secondlife.services.mapping;

import de.ait.secondlife.domain.dto.CategoryDto;
import de.ait.secondlife.domain.dto.NewCategoryDto;
import de.ait.secondlife.domain.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public abstract class NewCategoryMappingService extends EntityWIthImageMappingService {

    @Mapping(target = "images", expression = "java(getImages(entity))")
    public abstract CategoryDto mapEntityToDto(Category entity);

    public abstract Category mapDtoToEntity(NewCategoryDto dto);
}

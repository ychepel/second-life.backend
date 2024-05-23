package de.ait.secondlife.services.mapping;

import de.ait.secondlife.dto.CategoryDto;
import de.ait.secondlife.models.Category;
import org.mapstruct.Mapper;

@Mapper
public interface CategoryMappingService {


    CategoryDto mapEntityToDto(Category entity);


    //    @Mapping(target = "id", ignore = true)
    Category mapDtoToEntity(CategoryDto dto);
}

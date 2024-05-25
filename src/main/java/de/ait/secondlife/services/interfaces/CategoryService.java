package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.dto.CategoryDto;
import de.ait.secondlife.dto.IsActiveCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto getById(Long id);
    List<CategoryDto> getAll();
    CategoryDto save(CategoryDto category);
    CategoryDto update(Long id, CategoryDto category);
    CategoryDto update(Long id, IsActiveCategoryDto category);

}

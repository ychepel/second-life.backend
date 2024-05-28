package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.dto.CategoryDto;
import de.ait.secondlife.domain.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto getById(Long id);
    List<CategoryDto> getAll();
    CategoryDto save(NewCategoryDto category);
    CategoryDto update(Long id, CategoryDto category);

    CategoryDto setActive(Long categoryId);

    CategoryDto hide(Long categoryId);
}

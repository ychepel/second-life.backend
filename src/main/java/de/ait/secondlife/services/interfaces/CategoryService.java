package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.dto.CategoryDto;
import de.ait.secondlife.domain.dto.CategoryCreationDto;
import de.ait.secondlife.domain.entity.Category;

import java.util.List;

public interface CategoryService {

    CategoryDto getById(Long id);
    List<CategoryDto> getAll();
    CategoryDto save(CategoryCreationDto category);
    CategoryDto update(Long id, CategoryDto category);
    CategoryDto setActive(Long categoryId);
    CategoryDto hide(Long categoryId);
    Category getCategoryById(Long id);
}

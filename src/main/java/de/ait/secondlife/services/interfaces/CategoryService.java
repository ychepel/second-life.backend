package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.dto.CategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto getById(Long id);

    List<CategoryDto> getAll();

    CategoryDto save(CategoryDto category);

    void update(Long id, CategoryDto category);
}

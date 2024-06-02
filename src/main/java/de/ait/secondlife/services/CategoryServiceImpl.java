package de.ait.secondlife.services;

import de.ait.secondlife.domain.dto.CategoryDto;
import de.ait.secondlife.domain.dto.NewCategoryDto;
import de.ait.secondlife.domain.entity.Category;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.BadRequestException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.CategoryIsNotEmptyException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.is_null_exceptions.IdIsNullException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.CategoryNotFoundException;
import de.ait.secondlife.repositories.CategoriesRepository;
import de.ait.secondlife.services.interfaces.CategoryService;
import de.ait.secondlife.services.mapping.NewCategoryMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.method.MethodValidationException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoriesRepository repository;

    private final NewCategoryMappingService mappingService;

    @Override
    public CategoryDto getById(Long id) {

        if (id == null || id < 1) {
            throw new IllegalArgumentException("Category ID is incorrect");
        }

        Category category = repository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));

        return mappingService.mapEntityToDto(category);
    }

    @Override
    public List<CategoryDto> getAll() {
        return repository.findAll()
                .stream()
                .filter(x -> x.isActive())
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    @Override
    public CategoryDto save(NewCategoryDto categoryDto) {

        Category entity = mappingService.mapDtoToEntity(categoryDto);

        try {
            repository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("Cannot save category to db");
        }

        return mappingService.mapEntityToDto(entity);
    }

    @Override
    public CategoryDto update(Long id, CategoryDto dto) {

        Category existingCategory = repository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));

        existingCategory.setName(dto.getName());
        existingCategory.setDescription(dto.getDescription());

        try {
            return mappingService.mapEntityToDto(repository.save(existingCategory));
        } catch (Exception e) {
            throw new RuntimeException("Cannot save category to db");
        }
    }

    @Override
    public CategoryDto setActive(Long categoryId) {

        Category existingCategory = repository.findById(categoryId).orElseThrow(() -> new CategoryNotFoundException(categoryId));

        existingCategory.setActive(true);

        try {
            return mappingService.mapEntityToDto(repository.save(existingCategory));
        } catch (Exception e) {
            throw new RuntimeException("Cannot save category to db ", e);
        }
    }

    @Override
    public CategoryDto hide(Long categoryId) {
        Category existingCategory = repository.findById(categoryId).orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        if (!existingCategory.getOffers().isEmpty()) throw new CategoryIsNotEmptyException(categoryId);

        existingCategory.setActive(false);

        try {
            return mappingService.mapEntityToDto(repository.save(existingCategory));
        } catch (Exception e) {
            throw new RuntimeException("Cannot save category to db ", e);
        }
    }

    @Override
    public Category getCategoryById(Long id) {
        if (id == null) throw new IdIsNullException();
        return repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}

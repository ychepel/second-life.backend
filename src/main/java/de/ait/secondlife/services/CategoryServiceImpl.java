package de.ait.secondlife.services;

import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.domain.dto.CategoryDto;
import de.ait.secondlife.domain.dto.CategoryCreationDto;
import de.ait.secondlife.domain.dto.CategoryUpdateDto;
import de.ait.secondlife.domain.entity.Category;
import de.ait.secondlife.exception_handling.exceptions.DuplicateCategoryException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.CategoryIsNotEmptyException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.is_null_exceptions.IdIsNullException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.CategoryNotFoundException;
import de.ait.secondlife.repositories.CategoryRepository;
import de.ait.secondlife.services.interfaces.CategoryService;
import de.ait.secondlife.services.interfaces.ImageService;
import de.ait.secondlife.services.mapping.NewCategoryMappingService;
import de.ait.secondlife.services.utilities.UserPermissionsUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    private final NewCategoryMappingService mappingService;

    private final UserPermissionsUtilities utilities;
    @Lazy
    @Autowired
    private  ImageService imageService;

    @Override
    public CategoryDto getById(Long id) {

        if (id == null || id < 1) {
            throw new IllegalArgumentException("Category ID is incorrect");
        }

        Category category = repository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));

        return mappingService.toDto(category);
    }

    @Override
    public List<CategoryDto> getAll() {
        return repository.findAll()
                .stream()
                .filter(Category::isActive)
                .map(mappingService::toDto)
                .toList();
    }

    @Override
    public List<CategoryDto> getAllPlusHidden() {
        return repository.findAll()
                .stream()
                .map(mappingService::toDto)
                .toList();
    }

    @Override
    public CategoryDto save(CategoryCreationDto categoryDto) {

        utilities.checkUserPermissionsForImageByBaseName(categoryDto.getBaseNameOfImages());

        String categoryName = categoryDto.getName();
        if (repository.existsByName(categoryName)) {
            throw new DuplicateCategoryException(categoryName);
        }

        Category entity = mappingService.toEntity(categoryDto);

        try {
            Category newCategory = repository.save(entity);
            imageService.connectTempImagesToEntity(
                    categoryDto.getBaseNameOfImages(),
                    EntityTypeWithImages.CATEGORY.getType(),
                    newCategory.getId());
        } catch (Exception e) {
            throw new RuntimeException("Cannot save category to db", e);
        }
        return mappingService.toDto(entity);
    }

    @Override
    public CategoryDto update(Long id, CategoryUpdateDto dto) {

        Category existingCategory = repository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));

        existingCategory.setName(dto.getName());
        existingCategory.setDescription(dto.getDescription());
        utilities.checkUserPermissionsForImageByBaseName(dto.getBaseNameOfImages());
        imageService.connectTempImagesToEntity(
                dto.getBaseNameOfImages(),
                EntityTypeWithImages.CATEGORY.getType(),
                id);
        try {
            return mappingService.toDto(repository.save(existingCategory));
        } catch (Exception e) {
            throw new RuntimeException("Cannot save category to db", e);
        }
    }

    @Override
    public CategoryDto setActive(Long categoryId) {

        Category existingCategory = repository.findById(categoryId).orElseThrow(() -> new CategoryNotFoundException(categoryId));

        existingCategory.setActive(true);

        try {
            return mappingService.toDto(repository.save(existingCategory));
        } catch (Exception e) {
            throw new RuntimeException("Cannot save category to db", e);
        }
    }

    @Override
    public CategoryDto hide(Long categoryId) {
        Category existingCategory = repository.findById(categoryId).orElseThrow(() -> new CategoryNotFoundException(categoryId));

        if (!existingCategory.getOffers().isEmpty()) throw new CategoryIsNotEmptyException(categoryId);

        existingCategory.setActive(false);

        try {
            return mappingService.toDto(repository.save(existingCategory));
        } catch (Exception e) {
            throw new RuntimeException("Cannot save category to db", e);
        }
    }

    @Override
    public Category getCategoryById(Long id) {
        if (id == null) throw new IdIsNullException();
        return repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }



    @Override
    public boolean checkEntityExistsById(Long id) {
        if (id == null) throw new IdIsNullException();
        return repository.existsByIdAndActiveTrue(id);
    }
}

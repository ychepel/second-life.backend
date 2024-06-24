package de.ait.secondlife.services;

import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.domain.dto.CategoryCreationDto;
import de.ait.secondlife.domain.dto.CategoryDto;
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

/**
 * Service implementation for managing categories.(Version 1.0)
 * This service provides methods for retrieving, saving, updating, and hiding categories,
 * along with various validations and utilities related to categories.
 *
 * <p>
 * This service interacts with the CategoryRepository, NewCategoryMappingService, UserPermissionsUtilities,
 * and ImageService.
 * </p>
 *
 * <p>
 * Exceptions that may be thrown by this class include:
 * <ul>
 *     <li>{@link IllegalArgumentException} - if the category ID is invalid</li>
 *     <li>{@link CategoryNotFoundException} - if a category is not found</li>
 *     <li>{@link DuplicateCategoryException} - if a category with the same name already exists</li>
 *     <li>{@link RuntimeException} - if there is an issue saving the category to the database</li>
 *     <li>{@link CategoryIsNotEmptyException} - if attempting to hide a non-empty category</li>
 *     <li>{@link IdIsNullException} - if the provided ID is null</li>
 * </ul>
 * </p>
 *
 * <p>
 * Author: Second Life Team
 * </p>
 *
 * @version 1.0
 * @author: Second Life Team
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    private final NewCategoryMappingService mappingService;

    private final UserPermissionsUtilities utilities;
    @Lazy
    @Autowired
    private ImageService imageService;

    /**
     * Retrieves a category by its ID.
     *
     * @param id the ID of the category to be retrieved.
     * @return the CategoryDto object.
     * @throws IllegalArgumentException  if the category ID is invalid.
     * @throws CategoryNotFoundException if the category is not found.
     */
    @Override
    public CategoryDto getById(Long id) {

        if (id == null || id < 1) {
            throw new IllegalArgumentException("Category ID is incorrect");
        }

        Category category = repository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));

        return mappingService.toDto(category);
    }

    /**
     * Retrieves all active categories.
     *
     * @return a list of CategoryDto objects.
     */
    @Override
    public List<CategoryDto> getAll() {
        return repository.findAll()
                .stream()
                .filter(Category::isActive)
                .map(mappingService::toDto)
                .toList();
    }

    /**
     * Retrieves all categories, including hidden ones.
     *
     * @return a list of CategoryDto objects.
     */
    @Override
    public List<CategoryDto> getAllPlusHidden() {
        return repository.findAll()
                .stream()
                .map(mappingService::toDto)
                .toList();
    }

    /**
     * Saves a new category based on the provided CategoryCreationDto.
     *
     * @param categoryDto the data transfer object containing category creation details.
     * @return the saved CategoryDto object.
     * @throws DuplicateCategoryException if a category with the same name already exists.
     * @throws RuntimeException           if there is an issue saving the category to the database.
     */
    @Override
    public CategoryDto save(CategoryCreationDto categoryDto) {

        if (categoryDto.getBaseNameOfImages() != null)
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

    /**
     * Updates an existing category based on the provided ID and CategoryUpdateDto.
     *
     * @param id  the ID of the category to be updated.
     * @param dto the data transfer object containing category update details.
     * @return the updated CategoryDto object.
     * @throws CategoryNotFoundException if the category is not found.
     * @throws RuntimeException          if there is an issue saving the category to the database.
     */
    @Override
    public CategoryDto update(Long id, CategoryUpdateDto dto) {

        Category existingCategory = repository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));

        existingCategory.setName(dto.getName());
        existingCategory.setDescription(dto.getDescription());
        if (dto.getBaseNameOfImages() != null)
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

    /**
     * Activates an existing category by setting its active status to true.
     *
     * @param categoryId the ID of the category to be activated.
     * @return the updated CategoryDto object.
     * @throws CategoryNotFoundException if the category is not found.
     * @throws RuntimeException          if there is an issue saving the category to the database.
     */
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

    /**
     * Hides an existing category by setting its active status to false.
     *
     * @param categoryId the ID of the category to be hidden.
     * @return the updated CategoryDto object.
     * @throws CategoryNotFoundException   if the category is not found.
     * @throws CategoryIsNotEmptyException if the category has associated offers.
     * @throws RuntimeException            if there is an issue saving the category to the database.
     */
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

    /**
     * Retrieves a category entity by its ID.
     *
     * @param id the ID of the category to be retrieved.
     * @return the Category entity.
     * @throws IdIsNullException         if the provided ID is null.
     * @throws CategoryNotFoundException if the category is not found.
     */
    @Override
    public Category getCategoryById(Long id) {
        if (id == null) throw new IdIsNullException();
        return repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    /**
     * Checks if a category entity exists by its ID and is active.
     *
     * @param id the ID of the category to be checked.
     * @return true if the category exists and is active, false otherwise.
     * @throws IdIsNullException if the provided ID is null.
     */
    @Override
    public boolean checkEntityExistsById(Long id) {
        if (id == null) throw new IdIsNullException();
        return repository.existsByIdAndActiveTrue(id);
    }
}
